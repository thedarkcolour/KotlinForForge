package thedarkcolour.kotlinforforge

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import thedarkcolour.kotlinforforge.forge.MOD_BUS

/**
 * Set 'modLoader' in mods.toml to "kotlinforforge" and loaderVersion to "[1,)".
 *
 * Make sure to use [KotlinModLoadingContext] instead of [FMLJavaModLoadingContext].
 */
@Mod("kotlinforforge")
object KotlinForForge {
    init {
        MOD_BUS.addGenericListener(::registerBlocks)
    }

    private fun registerBlocks(event: RegistryEvent.Register<Block>) {
        event.registry.register(Block(Block.Properties.create(Material.ROCK)).setRegistryName("bruh"))
    }
}