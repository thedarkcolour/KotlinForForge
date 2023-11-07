package thedarkcolour.kotlinforforge.neoforge

import net.neoforged.bus.EventBusErrorMessage
import net.neoforged.bus.api.BusBuilder
import net.neoforged.bus.api.Event
import net.neoforged.bus.api.EventListener
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.Logging
import net.neoforged.fml.ModContainer
import net.neoforged.fml.ModLoadingException
import net.neoforged.fml.ModLoadingStage
import net.neoforged.fml.event.IModBusEvent
import net.neoforged.fml.javafmlmod.FMLModContainer
import net.neoforged.neoforgespi.language.IModInfo
import net.neoforged.neoforgespi.language.ModFileScanData
import java.lang.RuntimeException
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

public class KotlinModContainer(
    info: IModInfo,
    className: String,
    private val scanResults: ModFileScanData,
    gameLayer: ModuleLayer,
) : ModContainer(info) {
    private var modInstance: Any? = null
    internal val eventBus: IEventBus
    private val modClass: Class<*>

    init {
        LOGGER.debug(Logging.LOADING, "Creating KotlinModContainer instance for $className")

        activityMap[ModLoadingStage.CONSTRUCT] = Runnable(::constructMod)
        eventBus = BusBuilder.builder().setExceptionHandler(::onEventFailed).markerType(IModBusEvent::class.java).build()
        configHandler = Optional.of(Consumer { event ->
            eventBus.post(event.self())
        })

        val ctx = KotlinModLoadingContext(this)
        contextExtension = Supplier {ctx}

        try {
            val layer = gameLayer.findModule(info.owningFile.moduleName()).orElseThrow()
            modClass = Class.forName(layer, className)
            LOGGER.trace(Logging.LOADING, "Loaded modclass ${modClass.name} with ${modClass.classLoader}")
        } catch (t: Throwable) {
            LOGGER.error(Logging.LOADING, "Failed to load class $className", t)
            throw ModLoadingException(info, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmodclass", t)
        }
    }

    override fun matches(mod: Any?): Boolean {
        return mod == modInstance
    }

    override fun getMod(): Any? = modInstance

    private fun onEventFailed(iEventBus: IEventBus, event: Event, listeners: Array<EventListener>, busId: Int, throwable: Throwable) {
        LOGGER.error(EventBusErrorMessage(event, busId, listeners, throwable))
    }

    private fun constructMod() {
        try {
            LOGGER.trace(Logging.LOADING, "Loading mod instance ${getModId()} of type ${modClass.name}")
            try {
                modInstance = modClass.kotlin.objectInstance ?: modClass.getDeclaredConstructor().newInstance()
            } catch (ignored: NoSuchMethodException) {
                // If empty constructor is not found, try constructor that accepts additional arguments
                val allowedConstructorArgs = mapOf(IEventBus::class.java to eventBus, ModContainer::class.java to this, FMLModContainer::class.java to this)

                constructors@ for (constructor in modClass.declaredConstructors) {
                    val paramTypes = constructor.parameterTypes
                    val paramValues = arrayOfNulls<Any>(paramTypes.size)
                    val foundArgs = hashSetOf<Class<*>>()
                    var i = -1

                    for (paramType in paramTypes) {
                        i++
                        val paramValue = allowedConstructorArgs[paramType] ?: continue@constructors

                        if (foundArgs.contains(paramTypes[i])) {
                            throw RuntimeException("Duplicate construcot argument type: $paramType")
                        }
                        foundArgs.add(paramType)
                        paramValues[i] = paramValue
                    }

                    modInstance = constructor.newInstance(paramValues)
                }

                if (modInstance == null) {
                    throw RuntimeException("Could not find mod constructor. Allowed optional argument classes: " + allowedConstructorArgs.keys.joinToString(transform = Class<*>::getSimpleName))
                }
            }
            LOGGER.trace(Logging.LOADING, "Loaded mod instance ${getModId()} of type ${modClass.name}")
        } catch (throwable: Throwable) {
            LOGGER.error(Logging.LOADING, "Failed to create mod instance. ModID: ${getModId()}, class ${modClass.name}", throwable)
            throw ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", throwable, modClass)
        }

        try {
            LOGGER.trace(Logging.LOADING, "Injecting Automatic Kotlin event subscribers for ${getModId()}")
            // Inject into object EventBusSubscribers
            AutoKotlinEventBusSubscriber.inject(this, scanResults, modClass.classLoader)
            LOGGER.trace(Logging.LOADING, "Completed Automatic Kotlin event subscribers for ${getModId()}")
        } catch (throwable: Throwable) {
            LOGGER.error(Logging.LOADING, "Failed to register Automatic Kotlin subscribers. ModID: ${getModId()}, class ${modClass.name}", throwable)
            throw ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", throwable, modClass)
        }
    }
}