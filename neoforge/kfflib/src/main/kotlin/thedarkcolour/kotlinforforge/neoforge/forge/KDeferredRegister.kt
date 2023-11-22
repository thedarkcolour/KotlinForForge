package thedarkcolour.kotlinforforge.neoforge.forge

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * IMPORTANT: If these functions appear broken for you, please update to latest NeoForge.
 *
 * Returns a registry object that can be used as a property delegate
 *
 * @param name Path used in the registry name of this object
 * (if modid of deferred register is "foo", name "bar" would create "foo:bar")
 * @param supplier Supplier to use for the internal registry object
 *
 * @param R Registry type of deferred register (ex. Block)
 * @param T Specific type of object being registered (ex. SaplingBlock)
 */
public fun <R, T : R> DeferredRegister<R>.registerObject(name: String, supplier: () -> T): ObjectHolderDelegate<R, T> {
    return ObjectHolderDelegate(this.register(name, supplier))
}

public fun <R, T : R> DeferredRegister<R>.registerObject(name: String, supplier: (ResourceLocation) -> T) : ObjectHolderDelegate<R, T> {
    return ObjectHolderDelegate(this.register(name, supplier))
}

public fun <T : Item> DeferredRegister.Items.registerItem(name: String, supplier: () -> T) : ItemHolderDelegate<T> {
    return ItemHolderDelegate(this.register(name, supplier))
}

public fun <T : Item> DeferredRegister.Items.registerItem(name: String, supplier: (ResourceLocation) -> T) : ItemHolderDelegate<T> {
    return ItemHolderDelegate(this.register(name, supplier))
}

public fun <T : Block> DeferredRegister.Blocks.registerBlock(name: String, supplier: () -> T) : BlockHolderDelegate<T> {
    return BlockHolderDelegate(this.register(name, supplier))
}

public fun <T : Block> DeferredRegister.Blocks.registerBlock(name: String, supplier: (ResourceLocation) -> T) : BlockHolderDelegate<T> {
    return BlockHolderDelegate(this.register(name, supplier))
}

public fun <T : Block> DeferredRegister.Items.registerBlockItem(
    name: String,
    supplier: () -> T,
    properties: Item.Properties = Item.Properties(),
) : ItemHolderDelegate<BlockItem>
{
    val holder = this.registerBlockItem(name, supplier, properties)

    return ItemHolderDelegate(holder)
}

/**
 * @property registryObject For getting the ID or any other properties of the RegistryObject
 */
public class ObjectHolderDelegate<R, T : R>(public val registryObject: DeferredHolder<R, T>) : ReadOnlyProperty<Any?, T>, Supplier<T>, () -> T {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = registryObject.get()
    override fun invoke(): T = registryObject.get()
    override fun get(): T = registryObject.get()
}

/**
 * Specialized form of ObjectHolderDelegate that contains a DeferredItem instead of a DeferredHolder
 */
public class ItemHolderDelegate<T : Item>(public val registryObject: DeferredItem<T>) : ReadOnlyProperty<Any?, T>, Supplier<T>, () -> T, ItemLike {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = registryObject.get()
    override fun invoke(): T = registryObject.get()
    override fun get(): T = registryObject.get()
    override fun asItem(): T = registryObject.get()
}

/**
 * Specialized form of ObjectHolderDelegate that contains a DeferredBlock instead of a DeferredHolder
 */
public class BlockHolderDelegate<T : Block>(public val registryObject: DeferredBlock<T>) : ReadOnlyProperty<Any?, T>, Supplier<T>, () -> T, ItemLike {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = registryObject.get()
    override fun invoke(): T = registryObject.get()
    override fun get(): T = registryObject.get()
    override fun asItem(): Item = registryObject.get().asItem()
}
