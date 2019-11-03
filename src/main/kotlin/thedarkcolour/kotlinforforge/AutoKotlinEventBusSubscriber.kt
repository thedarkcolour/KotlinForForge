package thedarkcolour.kotlinforforge

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.Logging
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation
import net.minecraftforge.forgespi.language.ModFileScanData
import org.objectweb.asm.Type
import thedarkcolour.kotlinforforge.KotlinForForge.logger
import java.util.*
import java.util.stream.Collectors
import kotlin.reflect.full.companionObjectInstance

/**
 * Handles [net.minecraftforge.fml.common.Mod.EventBusSubscriber]
 */
@Suppress("unused")
object AutoKotlinEventBusSubscriber {
    private val EVENT_BUS_SUBSCRIBER: Type = Type.getType(Mod.EventBusSubscriber::class.java)

    /**
     * Registers Kotlin objects that are annotated with [Mod.EventBusSubscriber]
     * This allows you to declare an object that subscribes to the event bus
     * without making all the [net.minecraftforge.eventbus.api.SubscribeEvent] annotated with [JvmStatic]
     *
     * Example Usage:
     *
     *   @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
     *   public object ExampleSubscriber {
     *      @SubscribeEvent
     *      public fun onItemRegistry(event: RegistryEvent.Register<Item>) {
     *         println("Look! We're in items :)")
     *      }
     *   }
     *
     * This also works with companion objects.
     * You must define the [net.minecraftforge.eventbus.api.SubscribeEvent] methods inside the companion object.
     * Example Usage:
     *
     *   @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
     *   public class ExampleSubscriberClass {
     *
     *      companion object ExampleSubscriberCompanion {
     *         @SubscribeEvent
     *         public fun onItemRegistry(event: RegistryEvent.Register<Item>) {
     *            println("Look! We're in items :)")
     *         }
     *      }
     *   }
     */
    @Suppress("UNCHECKED_CAST", "unused")
    fun inject(mod: ModContainer, scanData: ModFileScanData, classLoader: ClassLoader) {
        logger.debug(Logging.LOADING, "Attempting to inject @EventBusSubscriber kotlin objects in to the event bus for {}", mod.modId)
        val data: ArrayList<ModFileScanData.AnnotationData> = scanData.annotations.stream()
                .filter { annotationData ->
                    EVENT_BUS_SUBSCRIBER == annotationData.annotationType
                }
                .collect(Collectors.toList()) as ArrayList<ModFileScanData.AnnotationData>
        data.forEach { annotationData ->
            val sidesValue: List<ModAnnotation.EnumHolder> = annotationData.annotationData.getOrDefault("value", listOf(ModAnnotation.EnumHolder(null, "CLIENT"), ModAnnotation.EnumHolder(null, "DEDICATED_SERVER"))) as List<ModAnnotation.EnumHolder>
            val sides: EnumSet<Dist> = sidesValue.stream().map { eh -> Dist.valueOf(eh.value) }
                    .collect(Collectors.toCollection { EnumSet.noneOf(Dist::class.java) })
            val modid = annotationData.annotationData.getOrDefault("modid", mod.modId)
            val busTargetHolder: ModAnnotation.EnumHolder = annotationData.annotationData.getOrDefault("bus", ModAnnotation.EnumHolder(null, "FORGE")) as ModAnnotation.EnumHolder
            val busTarget = Mod.EventBusSubscriber.Bus.valueOf(busTargetHolder.value)
            val clazz = Class.forName(annotationData.classType.className, true, classLoader)
            val ktClass = clazz.kotlin
            val ktObject = ktClass.objectInstance
            if (ktObject != null && mod.modId == modid && sides.contains(FMLEnvironment.dist)) {
                try {
                    logger.debug(Logging.LOADING, "Auto-subscribing kotlin object {} to {}", annotationData.classType.className, busTarget)
                    busTarget.bus().get().register(ktObject)
                } catch (e: Throwable) {
                    logger.fatal(Logging.LOADING, "Failed to load mod class {} for @EventBusSubscriber annotation", annotationData.classType, e)
                    throw RuntimeException(e)
                }
            } else if (ktClass.companionObjectInstance != null)  {
                try {
                    logger.debug(Logging.LOADING, "Auto-subscribing kotlin companion object from {} to {}", ktClass.simpleName, busTarget)
                    busTarget.bus().get().register(ktClass.companionObjectInstance)
                } catch (e: Throwable) {
                    logger.fatal(Logging.LOADING, "Failed to load kotlin companion object {} for @EventBusSubscriber annotation", annotationData.classType, e)
                    throw RuntimeException(e)
                }
            }
        }
    }
}