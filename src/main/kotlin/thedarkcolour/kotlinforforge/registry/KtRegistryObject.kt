package thedarkcolour.kotlinforforge.registry

import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import net.minecraftforge.registries.ObjectHolderRegistry
import net.minecraftforge.registries.RegistryManager
import java.util.function.Supplier

/**
 * An alternative to DeferredRegistry that enforces non-nullability.
 */
open class KtRegistryObject<T : IForgeRegistryEntry<T>>(val name: ResourceLocation, registry: IForgeRegistry<T>) : Supplier<T> {
    private lateinit var value: T

    constructor(name: String, registry: IForgeRegistry<T>) : this(ResourceLocation(name), registry)

    @Suppress("UNCHECKED_CAST")
    constructor(name: String, registryType: Supplier<Class<out T>>) : this(ResourceLocation(name), RegistryManager.ACTIVE.getRegistry(registryType.get()) as IForgeRegistry<T>)

    init {
        ObjectHolderRegistry.addHandler { predicate ->
            if (predicate.test(registry.registryName)) {
                value = registry.getValue(name)!!
            }
        }
    }

    override fun get(): T = value

    fun refresh(registry: IForgeRegistry<out T>) {
        value = registry.getValue(name)!!
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            other === this -> true
            other?.javaClass != javaClass -> false
            (other as KtRegistryObject<*>).name != name -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}