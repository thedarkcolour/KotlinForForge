package thedarkcolour.kotlinforforge

import net.minecraftforge.eventbus.EventBusErrorMessage
import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.IEventListener
import net.minecraftforge.fml.*
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.ModFileScanData
import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

@Suppress("UNUSED_PARAMETER")
class KotlinModContainer(info: IModInfo, className: String, loader: ClassLoader, private val scanData: ModFileScanData) : ModContainer(info) {
    private lateinit var modInstance: Any
    private val modClass: Class<*>
    val eventBus: IEventBus

    // Use a separate logger because KotlinForForge.logger isn't initialized yet
    private val logger = LogManager.getLogger()

    init {
        logger.debug(Logging.LOADING, "Creating KotlinModContainer instance for {} with classLoader {} & {}", className, loader, javaClass.classLoader)
        triggerMap[ModLoadingStage.CONSTRUCT] = dummy().andThen { beforeEvent(it) }.andThen { constructMod(it) }.andThen{ afterEvent(it) }
        triggerMap[ModLoadingStage.CREATE_REGISTRIES] = dummy().andThen { beforeEvent(it) }.andThen { fireEvent(it) }.andThen{ afterEvent(it) }
        triggerMap[ModLoadingStage.LOAD_REGISTRIES] = dummy().andThen { beforeEvent(it) }.andThen { fireEvent(it) }.andThen{ afterEvent(it) }
        triggerMap[ModLoadingStage.COMMON_SETUP] = dummy().andThen { beforeEvent(it) }.andThen { preinitMod(it) }.andThen{ fireEvent(it) }.andThen { this.afterEvent(it) }
        triggerMap[ModLoadingStage.SIDED_SETUP] = dummy().andThen { beforeEvent(it)}.andThen { fireEvent(it) }.andThen { afterEvent(it) }
        triggerMap[ModLoadingStage.ENQUEUE_IMC] = dummy().andThen { beforeEvent(it)}.andThen { initMod(it) }.andThen{ fireEvent(it) }.andThen{ this.afterEvent(it) }
        triggerMap[ModLoadingStage.PROCESS_IMC] = dummy().andThen { beforeEvent(it) }.andThen { fireEvent(it) }.andThen{ afterEvent(it) }
        triggerMap[ModLoadingStage.COMPLETE] = dummy().andThen { beforeEvent(it) }.andThen { completeLoading(it) }.andThen{ fireEvent(it) }.andThen { this.afterEvent(it) }
        triggerMap[ModLoadingStage.GATHERDATA] = dummy().andThen { beforeEvent(it) }.andThen { fireEvent(it) }.andThen{ afterEvent(it) }
        eventBus = BusBuilder.builder().setExceptionHandler{ bus, event, listeners, index, throwable -> onEventFailed(bus, event, listeners, index, throwable) }.setTrackPhases(false).build()
        configHandler = Optional.of(Consumer {event -> eventBus.post(event)})
        val ctx = KotlinModLoadingContext(this)
        contextExtension = Supplier { return@Supplier ctx}
        try {
            modClass = Class.forName(className, true, loader)
            logger.debug(Logging.LOADING, "Loaded kotlin modclass {} with {}", modClass.name, modClass.classLoader)
        } catch (e: Throwable) {
            logger.error(Logging.LOADING, "Failed to load kotlin class {}", className, e)
            throw ModLoadingException(info, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmodclass", e)
        }
    }

    private fun completeLoading(lifecycleEvent: LifecycleEventProvider.LifecycleEvent) {}
    private fun initMod(lifecycleEvent: LifecycleEventProvider.LifecycleEvent) {}
    private fun dummy(): Consumer<LifecycleEventProvider.LifecycleEvent> = Consumer {}
    private fun onEventFailed(iEventBus: IEventBus, event: Event, iEventListeners: Array<IEventListener>, i: Int, throwable: Throwable) = logger.error(EventBusErrorMessage(event, i, iEventListeners, throwable))
    private fun beforeEvent(lifecycleEvent: LifecycleEventProvider.LifecycleEvent) {}

    private fun fireEvent(lifecycleEvent: LifecycleEventProvider.LifecycleEvent) {
        val event = lifecycleEvent.getOrBuildEvent(this)
        logger.debug(Logging.LOADING, "Firing event for modid {} : {}", this.getModId(), event)
        try {

        } catch (e: Throwable) {
            logger.error(Logging.LOADING,"An error occurred while dispatching event {} to {}", lifecycleEvent.fromStage(), modId)
            throw ModLoadingException(modInfo, lifecycleEvent.fromStage(), "fml.modloading.errorduringevent", e)
        }
    }

    private fun afterEvent(lifecycleEvent: LifecycleEventProvider.LifecycleEvent) {
        if (currentState == ModLoadingStage.ERROR) {
            logger.error(Logging.LOADING, "An error occurred while dispatching event {} to {}", lifecycleEvent.fromStage(), modId)
        }
    }

    private fun preinitMod(lifecycleEvent: LifecycleEventProvider.LifecycleEvent) {}

    private fun constructMod(lifecycleEvent: LifecycleEventProvider.LifecycleEvent) {
        try {
            logger.debug(Logging.LOADING, "Loading mod instance {} of type {}", getModId(), modClass.name)
            modInstance = modClass.kotlin.objectInstance ?: modClass.newInstance()
            logger.debug(Logging.LOADING, "Loaded mod instance {} of type {}", getModId(), modClass.name)
        } catch (e: Throwable) {
            logger.error(Logging.LOADING, "Failed to create mod instance. ModID: {}, class {}", getModId(), modClass.name, e)
            throw ModLoadingException(modInfo, lifecycleEvent.fromStage(), "fml.modloading.failedtoloadmod", e, modClass)
        }

        try {
            logger.debug(Logging.LOADING, "Injecting Automatic event subscribers for {}", getModId())
            // Inject into object EventBusSubscribers
            AutoKotlinEventBusSubscriber.inject(this, scanData, modClass.classLoader)
            logger.debug(Logging.LOADING, "Completed Automatic event subscribers for {}", getModId())
        } catch (e: Throwable) {
            logger.error(Logging.LOADING, "Failed to register automatic subscribers. ModID: {}, class {}", getModId(), modClass.name, e)
            throw ModLoadingException(modInfo, lifecycleEvent.fromStage(), "fml.modloading.failedtoloadmod", e, modClass)
        }

    }

    override fun getMod(): Any {
        return modInstance
    }

    override fun matches(mod: Any?): Boolean {
        return mod == modInstance
    }
}