package thedarkcolour.kotlinforforge.neoforge.forge

import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.RegistryObject
import java.util.function.Supplier
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @property registryObject For getting the ID or any other properties of the RegistryObject
 */
public class ObjectHolderDelegate<V>(public val registryObject: RegistryObject<V>) : ReadOnlyProperty<Any?, V>, Supplier<V>, () -> V {
    override fun getValue(thisRef: Any?, property: KProperty<*>): V = registryObject.get()
    override fun invoke(): V = registryObject.get()
    override fun get(): V = registryObject.get()
}

/**
 * Returns a registry object that can be used as a property delegate
 *
 * @param name Path used in the registry name of this object
 * (if modid of deferred register is "foo", name "bar" would create "foo:bar")
 * @param supplier Supplier to use for the internal registry object
 *
 * @param V Type of deferred register
 * @param T Specific type of object being registered
 */
public fun <V, T : V> DeferredRegister<V>.registerObject(
    name: String,
    supplier: () -> T,
): ObjectHolderDelegate<T>
{
    val registryObject = this.register(name, supplier)

    // note that this anonymous class inherits three types
    return ObjectHolderDelegate(registryObject)
}