package thedarkcolour.kotlinforforge.forge

import net.minecraft.resources.ResourceLocation
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.*

/**
 * Alternative version of [DeferredRegister] that creates
 * [ObjectHolderDelegate] instances instead of [RegistryObject].
 */
public class KDeferredRegister<V : IForgeRegistryEntry<V>>(
    public val superType: Class<V>,
    public val modid: String,
) {
    private val entries = LinkedHashMap<ObjectHolderDelegate<out V>, () -> V>()

    private var type: IForgeRegistry<V>? = null
    private var registryFactory: (() -> RegistryBuilder<V>)? = null
    private var seenRegistryEvent = false

    // Use primary constructor for custom registries
    public constructor(registry: IForgeRegistry<V>, modid: String) : this(registry.registrySuperType, modid) {
        type = registry
    }

    /*/**
     * TODO: Uncomment when Forge fixes language providers for 1.17
     * Registers this deferred register to the `KotlinEventBus`.
     */
    public fun register(bus: IKotlinEventBus) {
        bus.addGenericListener(superType, ::addEntries)

        // Handle registry creation
        if (type == null && registryFactory != null) {
            bus.addListener(::createRegistry)
        }
    }*/

    private fun createRegistry(event: RegistryEvent.NewRegistry) {
        type = registryFactory!!.invoke().create()
    }

    /**
     * Registers this deferred register to the `IEventBus`.
     */
    //@Deprecated("Use a KotlinEventBus. Forge's EventBus does not support function references for event listeners.")
    public fun register(bus: IEventBus) {
        // function references are not supported by Forge's eventbus
        bus.addGenericListener(superType) { event: RegistryEvent.Register<V> ->
            addEntries(event)
        }
        if (type == null && registryFactory != null) {
            bus.addListener { event: RegistryEvent.NewRegistry ->
                createRegistry(event)
            }
        }
    }


    /**
     * Adds a registry object to this deferred registry.
     *
     * @param name the path of the new registry object's registry name with namespace [modid]
     * @param supplier the function to initialize the value of the new registry object with and
     *                 to reset the value when forge adds reloadable registries.
     *
     * @return A new [ObjectHolderDelegate] with the given registry name and value
     */
    public fun <T : V> register(name: String, supplier: () -> T): ObjectHolderDelegate<T> {
        if (seenRegistryEvent) {
            throw IllegalStateException("Cannot register new entries to KDeferredRegister after RegistryEvent.Register has been fired.")
        }
        val key = ResourceLocation(modid, name)

        val delegate = if (type != null) {
            ObjectHolderDelegate<T>(key, type!!)
        } else {
            ObjectHolderDelegate(key, superType, modid)
        }

        if (entries.putIfAbsent(delegate) { supplier().setRegistryName(key) } != null) {
            throw IllegalArgumentException("Duplicate registration $name")
        }

        return delegate
    }

    public fun getEntries(): Set<ObjectHolderDelegate<out V>> {
        return entries.keys
    }

    /**
     * Adds all entries in this registry to the corresponding game registries.
     */
    private fun addEntries(event: RegistryEvent.Register<V>) {
        // Find an existing registry if created by another mod
        if (type == null && registryFactory == null) {
            type = RegistryManager.ACTIVE.getRegistry(superType) ?: throw IllegalStateException("Unable to find registry for type ${superType.name} for modid $modid after NewRegistry event")
        }
        if (type != null && event.genericType == type!!.registrySuperType) {
            val registry = event.registry

            for ((objectHolder, supplier) in entries) {
                registry.register(supplier())
                // pass true to always update the entry
                objectHolder.accept { true }
            }
        }
    }
}