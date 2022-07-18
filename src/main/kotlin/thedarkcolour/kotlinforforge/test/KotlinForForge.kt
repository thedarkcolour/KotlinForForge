package thedarkcolour.kotlinforforge.test

import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager

@Mod("kotlinforforge")
object KotlinForForge {
    private val LOGGER = LogManager.getLogger()
    init {
        LOGGER.info("Kotlin For Forge Enabled!")
    }
}