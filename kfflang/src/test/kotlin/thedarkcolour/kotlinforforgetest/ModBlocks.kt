package thedarkcolour.kotlinforforgetest

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.Material
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import thedarkcolour.kotlinforforge.forge.registerObject

object ModBlocks {
    val REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, KotlinForForgeTest.ID)

    val EXAMPLE_BLOCK by REGISTRY.registerObject("example_block") {
        Block(BlockBehaviour.Properties.of(Material.LEAVES).strength(4.0f))
    }
}