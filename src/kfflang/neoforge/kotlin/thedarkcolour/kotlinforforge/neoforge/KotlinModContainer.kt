package thedarkcolour.kotlinforforge.neoforge

import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.EventBusErrorMessage
import net.neoforged.bus.api.BusBuilder
import net.neoforged.bus.api.Event
import net.neoforged.bus.api.EventListener
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.Logging
import net.neoforged.fml.ModContainer
import net.neoforged.fml.ModLoadingException
import net.neoforged.fml.ModLoadingIssue
import net.neoforged.fml.event.IModBusEvent
import net.neoforged.fml.javafmlmod.AutomaticEventSubscriber
import net.neoforged.fml.javafmlmod.FMLModContainer
import net.neoforged.fml.loading.FMLLoader
import net.neoforged.neoforgespi.language.IModInfo
import net.neoforged.neoforgespi.language.ModFileScanData
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.function.Supplier

public class KotlinModContainer(
    info: IModInfo,
    entrypoints: List<String>,
    private val scanResults: ModFileScanData,
    gameLayer: ModuleLayer,
) : ModContainer(info) {
    private var modInstance: Any? = null
    internal val eventBus: IEventBus
    private val modClasses: List<Class<*>>
    private val layer: Module
    internal val context: KotlinModLoadingContext

    init {
        LOGGER.debug(Logging.LOADING, "Creating KotlinModContainer instance for {}", entrypoints)

        this.eventBus = BusBuilder.builder()
            .setExceptionHandler(::onEventFailed)
            .markerType(IModBusEvent::class.java)
            .allowPerPhasePost()
            .build()
        this.layer = gameLayer.findModule(info.owningFile.moduleName()).orElseThrow()

        this.context = KotlinModLoadingContext(this)
        // Backwards compatibility with FancyModLoader 3.x
        try {
            val contextExtensionField = ModContainer::class.java.getDeclaredField("contextExtension")
            val legacyExtension = Supplier { context }
            contextExtensionField.set(this, legacyExtension)
        } catch (ignored: NoSuchFieldException) {}

        this.modClasses = ArrayList(entrypoints.size)

        for (entrypoint in entrypoints) {
            try {
                val cls = Class.forName(layer, entrypoint)
                modClasses.add(cls)
                LOGGER.trace(Logging.LOADING, "Loaded modclass {} with {}", cls.name, cls.classLoader)
            } catch (t: Throwable) {
                LOGGER.error(Logging.LOADING, "Failed to load class {}", entrypoint, t)
                throw ModLoadingException(ModLoadingIssue.error("fml.modloadingissue.failedtoloadmodclass").withCause(t).withAffectedMod(info))
            }
        }
    }

    override fun getEventBus(): IEventBus = eventBus

    private fun onEventFailed(iEventBus: IEventBus, event: Event, listeners: Array<EventListener>, busId: Int, throwable: Throwable) {
        LOGGER.error(EventBusErrorMessage(event, busId, listeners, throwable))
    }

    protected override fun constructMod() {
        for (modClass in modClasses) {
            try {
                LOGGER.trace(Logging.LOADING, "Loading mod instance {} of type {}", getModId(), modClass.name)

                if (modClass.kotlin.objectInstance != null) {
                    // Handle object declarations
                    LOGGER.trace(Logging.LOADING, "Loading object instance for mod {} of type {}", getModId(), modClass.name)
                    this.modInstance = modClass.kotlin.objectInstance
                } else {
                    val constructors = modClass.constructors
                    if (constructors.size != 1) {
                        throw RuntimeException("Mod class must have exactly 1 public constructor, found " + constructors.size)
                    }
                    val constructor = constructors[0]
                    val allowedConstructorArgs = mapOf(
                        IEventBus::class.java to eventBus,
                        ModContainer::class.java to this,
                        FMLModContainer::class.java to this,
                        Dist::class.java to FMLLoader.getDist()
                    )

                    val paramTypes = constructor.parameterTypes
                    val foundArgs = hashSetOf<Class<*>>()
                    val constructorArgs = Array(paramTypes.size) { i ->
                        val argInstance = allowedConstructorArgs[paramTypes[i]] ?: throw RuntimeException("Mod constructor has unsupported argument ${paramTypes[i]}. Allowed optional argument classes: " + allowedConstructorArgs.keys.joinToString(transform = Class<*>::getSimpleName))

                        if (foundArgs.contains(paramTypes[i])) {
                            throw RuntimeException("Duplicate mod constructor argument type: ${paramTypes[i]}")
                        }
                        foundArgs.add(paramTypes[i])
                        argInstance
                    }

                    this.modInstance = constructor.newInstance(*constructorArgs)
                }

                LOGGER.trace("Loaded mod instance {} of type {}", getModId(), modClass.simpleName)
            } catch (throwable: Throwable) {
                // exceptions thrown when a reflected method call throws are wrapped in an InvocationTargetException.
                @Suppress("NAME_SHADOWING")
                var throwable = throwable
                if (throwable is InvocationTargetException) {
                    val cause = throwable.cause
                    if (cause != null) {
                        throwable = cause
                    }
                }
                LOGGER.error(Logging.LOADING, "Failed to create mod instance. ModID: {}, class {}", getModId(), modClass.name, throwable)
                throw ModLoadingException(ModLoadingIssue.error("fml.modloadingissue.failedtoloadmod", throwable).withCause(throwable).withAffectedMod(modInfo))
            }

            try {
                LOGGER.trace(Logging.LOADING, "Injecting Automatic Kotlin event subscribers for {}", getModId())
                // Inject into object EventBusSubscribers
                AutoKotlinEventBusSubscriber.inject(this, scanResults, layer)
                LOGGER.trace(Logging.LOADING, "Completed Automatic Kotlin event subscribers for {}", getModId())
            } catch (throwable: Throwable) {
                LOGGER.error(Logging.LOADING, "Failed to register Automatic Kotlin subscribers. ModID: {}, class {}", getModId(), modClass.name, throwable)
                throw ModLoadingException(ModLoadingIssue.error("fml.modloadingissue.failedtoloadmod", throwable).withCause(throwable).withAffectedMod(modInfo))
            }
        }

    }
}