package thedarkcolour.kfflibtest

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT

/**
 * Set `modLoader` in mods.toml to
 * `"kotlinforforge"` and loaderVersion to `"[3,)"`.
 *
 * Make sure to use [MOD_CONTEXT]
 * instead of [FMLJavaModLoadingContext].
 *
 * For a more detailed example mod,
 * check out the [KotlinModdingSkeleton repository](https://github.com/thedarkcolour/KotlinModdingSkeleton).
 */
@Mod(KFFLibTest.ID)
object KFFLibTest {
    const val ID = "kfflibtest"

    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        LOGGER.log(Level.INFO, "Hello world!")

        ModBlocks.REGISTRY.register(MOD_BUS)
    }
}