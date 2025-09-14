package thedarkcolour.kfflibtest

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestAssertException
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.gametest.GameTestHolder
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate

@GameTestHolder(KFFLibTest.ID)
public object InGameTester {
    @PrefixGameTestTemplate(false)
    @GameTest(template = "dummy")
    @JvmStatic
    public fun testBlock(helper: GameTestHelper) {
        helper.succeedIf {
            if (!BuiltInRegistries.BLOCK.containsKey(ResourceLocation(KFFLibTest.ID, "example_block"))) {
                throw GameTestAssertException("Block is not registered correctly!")
            }
        }
    }
}
