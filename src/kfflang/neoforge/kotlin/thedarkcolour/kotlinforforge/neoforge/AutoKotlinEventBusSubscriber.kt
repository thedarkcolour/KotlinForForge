package thedarkcolour.kotlinforforge.neoforge

import net.neoforged.bus.SubscribeEventListener
import net.neoforged.bus.api.Event
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.Logging
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.IModBusEvent
import net.neoforged.fml.javafmlmod.AutomaticEventSubscriber
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.fml.loading.FMLLoader
import net.neoforged.neoforgespi.language.ModFileScanData
import org.objectweb.asm.Type
import java.lang.reflect.Method
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaMethod

/**
 * Automatically registers `object` classes to
 * Kotlin for Forge's event buses.
 *
 * This also allows [EventBusSubscriber] to be used as a file-wide annotation,
 * registering any top-level functions annotated with @SubscribeEvent to the event bus.
 *
 * Example:
 * ```
 * @file:Mod.EventBusSubscriber
 *
 * package example
 *
 * @SubscribeEvent
 * fun onCommonSetup(event: FMLCommonSetupEvent) {
 *   // registered to mod event bus
 * }
 * ```
 */
public object AutoKotlinEventBusSubscriber {
    // EventBusSubscriber annotation
    private val EVENT_BUS_SUBSCRIBER: Type = Type.getType(EventBusSubscriber::class.java)
    private val MOD: Type = Type.getType(Mod::class.java)

    private const val MOD_BUS_TARGET = "MOD"
    private const val GAME_BUS_TARGET = "GAME"

    private val gameBus by lazy {
        try {
            getOldGameBus()
        } catch (t: Throwable) {
            getNewGameBus()
        }
    }

    private fun getOldGameBus(): IEventBus {
        val bindings = Class.forName("net.neoforged.fml.Bindings")
        return bindings.getDeclaredMethod("getGameBus").invoke(null) as IEventBus
    }

    private fun getNewGameBus(): IEventBus {
        @Suppress("UnstableApiUsage")
        return FMLLoader.getCurrent().bindings.gameBus
    }

    /**
     * Allows the [EventBusSubscriber] annotation
     * to target member functions of an `object` class.
     *
     * You **must** be using an `object` class, or the
     * `Mod.EventBusSubscriber` annotation will ignore it.
     *
     * I am against using `Mod.EventBusSubscriber`
     * because it makes it difficult to follow where event
     * listeners are registered. Instead, prefer to directly
     * register event listeners to the forge bus or the mod-specific bus.
     */
    public fun inject(mod: KotlinModContainer, scanData: ModFileScanData?, layer: Module) {
        if (scanData == null) return
        LOGGER.debug(Logging.LOADING, "Attempting to inject @EventBusSubscriber kotlin objects in to the event bus for ${mod.modId}")

        val ebsTargets = scanData.annotations.filter { annotationData ->
            EVENT_BUS_SUBSCRIBER == annotationData.annotationType
        }
        val modIds = scanData.annotations.filter { annotationData ->
            MOD == annotationData.annotationType
        }.associate { annotationData ->
            annotationData.clazz.className to annotationData.annotationData.get("value")
        }

        val currentDist = FMLEnvironment.getDist()

        // we only need to worry about cases where NeoForge can't automatically register (object and file)
        for (annotationData in ebsTargets) {
            val sides = AutomaticEventSubscriber.getSides(annotationData.annotationData.get("value"))
            val className = annotationData.clazz.className
            val modid = annotationData.annotationData.getOrDefault("modid", modIds.getOrDefault(className, mod.modId))

            if (mod.modId == modid && currentDist in sides) {
                val kClass = Class.forName(annotationData.clazz.className, true, layer.classLoader).kotlin

                var ktObject: Any?

                try {
                    ktObject = kClass.objectInstance
                } catch (unsupported: UnsupportedOperationException) {
                    if (unsupported.message?.contains("file facades") == false) {
                        throw unsupported
                    }
                    ktObject = null
                }

                if (ktObject != null) {
                    try {
                        val gameListeners = arrayListOf<Method>()
                        val modListeners = arrayListOf<Method>()

                        for (function in kClass.declaredMemberFunctions) {
                            if (!function.hasAnnotation<SubscribeEvent>()) continue

                            val method = function.javaMethod!!
                            val paramTypes = method.parameterTypes
                            if (method.parameterCount != 1 || !Event::class.java.isAssignableFrom(paramTypes[0])) {
                                throw IllegalArgumentException("Kotlin function $method annotated with @SubscribeEvent must have only one parameter that is an Event subtype")
                            }

                            val eventType = paramTypes[0]
                            if (IModBusEvent::class.java.isAssignableFrom(eventType)) {
                                modListeners.add(method)
                            } else {
                                gameListeners.add(method)
                            }
                        }

                        // Preserve old behavior when there's no mix, allowing the entire object to be unregistered like before
                        if (modListeners.isEmpty()) {
                            LOGGER.debug(Logging.LOADING, "Auto-subscribing kotlin object {} to $GAME_BUS_TARGET", ktObject)
                            gameBus.register(ktObject)
                        } else {
                            if (gameListeners.isEmpty()) {
                                LOGGER.debug(Logging.LOADING, "Auto-subscribing kotlin object {} to $MOD_BUS_TARGET", ktObject)
                                mod.eventBus.register(ktObject)
                            } else {
                                // New behavior that automatically detects which bus to register to
                                for (method in modListeners) {
                                    LOGGER.debug(Logging.LOADING, "Subscribing kotlin function {} to the $MOD_BUS_TARGET event bus", method)
                                    mod.eventBus.registerMemberMethod(ktObject, method)
                                }
                                for (method in gameListeners) {
                                    LOGGER.debug(Logging.LOADING, "Subscribing kotlin function {} to the $GAME_BUS_TARGET event bus", method)
                                    gameBus.registerMemberMethod(ktObject, method)
                                }
                            }
                        }
                    } catch (e: Throwable) {
                        LOGGER.fatal(Logging.LOADING, "Failed to load mod class ${annotationData.annotationType} for @EventBusSubscriber annotation", e)
                        throw RuntimeException(e)
                    }
                } else {
                    LOGGER.debug(Logging.LOADING, "Passing kotlin file {} to NeoForge's AutomaticEventSubscriber", annotationData.annotationType.className)
                    // Make NeoForge do it
                    val fakeData = ModFileScanData()
                    fakeData.annotations.add(annotationData)
                    AutomaticEventSubscriber.inject(mod, fakeData, layer)
                }
            }
        }
    }

    private fun IEventBus.registerMemberMethod(obj: Any, method: Method) {
        val subscribeEvent = method.getAnnotation(SubscribeEvent::class.java)
        // let's hope this doesn't break
        @Suppress("UnstableApiUsage")
        val eventListener = SubscribeEventListener(obj, method).withoutCheck

        @Suppress("UNCHECKED_CAST")
        addListener(subscribeEvent.priority, subscribeEvent.receiveCanceled, method.parameterTypes[0] as Class<Event>, eventListener::invoke)
    }
}
