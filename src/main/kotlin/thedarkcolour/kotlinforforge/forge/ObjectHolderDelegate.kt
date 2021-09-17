package thedarkcolour.kotlinforforge.forge

import net.minecraft.resources.ResourceLocation
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import net.minecraftforge.registries.ObjectHolderRegistry
import net.minecraftforge.registries.RegistryManager
import thedarkcolour.kotlinforforge.LOGGER
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.Supplier
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/** @since 2.0.0
 * An alternative to the `@ObjectHolder` annotation.
 *
 * This property delegate is for those who would like to avoid
 * using annotations all over their non-static Kotlin code.
 *
 * [ObjectHolderDelegate] delegates to a non-null
 * `IForgeRegistryEntry` value with registry name [registryName]
 * in an `IForgeRegistry` [registry] of type [T].
 *
 * @param T the type of object this delegates to
 * @property registryName the registry name of the object this delegate references
 * @property registry the registry the object of this delegate is in
 * @property value the current value of this object holder.
 */
public class ObjectHolderDelegate<T : IForgeRegistryEntry<in T>>(
    public val registryName: ResourceLocation,
    registry: () -> IForgeRegistry<*>,
) : ReadOnlyProperty<Any?, T>, Consumer<Predicate<ResourceLocation>>, Supplier<T>, () -> T {
    private lateinit var value: T

    // Registry that this object holder is in
    // Lazy so that modded registries can be fetched at the proper time
    public val registry: IForgeRegistry<*> by lazy(registry)

    public constructor(registryName: ResourceLocation, registry: IForgeRegistry<*>) : this(registryName, { registry }) {
        ObjectHolderRegistry.addHandler(this)
    }

    public constructor(registryName: ResourceLocation, clazz: Class<*>, modid: String) : this(registryName, getRegistryLazy(clazz, modid, Throwable("Calling site from mod: $modid")))

    override fun get(): T {
        return if (::value.isInitialized) {
            value
        } else {
            throw UninitializedPropertyAccessException("Object holder $registryName of type ${registry.registryName} has not been initialized")
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return get()
    }

    override fun invoke(): T {
        return get()
    }

    /**
     * Refreshes the value of this ObjectHolder.
     *
     * If the [registry] no longer contains [registryName],
     * the value will remain unchanged.
     */
    override fun accept(filter: Predicate<ResourceLocation>) {
        if (!filter.test(registry.registryName)) {
            return
        }

        if (registry.containsKey(registryName)) {
            val tempValue = registry.getValue(registryName)

            if (tempValue != null) {
                value = tempValue as T
            } else {
                LOGGER.debug("Unable to lookup value for $this, likely just mod options.")
            }
        }
    }

    public companion object {
        private val TYPE_2_REGISTRY = HashMap<Class<*>, IForgeRegistry<*>>()

        private fun getRegistryLazy(clazz: Class<*>, modid: String, callerStack: Throwable): () -> IForgeRegistry<*> {
            return {
                RegistryManager.ACTIVE.getRegistry(clazz) ?: throw IllegalStateException("Unable to find registry for type ${clazz.name} for mod ${modid}. Check the 'caused by' to see further stack.", callerStack)
            }
        }

        public fun getRegistry(clazz: Class<*>): IForgeRegistry<*> {
            return TYPE_2_REGISTRY.computeIfAbsent(clazz, ::findRegistry)
        }

        private fun findRegistry(clazz: Class<*>): IForgeRegistry<*> {
            val typeQueue = LinkedList<Class<*>>()
            var registry: IForgeRegistry<*>? = null

            typeQueue.add(clazz)

            while (typeQueue.isNotEmpty() && registry == null) {
                val type = typeQueue.remove()
                typeQueue.addAll(type.interfaces)

                if (IForgeRegistryEntry::class.java.isAssignableFrom(type)) {
                    registry = RegistryManager.ACTIVE.getRegistry(type)

                    val parent = type.superclass

                    if (parent != null) {
                        typeQueue.add(parent)
                    }
                }
            }

            return registry ?: throw IllegalArgumentException("ObjectHolderDelegate must represent a type that implements IForgeRegistryEntry")
        }
    }
}