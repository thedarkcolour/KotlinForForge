package thedarkcolour.kfflibtest

import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestAssertException
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.gametest.GameTestHolder
import net.minecraftforge.gametest.PrefixGameTestTemplate
import net.minecraftforge.registries.ForgeRegistries

@GameTestHolder(KFFLibTest.ID)
public object InGameTester {
    @PrefixGameTestTemplate(false)
    @GameTest(template = "dummy")
    @JvmStatic
    public fun testBlock(helper: GameTestHelper) {
        helper.succeedIf {
            if (!ForgeRegistries.BLOCKS.containsKey(ResourceLocation(KFFLibTest.ID, "example_block"))) {
                throw GameTestAssertException("Block is not registered correctly!")
            }
        }
    }
}