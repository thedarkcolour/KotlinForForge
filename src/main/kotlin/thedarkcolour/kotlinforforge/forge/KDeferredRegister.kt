package thedarkcolour.kotlinforforge.forge

import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.RegistryObject
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import net.minecraftforge.registries.RegistryManager
import thedarkcolour.kotlinforforge.eventbus.KotlinEventBus
import kotlin.properties.ReadOnlyProperty

/**
 * Alternative version of [DeferredRegister] that creates
 * [ObjectHolderDelegate] instances instead of [RegistryObject].
 */
public class KDeferredRegister<V : IForgeRegistryEntry<V>>(
    public val registry: IForgeRegistry<V>,
    public val modid: String,
) {
    /**
     * A map of all registry objects and their value suppliers.
     */
    private val entries = HashMap<ObjectHolderDelegate<out V>, () -> V>()

    /**
     * Alternative constructor that uses a class instead of a registry.
     */
    public constructor(registryClass: Class<V>, modid: String) : this(RegistryManager.ACTIVE.getRegistry(registryClass), modid)

    /**
     * Registers this deferred register to the `KotlinEventBus`.
     */
    public fun register(bus: KotlinEventBus) {
        bus.addGenericListener(registry.registrySuperType, ::addEntries)
    }

    /**
     * Registers this deferred register to the `IEventBus`.
     */
    @Deprecated("Use a KotlinEventBus. Forge's EventBus does not support function references for event listeners.")
    public fun register(bus: IEventBus) {
        // function references are not supported by Forge's eventbus
        bus.addGenericListener(registry.registrySuperType) { event: RegistryEvent.Register<V> ->
            addEntries(event)
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
    public fun <T : V> register(name: String, supplier: () -> T): ReadOnlyProperty<Any?, T> {
        val key = ResourceLocation(modid, name)
        val a = ObjectHolderDelegate<T>(key, registry)

        entries[a] = { supplier().setRegistryName(key) }

        return a
    }

    public fun getEntries(): Set<ObjectHolderDelegate<out V>> {
        return entries.keys
    }

    /**
     * Adds all entries in this registry to the corresponding game registries.
     */
    private fun addEntries(event: RegistryEvent.Register<V>) {
        val registry = event.registry

        for ((objectHolder, supplier) in entries) {
            registry.register(supplier())
            // pass true to always update the entry
            objectHolder.accept { true }
        }
    }
}