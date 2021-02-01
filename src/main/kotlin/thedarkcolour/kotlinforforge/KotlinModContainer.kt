package thedarkcolour.kotlinforforge

import net.minecraftforge.eventbus.EventBusErrorMessage
import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.IEventListener
import net.minecraftforge.fml.Logging
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.ModLoadingException
import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.IModBusEvent
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.ModFileScanData
import thedarkcolour.kotlinforforge.eventbus.KotlinEventBus
import thedarkcolour.kotlinforforge.kotlin.supply
import java.lang.IllegalArgumentException
import java.lang.reflect.Field
import java.util.function.Consumer

/**
 * The Kotlin for Forge `ModContainer`.
 */
public class KotlinModContainer(
        private val info: IModInfo,
        private val className: String,
        private val classLoader: ClassLoader,
        private val scanData: ModFileScanData,
) : ModContainer(info) {

    /**
     * The `@Mod` object or instance of the `@Mod` class.
     */
    private lateinit var modInstance: Any

    /**
     * The `IEventBus` for Kotlin for Forge mods
     * that supports `KCallable` event listeners.
     */
    public val eventBus: KotlinEventBus

    private val _triggerMap: MutableMap<ModLoadingStage, Any>?
    private val _activityMap: MutableMap<ModLoadingStage, Runnable>?

    init {
        @Suppress("UNCHECKED_CAST")
        _activityMap = try {
            ACTIVITY_MAP_FIELD?.get(this) as MutableMap<ModLoadingStage, Runnable>
        } catch (e: Exception) {
            null
        }
        @Suppress("UNCHECKED_CAST")
        _triggerMap = try {
            TRIGGER_MAP_FIELD?.get(this) as MutableMap<ModLoadingStage, Any>
        } catch (e: Exception) {
            null
        }

        LOGGER.debug(Logging.LOADING, "Creating KotlinModContainer instance for {} with classLoader {} & {}", className, classLoader, javaClass.classLoader)

        if (_activityMap != null) {
            _activityMap[ModLoadingStage.CONSTRUCT] = Runnable(::constructMod)
        } else if (_triggerMap != null) {
            setupTriggerMap(_triggerMap)
        }

        eventBus = KotlinEventBus(BusBuilder.builder().setExceptionHandler(::onEventFailed).setTrackPhases(false))
        contextExtension = supply(KotlinModLoadingContext(this))
    }

    /**
     * The `IEventExceptionHandler` that logs
     * errors in events as errors.
     */
    private fun onEventFailed(
        iEventBus: IEventBus,
        event: Event,
        iEventListeners: Array<IEventListener>,
        i: Int,
        throwable: Throwable,
    ) {
        LOGGER.error(EventBusErrorMessage(event, i, iEventListeners, throwable))
    }

    /**
     * Initializes [modInstance] and calls the mod constructor
     */
    private fun constructMod() {
        val modClass: Class<*>

        try {
            modClass = Class.forName(className, false, classLoader)
            LOGGER.debug(Logging.LOADING, "Loaded kotlin modclass ${modClass.name} with ${modClass.classLoader}")
        } catch (throwable: Throwable) {
            LOGGER.error(Logging.LOADING, "Failed to load kotlin class $className", throwable)
            throw ModLoadingException(info, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmodclass", throwable)
        }

        try {
            LOGGER.debug(Logging.LOADING, "Loading mod instance ${getModId()} of type ${modClass.name}")
            modInstance = modClass.kotlin.objectInstance ?: modClass.newInstance()
            LOGGER.debug(Logging.LOADING, "Loaded mod instance ${getModId()} of type ${modClass.name}")
        } catch (throwable: Throwable) {
            LOGGER.error(Logging.LOADING, "Failed to create mod instance. ModID: ${getModId()}, class ${modClass.name}", throwable)
            throw ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", throwable, modClass)
        }

        try {
            LOGGER.debug(Logging.LOADING, "Injecting Automatic Kotlin event subscribers for ${getModId()}")
            // Inject into object EventBusSubscribers
            AutoKotlinEventBusSubscriber.inject(this, scanData, modClass.classLoader)
            LOGGER.debug(Logging.LOADING, "Completed Automatic Kotlin event subscribers for ${getModId()}")
        } catch (throwable: Throwable) {
            LOGGER.error(Logging.LOADING, "Failed to register Automatic Kotlin subscribers. ModID: ${getModId()}, class ${modClass.name}", throwable)
            throw ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "fml.modloading.failedtoloadmod", throwable, modClass)
        }
    }

    override fun dispatchConfigEvent(event: ModConfig.ModConfigEvent) {
        eventBus.post(event)
    }

    override fun matches(mod: Any?): Boolean {
        return mod == modInstance
    }

    override fun getMod(): Any = modInstance

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

    public fun setupTriggerMap(map: MutableMap<ModLoadingStage, Any>) {
        map[ModLoadingStage.CONSTRUCT] = createTrigger( { constructMod() }, ::afterEvent)
        map[ModLoadingStage.CREATE_REGISTRIES] = createTrigger(::fireEvent, ::afterEvent)
        map[ModLoadingStage.LOAD_REGISTRIES] = createTrigger(::fireEvent, ::afterEvent)
        map[ModLoadingStage.COMMON_SETUP] = createTrigger(::fireEvent, ::afterEvent)
        map[ModLoadingStage.SIDED_SETUP] = createTrigger(::fireEvent, ::afterEvent)
        map[ModLoadingStage.ENQUEUE_IMC] = createTrigger(::fireEvent, ::afterEvent)
        map[ModLoadingStage.PROCESS_IMC] = createTrigger(::fireEvent, ::afterEvent)
        map[ModLoadingStage.COMPLETE] = createTrigger(::fireEvent, ::afterEvent)
        try {
            map[ModLoadingStage.valueOf("GATHERDATA")] = createTrigger(::fireEvent, ::afterEvent)
        } catch (e: IllegalArgumentException) {}
    }

    private fun afterEvent(lifecycleEvent: Any) {
        if (currentState == ModLoadingStage.ERROR) {
            LOGGER.error(Logging.LOADING, "An error occurred while dispatching event ${fromStageMethod!!.invoke(lifecycleEvent)} to ${getModId()}")
        }
    }

    private fun fireEvent(lifecycleEvent: Any) {
        val event = getOrBuildEventMethod!!.invoke(lifecycleEvent, this) as Event

        LOGGER.debug(Logging.LOADING, "Firing event for modid ${getModId()} : $event")

        try {
            eventBus.post(event)
            LOGGER.debug(Logging.LOADING, "Fired event for modid ${getModId()} : $event")
        } catch (throwable: Throwable) {
            LOGGER.error(Logging.LOADING, "An error occurred while dispatching event ${fromStageMethod!!.invoke(lifecycleEvent)} to ${getModId()}")
            throw ModLoadingException(getModInfo(), fromStageMethod.invoke(lifecycleEvent) as ModLoadingStage, "fml.modloading.errorduringevent", throwable)
        }
    }

    private fun createTrigger(
        consumerA: (Any) -> Unit,
        consumerB: (Any) -> Unit,
    ): Consumer<Any> {
        return Consumer { event ->
            consumerA(event)
            consumerB(event)
        }
    }

    private companion object {
        private val TRIGGER_MAP_FIELD: Field? = try {
            ModContainer::class.java.getDeclaredField("triggerMap").also { field ->
                field.isAccessible = true
            }
        } catch (e: NoSuchFieldException) {
            null
        }
        private val ACTIVITY_MAP_FIELD: Field? = try {
            ModContainer::class.java.getDeclaredField("activityMap").also { field ->
                field.isAccessible = true
            }
        } catch (e: NoSuchFieldException) {
            null
        }

        private val getOrBuildEventMethod = try {
            Class.forName("net.minecraftforge.fml.LifecycleEventProvider\$LifecycleEvent").getDeclaredMethod("getOrBuildEvent", ModContainer::class.java)
        } catch (e: ClassNotFoundException) { null } catch (e: NoSuchMethodError) { null }
        private val fromStageMethod = try {
            Class.forName("net.minecraftforge.fml.LifecycleEventProvider\$LifecycleEvent").getDeclaredMethod("fromStage")
        } catch (e: ClassNotFoundException) { null } catch (e: NoSuchMethodError) { null }
    }
}