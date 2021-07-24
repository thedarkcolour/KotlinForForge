package thedarkcolour.kotlinforforge

import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.Material
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.registries.ForgeRegistries
import thedarkcolour.kotlinforforge.forge.KDeferredRegister
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT

/**
 * Set `modLoader` in mods.toml to
 * `"kotlinforforge"` and loaderVersion to `"[2.0,)"`.
 *
 * Make sure to use [MOD_CONTEXT]
 * instead of [FMLJavaModLoadingContext].
 *
 * For a more detailed example mod,
 * check out the [KotlinModdingSkeleton repository](https://github.com/thedarkcolour/KotlinModdingSkeleton).
 */
@Mod("kotlinforforge")
public class KotlinForForge {
    init {
        val blocks = KDeferredRegister(ForgeRegistries.BLOCKS, "kotlinforforge")
        val items = KDeferredRegister(ForgeRegistries.ITEMS, "kotlinforforge")
        blocks.register(FMLJavaModLoadingContext.get().modEventBus)
        items.register(FMLJavaModLoadingContext.get().modEventBus)

        val red by blocks.register("red_block") { Block(BlockBehaviour.Properties.of(Material.BAMBOO)) }
        val item = items.register("red_item") { BlockItem(red, Item.Properties()) }
    }
}