package thedarkcolour.kotlinforforge

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.Logging
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation
import net.minecraftforge.forgespi.language.ModFileScanData
import org.objectweb.asm.Type
import thedarkcolour.kotlinforforge.forge.DIST
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.kotlin.enumSet

/**
 * Automatically registers `object` classes to
 * Kotlin for Forge's event buses.
 *
 * @see MOD_BUS
 * @see FORGE_BUS
 */
object AutoKotlinEventBusSubscriber {
    private val EVENT_BUS_SUBSCRIBER: Type = Type.getType(Mod.EventBusSubscriber::class.java)
    private val DIST_ENUM_HOLDERS = listOf(
            ModAnnotation.EnumHolder(null, "CLIENT"),
            ModAnnotation.EnumHolder(null, "DEDICATED_SERVER")
    )

    /**
     * Allows the Mod.EventBusSubscriber annotation
     * to target member functions of an `object` class.
     *
     * You **must** be using an `object` class, or the
     * EventBusSubscriber annotation will ignore it.
     *
     * Personally, I am against using [Mod.EventBusSubscriber]
     * because it makes
     *
     * @sample thedarkcolour.kotlinforforge.ExampleMod
     */
    fun inject(mod: ModContainer, scanData: ModFileScanData, classLoader: ClassLoader) {
        LOGGER.debug(Logging.LOADING, "Attempting to inject @EventBusSubscriber kotlin objects in to the event bus for ${mod.modId}")

        val data = scanData.annotations.filter { annotationData ->
            EVENT_BUS_SUBSCRIBER == annotationData.annotationType
        }

        data.forEach { annotationData ->
            val sidesValue = annotationData.annotationData.getOrDefault("value", DIST_ENUM_HOLDERS) as List<ModAnnotation.EnumHolder>
            val sides = enumSet<Dist>().plus(sidesValue.map { eh -> Dist.valueOf(eh.value) })
            val modid = annotationData.annotationData.getOrDefault("modid", mod.modId)
            val busTargetHolder = annotationData.annotationData.getOrDefault("bus", ModAnnotation.EnumHolder(null, "FORGE")) as ModAnnotation.EnumHolder
            val busTarget = Mod.EventBusSubscriber.Bus.valueOf(busTargetHolder.value)
            val ktObject = Class.forName(annotationData.classType.className, true, classLoader).kotlin.objectInstance

            if (ktObject != null && mod.modId == modid && DIST in sides) {
                try {
                    LOGGER.debug(Logging.LOADING, "Auto-subscribing kotlin object ${annotationData.classType.className} to $busTarget")

                    if (busTarget == Mod.EventBusSubscriber.Bus.MOD) {
                        MOD_BUS.register(ktObject)
                    } else {
                        FORGE_BUS.register(ktObject)
                    }
                } catch (e: Throwable) {
                    LOGGER.fatal(Logging.LOADING, "Failed to load mod class ${annotationData.classType} for @EventBusSubscriber annotation", e)
                    throw RuntimeException(e)
                }
            }
        }
    }
}