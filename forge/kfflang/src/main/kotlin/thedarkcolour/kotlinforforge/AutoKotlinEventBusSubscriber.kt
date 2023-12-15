package thedarkcolour.kotlinforforge

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.Logging
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.forgespi.language.ModFileScanData.EnumData
import org.objectweb.asm.Type
import java.lang.reflect.Method
import java.util.*

/**
 * Automatically registers `object` classes to
 * Kotlin for Forge's event buses.
 *
 * This also allows [Mod.EventBusSubscriber] to be used as a file-wide annotation,
 * registering any top-level functions annotated with @SubscribeEvent to the event bus.
 *
 * Example:
 * ```
 * @file:Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
 *
 * package example
 *
 * @SubscribeEvent
 * fun onCommonSetup(event: FMLCommonSetupEvent) {
 *   // registered to mod event bus
 * }
 * ```
 *
 * @see thedarkcolour.kotlinforforge.forge.MOD_BUS
 * @see thedarkcolour.kotlinforforge.forge.FORGE_BUS
 */
public object AutoKotlinEventBusSubscriber {
    // EventBusSubscriber annotation
    private val EVENT_BUS_SUBSCRIBER: Type = Type.getType(Mod.EventBusSubscriber::class.java)

    // Legacy EnumHolder
    private val enumHolderGetValue: Method? = try {
        val klass = Class.forName("net.minecraftforge.fml.loading.moddiscovery.ModAnnotation\$EnumHolder")
        klass.getDeclaredMethod("getValue")
    } catch (e: ClassNotFoundException) {
        null
    }

    /**
     * Allows the [Mod.EventBusSubscriber] annotation
     * to target member functions of an `object` class.
     *
     * You **must** be using an `object` class, or the
     * `Mod.EventBusSubscriber` annotation will ignore it.
     *
     * I am against using `Mod.EventBusSubscriber`
     * because it makes it difficult to follow where event
     * listeners are registered. Instead, prefer to directly
     * register event listeners to
     * [thedarkcolour.kotlinforforge.forge.FORGE_BUS]
     * or [thedarkcolour.kotlinforforge.forge.MOD_BUS].
     */
    public fun inject(mod: KotlinModContainer, scanData: ModFileScanData, classLoader: ClassLoader) {
        LOGGER.debug(Logging.LOADING, "Attempting to inject @EventBusSubscriber kotlin objects in to the event bus for ${mod.modId}")

        val data = scanData.annotations.filter { annotationData ->
            EVENT_BUS_SUBSCRIBER == annotationData.annotationType
        }

        for (annotationData in data) {
            val annotationMap = annotationData.annotationData
            val sides = getSides(annotationMap)
            val modid = annotationMap.getOrDefault("modid", mod.modId)
            val busTarget = getBusTarget(annotationMap)

            if (mod.modId == modid && FMLEnvironment.dist in sides) {
                val kClass = Class.forName(annotationData.clazz.className, true, classLoader).kotlin

                var ktObject: Any?

                try {
                    ktObject = kClass.objectInstance
                } catch (unsupported: UnsupportedOperationException) {
                    if (unsupported.message?.contains("file facades") == false) {
                        throw unsupported
                    } else {
                        LOGGER.debug(Logging.LOADING, "Auto-subscribing kotlin file ${annotationData.annotationType.className} to $busTarget")
                        registerTo(kClass.java, busTarget, mod)
                        continue
                    }
                }

                if (ktObject != null) {
                    try {
                        LOGGER.debug(Logging.LOADING, "Auto-subscribing kotlin object ${annotationData.annotationType.className} to $busTarget")

                        registerTo(ktObject, busTarget, mod)
                    } catch (e: Throwable) {
                        LOGGER.fatal(Logging.LOADING, "Failed to load mod class ${annotationData.annotationType} for @EventBusSubscriber annotation", e)
                        throw RuntimeException(e)
                    }
                }
            }
        }
    }

    private fun getSides(annotationMap: Map<String, Any>): List<Dist> {
        val sidesHolders = annotationMap["value"]

        return if (sidesHolders != null) {
            if (enumHolderGetValue != null) {
                (sidesHolders as List<Any>).map { data -> Dist.valueOf(enumHolderGetValue.invoke(data) as String) }
            } else {
                (sidesHolders as List<EnumData>).map { data -> Dist.valueOf(data.value()) }
            }
        } else {
            listOf(Dist.CLIENT, Dist.DEDICATED_SERVER)
        }
    }

    private fun getBusTarget(annotationMap: Map<String, Any>): Mod.EventBusSubscriber.Bus {
        val busTargetHolder = annotationMap["bus"]

        return if (busTargetHolder != null) {
            if (enumHolderGetValue != null) {
                Mod.EventBusSubscriber.Bus.valueOf(enumHolderGetValue.invoke(busTargetHolder) as String)
            } else {
                Mod.EventBusSubscriber.Bus.valueOf((busTargetHolder as EnumData).value)
            }
        } else {
            Mod.EventBusSubscriber.Bus.FORGE
        }
    }

    private fun registerTo(any: Any, target: Mod.EventBusSubscriber.Bus, mod: KotlinModContainer) {
        if (target == Mod.EventBusSubscriber.Bus.FORGE) {
            target.bus().get().register(any)
        } else {
            mod.eventBus.register(any)
        }
    }
}
