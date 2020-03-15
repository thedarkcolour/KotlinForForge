package thedarkcolour.kotlinforforge

import net.minecraftforge.eventbus.EventBusErrorMessage
import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.IEventListener
import net.minecraftforge.fml.*
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.ModFileScanData
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * Functions as [net.minecraftforge.fml.javafmlmod.FMLModContainer] for Kotlin
 */
public class KotlinModContainer(private val info: IModInfo, private val className: String, private val classLoader: ClassLoader, private val scanData: ModFileScanData) : ModContainer(info) {
    private lateinit var modInstance: Any
    public val eventBus: IEventBus

    init {
        logger.debug(Logging.LOADING, "Creating KotlinModContainer instance for {} with classLoader {} & {}", className, classLoader, javaClass.classLoader)
        triggerMap[ModLoadingStage.CONSTRUCT] = dummy().andThen(::constructMod).andThen(::afterEvent)
        triggerMap[ModLoadingStage.CREATE_REGISTRIES] = dummy().andThen(::fireEvent).andThen(::afterEvent)
        triggerMap[ModLoadingStage.LOAD_REGISTRIES] = dummy().andThen(::fireEvent).andThen(::afterEvent)
        triggerMap[ModLoadingStage.COMMON_SETUP] = dummy().andThen(::fireEvent).andThen(::afterEvent)
        triggerMap[ModLoadingStage.SIDED_SETUP] = dummy().andThen(::fireEvent).andThen(::afterEvent)
        triggerMap[ModLoadingStage.ENQUEUE_IMC] = dummy().andThen(::fireEvent).andThen(::afterEvent)
        triggerMap[ModLoadingStage.PROCESS_IMC] = dummy().andThen(::fireEvent).andThen(::afterEvent)
        triggerMap[ModLoadingStage.COMPLETE] = dummy().andThen(::fireEvent).andThen(::afterEvent)
        triggerMap[ModLoadingStage.GATHERDATA] = dummy().andThen(::fireEvent).andThen(::afterEvent)
        eventBus = BusBuilder.builder().setExceptionHandler(::onEventFailed).setTrackPhases(false).build()
        configHandler = Optional.of(Consumer { event -> eventBus.post(event) })
        val ctx = KotlinModLoadingContext(this)
        contextExtension = Supplier { ctx }
    }

    private fun dummy(): Consumer<LifecycleEventProvider.LifecycleEvent> = Consumer {}

    private fun onEventFailed(iEventBus: IEventBus, event: Event, iEventListeners: Array<IEventListener>, i: Int, throwable: Throwable) {
        logger.error(EventBusErrorMessage(event, i, iEventListeners, throwable))
    }

    private fun fireEvent(lifecycleEvent: LifecycleEventProvider.LifecycleEvent) {
        val event = lifecycleEvent.getOrBuildEvent(this)
        logger.debug(Logging.LOADING, "Firing event for modid $modId : $event")

        try {
            eventBus.post(event)
            logger.debug(Logging.LOADING, "Fired event for modid $modId : $event")
        } catch (throwable: Throwable) {
            logger.error(Logging.LOADING,"An error occurred while dispatching event ${lifecycleEvent.fromStage()} to $modId")
            throw ModLoadingException(modInfo, lifecycleEvent.fromStage(), "fml.modloading.errorduringevent", throwable)
        }
    }

    private fun afterEvent(lifecycleEvent: LifecycleEventProvider.LifecycleEvent) {
        if (currentState == ModLoadingStage.ERROR) {
            logger.error(Logging.LOADING, "An error occurred while dispatching event ${lifecycleEvent.fromStage()} to $modId")
        }
    }

    private fun constructMod(lifecycleEvent: LifecycleEventProvider.LifecycleEvent) {
        val modClass: Class<*>
        try {
            modClass = Class.forName(className, true, classLoader)
            logger.debug(Logging.LOADING, "Loaded kotlin modclass ${modClass.name} with ${modClass.classLoader}")
        } catch (throwable: Throwable) {
            logger.error(Logging.LOADING, "Failed to load kotlin class $className", throwable)
            throw ModLoadingException(info, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmodclass", throwable)
        }

        try {
            logger.debug(Logging.LOADING, "Loading mod instance ${getModId()} of type ${modClass.name}")
            modInstance = modClass.kotlin.objectInstance ?: modClass.newInstance()
            logger.debug(Logging.LOADING, "Loaded mod instance ${getModId()} of type ${modClass.name}")
        } catch (throwable: Throwable) {
            logger.error(Logging.LOADING, "Failed to create mod instance. ModID: ${getModId()}, class ${modClass.name}", throwable)
            throw ModLoadingException(modInfo, lifecycleEvent.fromStage(), "fml.modloading.failedtoloadmod", throwable, modClass)
        }

        try {
            logger.debug(Logging.LOADING, "Injecting Automatic Kotlin event subscribers for ${getModId()}")
            // Inject into object EventBusSubscribers
            AutoKotlinEventBusSubscriber.inject(this, scanData, modClass.classLoader)
            logger.debug(Logging.LOADING, "Completed Automatic Kotlin event subscribers for ${getModId()}")
        } catch (throwable: Throwable) {
            logger.error(Logging.LOADING, "Failed to register Automatic Kotlin subscribers. ModID: ${getModId()}, class ${modClass.name}", throwable)
            throw ModLoadingException(modInfo, lifecycleEvent.fromStage(), "fml.modloading.failedtoloadmod", throwable, modClass)
        }
    }

    override fun matches(mod: Any?): Boolean {
        return mod == modInstance
    }

    override fun getMod(): Any {
        return modInstance
    }

    override fun acceptEvent(e: Event) {
        eventBus.post(e)
    }
}