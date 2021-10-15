package thedarkcolour.kotlinforforge.forge

import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import kotlin.properties.ReadOnlyProperty

/**
 * Got rid of KDeferredRegister because KFF can no longer access forge/mc code
 */
public typealias KDeferredRegister<T> = DeferredRegister<T>

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
public inline fun <V : IForgeRegistryEntry<V>> KDeferredRegister<V>.registerObject(
    name: String,
    noinline supplier: () -> V
): ReadOnlyProperty<Any, V> {
    val registryObject = this.register(name, supplier)
    return ReadOnlyProperty { thisRef, _ -> registryObject.get() }
}