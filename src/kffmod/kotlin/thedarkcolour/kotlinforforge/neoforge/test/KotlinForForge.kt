package thedarkcolour.kotlinforforge.neoforge.test

import net.neoforged.fml.common.Mod
import org.apache.logging.log4j.LogManager

@Mod("kotlinforforge")
public object KotlinForForge {
    private val LOGGER = LogManager.getLogger()
    init {
        LOGGER.info("Kotlin For Forge Enabled!")
    }
}