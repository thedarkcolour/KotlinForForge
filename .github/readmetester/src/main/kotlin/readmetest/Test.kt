package readmetest

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent
import net.minecraftforge.server.ServerLifecycleHooks
import org.apache.logging.log4j.LogManager
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(Test.MODID)
object Test {
    const val MODID = "readmetester"
    val LOGGER = LogManager.getLogger()

    init {
        LOGGER.info("Launch succeed")
        System.exit(0)
    }
}