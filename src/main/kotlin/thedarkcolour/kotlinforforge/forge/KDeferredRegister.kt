package thedarkcolour.kotlinforforge.forge

import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import java.util.function.Supplier
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Got rid of KDeferredRegister because KFF can no longer access forge/mc code
 */
public typealias KDeferredRegister<T> = DeferredRegister<T>

// TODO re-add once Jar in Jar is a thing
/*public fun interface ObjectHolderDelegate<V : IForgeRegistryEntry<in V>> : ReadOnlyProperty<Any?, V>, Supplier<V>, () -> V {
    override fun getValue(thisRef: Any?, property: KProperty<*>): V {
        return get()
    }

    override fun invoke(): V {
        return get()
    }
}*/

@Deprecated(
    message = "KDeferredRegister no longer exists in 1.17+ because of the module system",
    replaceWith = ReplaceWith(
        "DeferredRegister.create(registry, modId)",
        "net.minecraftforge.registries.DeferredRegister"
    )
)
public inline fun <V : IForgeRegistryEntry<V>> KDeferredRegister(registry: IForgeRegistry<V>, modId: String) =
    DeferredRegister.create(registry, modId)

/**
 * Inline function to replace ObjectHolderDelegate
 *
 * @param name Path used in the registry name of this object
 * (if modid of deferred register is "foo", name "bar" would create "foo:bar")
 *
 * @param V Type of deferred register
 * @param T Specific type of object being registered
 * @param OBJ "Registry object type" that represents the type of the anonymous property delegate returned
 * The returned object can be used as an instance of either `ReadOnlyProperty`, `() -> T`, or `Supplier`.
 *
 * @return `ReadOnlyProperty` delegate, can be used as `() -> T` or `Supplier<T>` if needed without casting
 */
public inline fun <V, T : V, OBJ> KDeferredRegister<V>.registerObject(
    name: String,
    noinline supplier: () -> T
): OBJ where OBJ : ReadOnlyProperty<Any?, T>, OBJ : () -> T, OBJ : Supplier<T> {
    val registryObject = this.register(name, supplier)

    // note that this anonymous class inherits three types
    return object : ReadOnlyProperty<Any?, T>, Supplier<T>, () -> T {
        override fun get(): T = registryObject.get()

        override fun getValue(thisRef: Any?, property: KProperty<*>): T = get()

        override fun invoke(): T = get()
    } as OBJ
}