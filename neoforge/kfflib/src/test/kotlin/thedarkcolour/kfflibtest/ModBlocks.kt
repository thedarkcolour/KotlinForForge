package thedarkcolour.kfflibtest

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.util.function.Supplier

internal object ModBlocks {
    internal val REGISTRY = DeferredRegister.create(BuiltInRegistries.BLOCK, KFFLibTest.ID)

    internal val EXAMPLE_BLOCK by REGISTRY.register("example_block", Supplier {
        Block(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(4.0f))
    })
}
