package thedarkcolour.kotlinforforge.forge

import net.minecraftforge.fmllegacy.RegistryObject
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

public fun interface ObjectHolderDelegate<V : IForgeRegistryEntry<in V>> : ReadOnlyProperty<Any?, V>, Supplier<V>, () -> V {
    override fun getValue(thisRef: Any?, property: KProperty<*>): V {
        return get()
    }

    override fun invoke(): V {
        return get()
    }
}

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
 */
public inline fun <V : IForgeRegistryEntry<V>, T : V> KDeferredRegister<V>.registerObject(
    name: String,
    noinline supplier: () -> T
): ObjectHolderDelegate<T> {
    val registryObject = this.register<T>(name, supplier)
    return ObjectHolderDelegate { registryObject.get() }
}