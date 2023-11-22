package thedarkcolour.kotlinforforge.neoforge.forge

import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredItem
import kotlin.reflect.KProperty

public operator fun <R, T : R> DeferredHolder<R, T>.getValue(any: Any?, property: KProperty<*>): T {
    return get()
}

public operator fun <T : Block> DeferredBlock<T>.getValue(any: Any?, property: KProperty<*>): T {
    return get()
}

public operator fun <T : Item> DeferredItem<T>.getValue(any: Any?, property: KProperty<*>): T {
    return get()
}