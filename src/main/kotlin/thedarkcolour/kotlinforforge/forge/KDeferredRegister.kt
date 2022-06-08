package thedarkcolour.kotlinforforge.forge

import net.minecraftforge.registries.DeferredRegister
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

/**
 * Inline function to replace ObjectHolderDelegate
 *
 * @param name Path used in the registry name of this object
 * (if modid of deferred register is "foo", name "bar" would create "foo:bar")
 *
 * @param V Type of deferred register
 * @param T Specific type of object being registered
 */
public inline fun <V, T : V> KDeferredRegister<V>.registerObject(
    name: String,
    noinline supplier: () -> T
): ReadOnlyProperty<Any?, T> {
    val registryObject = this.register(name, supplier)

    // note that this anonymous class inherits three types
    return object : ReadOnlyProperty<Any?, T>, Supplier<T>, () -> T {
        override fun get(): T = registryObject.get()

        override fun getValue(thisRef: Any?, property: KProperty<*>): T = get()

        override fun invoke(): T = get()
    }
}