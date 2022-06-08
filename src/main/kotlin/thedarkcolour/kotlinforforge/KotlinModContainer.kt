package thedarkcolour.kotlinforforge

import net.minecraftforge.eventbus.EventBus
import net.minecraftforge.eventbus.EventBusErrorMessage
import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.IEventListener
import net.minecraftforge.fml.Logging
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.ModLoadingException
import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.fml.event.IModBusEvent
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.ModFileScanData
import thedarkcolour.kotlinforforge.kotlin.supply
import java.util.*
import java.util.function.Consumer

/**
 * Kotlin mod container
 */
public class KotlinModContainer(
    info: IModInfo,
    className: String,
    private val scanResults: ModFileScanData,
    gameLayer: ModuleLayer,
) : ModContainer(info) {
    // Mod instance
    private var modInstance: Any? = null
    // Mod-specific event bus
    public val eventBus: IEventBus
    // Main mod class (moved out of constructMod)
    private val modClass: Class<*>

    init {
        LOGGER.debug(Logging.LOADING, "Creating KotlinModContainer instance for $className")

        activityMap[ModLoadingStage.CONSTRUCT] = Runnable(::constructMod)
        eventBus = EventBus(BusBuilder.builder().setExceptionHandler(::onEventFailed).setTrackPhases(false).markerType(IModBusEvent::class.java))
        
        configHandler = Optional.of(Consumer { event ->
            eventBus.post(event.self())
        })
        
        contextExtension = supply(KotlinModLoadingContext(this))
        
        try {
            val layer = gameLayer.findModule(info.owningFile.moduleName()).orElseThrow()
            modClass = Class.forName(layer, className)
            LOGGER.trace(Logging.LOADING, "Loaded modclass ${modClass.name} with ${modClass.classLoader}")
        } catch (t: Throwable) {
            LOGGER.error(Logging.LOADING, "Failed to load class $className", t)
            throw ModLoadingException(info, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmodclass", t)
        }
    }

    private fun onEventFailed(iEventBus: IEventBus, event: Event, listeners: Array<IEventListener>, busId: Int, throwable: Throwable) {
        LOGGER.error(EventBusErrorMessage(event, busId, listeners, throwable))
    }

    // Sets modInstance to a new instance of the mod class or the object instance
    private fun constructMod() {
        try {
            LOGGER.trace(Logging.LOADING, "Loading mod instance ${getModId()} of type ${modClass.name}")
            modInstance = modClass.kotlin.objectInstance ?: modClass.getDeclaredConstructor().newInstance()
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

    override fun matches(mod: Any?): Boolean {
        return mod == modInstance
    }

    override fun getMod(): Any? = modInstance

    public override fun <T> acceptEvent(e: T) where T : Event, T : IModBusEvent {
        try {
            LOGGER.debug("Firing event for modid $modId : $e")
            eventBus.post(e)
            LOGGER.debug("Fired event for modid $modId : $e")
        } catch (t: Throwable) {
            LOGGER.error("Caught exception during event $e dispatch for modid $modId", t)
            throw ModLoadingException(modInfo, modLoadingStage, "fml.modloading.errorduringevent", t)
        }
    }
}