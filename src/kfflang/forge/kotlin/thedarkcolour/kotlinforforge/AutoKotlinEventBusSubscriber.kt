package thedarkcolour.kotlinforforge

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.eventbus.api.bus.BusGroup
import net.minecraftforge.eventbus.api.bus.CancellableEventBus
import net.minecraftforge.eventbus.api.bus.EventBus
import net.minecraftforge.eventbus.api.event.characteristic.Cancellable
import net.minecraftforge.eventbus.api.listener.EventListener
import net.minecraftforge.eventbus.api.listener.ObjBooleanBiConsumer
import net.minecraftforge.eventbus.api.listener.Priority
import net.minecraftforge.eventbus.api.listener.SubscribeEvent
import net.minecraftforge.eventbus.internal.Event
import net.minecraftforge.fml.Logging
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.IModBusEvent
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.forgespi.language.ModFileScanData.EnumData
import net.minecraftforge.unsafe.UnsafeHacks
import org.objectweb.asm.Type
import thedarkcolour.common.KotlinBus
import thedarkcolour.common.KotlinMod
import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation

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
    private val EVENT_BUS_SUBSCRIBER: Type = Type.getType(KotlinMod.KotlinEventBusSubscriber::class.java)
    private val MOD_TYPE: Type = Type.getType(KotlinMod::class.java)
    private val ONLY_IN_TYPE: Type = Type.getType(OnlyIn::class.java)

    /**
     * Allows the [KotlinMod.KotlinEventBusSubscriber] annotation
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
        LOGGER.debug(Logging.LOADING, "Attempting to inject @KotlinEventBusSubscriber kotlin objects in to the event bus for ${mod.modId}")

        val targets = scanData.annotations.filter { data -> EVENT_BUS_SUBSCRIBER == data.annotationType }.toList()

        val onlyIns: Set<String> = if (FMLEnvironment.production) {
            emptySet()
        } else {
            scanData.annotations
                .asSequence()
                .filter { it.annotationType() == ONLY_IN_TYPE }
                .map { it.clazz().className }
                .toSet()
        }

        val modids: Map<String, String> = scanData.annotations
            .asSequence()
            .filter { it.annotationType() == MOD_TYPE }
            .associate { it.clazz().className to (it.annotationData()["value"] as String) }

        val defaultSides = listOf(EnumData(null, "CLIENT"), EnumData(null, "DEDICATED_SERVER"))
        val defaultBus = EnumData(null, "FORGE")

        for (annotationData in targets) {
            if (!FMLEnvironment.production && onlyIns.contains(annotationData.clazz.className)) {
                throw RuntimeException("Found @OnlyIn on @KotlinEventBusSubscriber class ${annotationData.clazz().className} - this is not allowed as it causes crashes. Remove the OnlyIn and set value=Dist.CLIENT in the EventBusSubscriber annotation instead")
            }

            var modId = modids.getOrDefault(annotationData.clazz.className, mod.modId)
            modId = value(annotationData, "modId", modId)

            val sidesValue = value(annotationData, "value", defaultSides)
            val sides = sidesValue
                .map(EnumData::value)
                .map(Dist::valueOf)
                .toSet()
            val busName = value(annotationData, "bus", defaultBus).value()
            val busTarget = KotlinBus.valueOf(busName)
            if (mod.modId == modId && FMLEnvironment.dist in sides) {
                try {
                    LOGGER.debug(LOADING, "Auto-subscribing {} to {}", annotationData.clazz().className, busTarget)
                    val clazz = Class.forName(annotationData.clazz.className, true, classLoader)
                    val instance = try {
                        val field = clazz.getDeclaredField("INSTANCE")
                        field.isAccessible = true
                        field.get(null)
                    } catch (e: Exception) {
                        LOGGER.warn(LOADING, "⚠️ Failed to get INSTANCE from ${clazz.name}: ${e.message}")
                        null
                    }

                    if (instance != null) {
                        EventBusSubscriberLogic.register(busTarget.bus().get(), instance)
                    } else {
                        LOGGER.warn(LOADING, "⚠️ Could not register ${clazz.name}, instance was null")
                    }
                } catch (e: ClassNotFoundException){
                    LOGGER.fatal(LOADING, "Failed to load mod class {} for @EventBusSubscriber annotation", annotationData.clazz(), e)
                    throw RuntimeException(e)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <R> value(data: ModFileScanData.AnnotationData, key: String, default: R): R {
        return data.annotationData.getOrDefault(key, default) as R
    }

    private object EventBusSubscriberLogic {
        private val STRICT_RUNTIME_CHECKS = java.lang.Boolean.getBoolean("eventbus.api.strictRuntimeChecks")
        private val STRICT_REGISTRATION_CHECKS =
            STRICT_RUNTIME_CHECKS || java.lang.Boolean.getBoolean("eventbus.api.strictRegistrationChecks")

        private val RETURNS_CONSUMER = MethodType.methodType(Consumer::class.java)
        private val RETURNS_PREDICATE = MethodType.methodType(Predicate::class.java)
        private val RETURNS_MONITOR = MethodType.methodType(ObjBooleanBiConsumer::class.java)

        private val CONSUMER_FI_TYPE = MethodType.methodType(Void.TYPE, Any::class.java)
        private val PREDICATE_FI_TYPE = CONSUMER_FI_TYPE.changeReturnType(Boolean::class.javaPrimitiveType)
        private val MONITOR_FI_TYPE = MethodType.methodType(Void.TYPE, Any::class.java, Boolean::class.javaPrimitiveType)

        @JvmStatic
        fun register(busGroup: BusGroup?, listenerClass: Any) {
            if (STRICT_REGISTRATION_CHECKS) registerStrict(busGroup, listenerClass)
            else registerLenient(busGroup, listenerClass)
        }

        fun registerLenient(busGroup: BusGroup?, listenerObject: Any) {
            val kClass = listenerObject::class
            val functions = kClass.declaredFunctions
            var listeners = 0

            LOGGER.debug("🔍 Inspecting Kotlin class: ${kClass.qualifiedName}")
            for (function in functions) {
                LOGGER.debug("→ Function: ${function.name}")

                val annotation = function.findAnnotation<SubscribeEvent>()
                if (annotation == null) {
                    LOGGER.debug("   ⛔ Skipped: No @SubscribeEvent")
                    continue
                } else {
                    LOGGER.debug("   ✅ Found @SubscribeEvent")
                }

                val params = function.parameters
                LOGGER.debug("   🧩 Params count: ${params.size}")
                LOGGER.debug("   🧩 Params: ${params.joinToString { it.type.toString() }}")
                LOGGER.debug("   🧩 Return type: ${function.returnType}")

                if (params.size !in 2..3) {
                    LOGGER.warn("   ⛔ Skipped: Unexpected parameter count: ${params.size}")
                    continue
                }

                val classifier = params[1].type.classifier
                val eventParamKClass = classifier as? KClass<*>
                if (eventParamKClass == null) {
                    LOGGER.warn("   ⛔ Skipped: Param[1] classifier is not a KClass: $classifier")
                    continue
                }

                val eventParam = try {
                    eventParamKClass.java
                } catch (e: Exception) {
                    LOGGER.warn("   ⛔ Skipped: Failed to get Java class for event param: ${e.message}")
                    continue
                }

                LOGGER.debug("   📦 Event param class: ${eventParam.name}")

                if (!Event::class.java.isAssignableFrom(eventParam)) {
                    LOGGER.warn("   ⛔ Skipped: ${eventParam.name} is not a subtype of Event")
                    continue
                }

                val cancellable = Cancellable::class.java.isAssignableFrom(eventParam)
                val eventClass = eventParam as Class<out Event>
                val bus = busGroup ?: if (IModBusEvent::class.java.isAssignableFrom(eventClass))
                    KotlinModLoadingContext.get().getKBusGroup()
                else BusGroup.DEFAULT

                val priority = annotation.priority

                try {
                    when (params.size) {
                        2 -> {
                            when (function.returnType.classifier) {
                                Unit::class -> {
                                    if (cancellable) {
                                        val busC = CancellableEventBus.create(bus, castToCancellableEvent(eventClass))
                                        if (annotation.alwaysCancelling)
                                            busC.addListener(priority, true) { function.call(listenerObject, it) }
                                        else
                                            busC.addListener(priority, Consumer { function.call(listenerObject, it) })
                                    } else {
                                        EventBus.create(bus, eventClass)
                                            .addListener(priority) { function.call(listenerObject, it) }
                                    }
                                }

                                Boolean::class -> {
                                    if (!cancellable) error("Boolean return type only valid on cancellable events")
                                    if (annotation.alwaysCancelling) error("Can't use alwaysCancelling with boolean return")

                                    val busC = CancellableEventBus.create(bus, castToCancellableEvent(eventClass))
                                    busC.addListener(priority, Predicate {
                                        function.call(listenerObject, it) as Boolean
                                    })
                                }

                                else -> {
                                    LOGGER.warn("   ⛔ Skipped: Unsupported return type: ${function.returnType}")
                                    return
                                }
                            }
                        }

                        3 -> {
                            if (!cancellable) error("Two params only valid for cancellable events")
                            if (function.returnType.classifier != Unit::class) error("Third param requires void return")
                            if (params[2].type.classifier != Boolean::class) error("Third param must be boolean")
                            if (annotation.priority != Priority.MONITOR) error("Third param only allowed on MONITOR")

                            val busC = CancellableEventBus.create(bus, castToCancellableEvent(eventClass))
                            busC.addListener(ObjBooleanBiConsumer { event, cancelled ->
                                function.call(listenerObject, event, cancelled)
                            })
                        }

                        else -> {
                            LOGGER.warn("   ⛔ Skipped: Invalid param count ${params.size}")
                            return
                        }
                    }

                    LOGGER.info("✅ Registered: ${function.name} for ${eventClass.simpleName}")
                    listeners++

                } catch (e: Exception) {
                    LOGGER.error("💥 Error registering function ${function.name}: ${e.message}", e)
                }
            }

            if (listeners == 0) {
                LOGGER.error("❌ No valid listeners found in ${kClass.qualifiedName}")
                error("❌ No valid listeners found in ${kClass.qualifiedName}")
            }
        }


        fun registerStrict(busGroup: BusGroup?, listenerObject: Any) {
            val kClass = listenerObject::class
            val functions = kClass.declaredFunctions.filter { fn ->
                // Filter out compiler-generated/internal stuff
                fn.name !in setOf("equals", "hashCode", "toString") &&
                        !fn.name.contains("\$") && // filters accessors/lambdas/inline$ methods
                        fn.visibility == KVisibility.PUBLIC
            }

            if (functions.isEmpty()) {
                val superClass = listenerObject::class.java.superclass
                var msg = "No declared methods found in ${kClass.qualifiedName}"
                if (superClass != null && superClass != Record::class.java && superClass != Enum::class.java) {
                    msg += ". Listener inheritance isn't supported. Use @Override + @SubscribeEvent on subclass."
                }
                error(msg)
            }

            var listeners = 0

            for (func in functions) {
                val annotation = func.findAnnotation<SubscribeEvent>() ?: continue
                val params = func.parameters
                val returnType = func.returnType
                val isMonitor = annotation.priority == Priority.MONITOR

                // expect instance + 1 or 2 parameters (event, [boolean])
                if (params.size !in 2..3) {
                    error("Invalid parameter count on ${func.name}: ${params.size - 1}")
                }

                val eventParamClass = params[1].type.classifier as? Class<*> ?: continue
                if (!Event::class.java.isAssignableFrom(eventParamClass)) {
                    error("First parameter of ${func.name} must be Event")
                }

                val cancellable = Cancellable::class.java.isAssignableFrom(eventParamClass)
                val eventClass = eventParamClass as Class<out Event>
                val bus = busGroup ?: if (net.minecraftforge.fml.event.IModBusEvent::class.java.isAssignableFrom(eventClass))
                    KotlinModLoadingContext.get().getKBusGroup()
                else BusGroup.DEFAULT

                // Validate return type
                if (returnType.classifier !in listOf(Void.TYPE.kotlin, Boolean::class)) {
                    error("Invalid return type on ${func.name}: $returnType")
                }

                // Two-parameter (event, cancelled) validation
                if (params.size == 3) {
                    if (!cancellable) error("Second param only valid for cancellable events in ${func.name}")
                    if (params[2].type.classifier != Boolean::class) error("Second param must be Boolean in ${func.name}")
                    if (!isMonitor) error("Second param only valid for MONITOR priority in ${func.name}")
                    if (returnType.classifier != Void.TYPE.kotlin) error("Cancellation-monitoring listener must return void in ${func.name}")
                }

                // Cancel checks
                if (!cancellable) {
                    if (annotation.alwaysCancelling) error("alwaysCancelling only valid on cancellable events in ${func.name}")
                    if (returnType.classifier == Boolean::class) error("boolean return type only valid on cancellable events in ${func.name}")
                }

                // REGISTER
                when (params.size) {
                    2 -> {
                        val priority = annotation.priority
                        if (returnType.classifier == Void.TYPE.kotlin) {
                            if (cancellable) {
                                val busC = CancellableEventBus.create(bus, castToCancellableEvent(eventClass))
                                if (annotation.alwaysCancelling)
                                    busC.addListener(priority, true, Consumer { func.call(listenerObject, it) })
                                else
                                    busC.addListener(priority, Consumer { func.call(listenerObject, it) })
                            } else {
                                EventBus.create(bus, eventClass).addListener(priority, Consumer { func.call(listenerObject, it) })
                            }
                        } else {
                            if (!cancellable) error("Boolean return type only valid on cancellable events in ${func.name}")
                            if (annotation.alwaysCancelling) error("Cannot combine boolean return type with alwaysCancelling in ${func.name}")
                            val busC = CancellableEventBus.create(bus, castToCancellableEvent(eventClass))
                            busC.addListener(priority, Predicate { func.call(listenerObject, it) as Boolean })
                        }
                    }

                    3 -> {
                        val busC = CancellableEventBus.create(bus, castToCancellableEvent(eventClass))
                        busC.addListener(ObjBooleanBiConsumer { event, cancelled ->
                            func.call(listenerObject, event, cancelled)
                        })
                    }
                }

                println("✅ Strictly registered: ${func.name} for ${eventClass.simpleName}")
                listeners++
            }

            if (listeners == 0) error("❌ No valid listeners found in ${kClass.qualifiedName}")
        }

        @Suppress("UNCHECKED_CAST")
        fun <T> castToCancellableEvent(eventType: Class<*>): Class<T>
                where T : Event, T : Cancellable {
            return eventType as Class<T>
        }
    }
}
