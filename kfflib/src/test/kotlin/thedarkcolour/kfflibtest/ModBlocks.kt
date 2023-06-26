package thedarkcolour.kfflibtest

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import thedarkcolour.kotlinforforge.forge.registerObject

internal object ModBlocks {
    internal val REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, KFFLibTest.ID)

    internal val EXAMPLE_BLOCK by REGISTRY.registerObject("example_block") {
        Block(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(4.0f))
    }
}