package thedarkcolour.kotlinforforge.test

import org.apache.logging.log4j.LogManager
import thedarkcolour.common.KotlinMod
import thedarkcolour.kotlinforforge.KotlinModLoadingContext
import java.lang.annotation.ElementType

@KotlinMod("kotlinforforge")
public class KotlinForForge(context: KotlinModLoadingContext) {
    private val LOGGER = LogManager.getLogger()
    init {
        LOGGER.info("Kotlin For Forge Enabled!")
    }
}