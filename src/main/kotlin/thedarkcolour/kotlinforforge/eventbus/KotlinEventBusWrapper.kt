package thedarkcolour.kotlinforforge.eventbus

import net.minecraftforge.eventbus.EventBus
import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.IEventExceptionHandler
import net.minecraftforge.eventbus.api.IEventListener
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import java.util.concurrent.ConcurrentHashMap

/** @since 1.2.0
 * Fixes [IEventBus.addListener] for Kotlin SAM interfaces
 * when using [FORGE_BUS].
 */
public class KotlinEventBusWrapper(private val parent: EventBus) : KotlinEventBus(BusBuilder()
        .setExceptionHandler(getExceptionHandler(parent))
        .setTrackPhases(getTrackPhases(parent))
        .also { if (getShutdown(parent)) it.startShutdown() }
) {
    override val busID: Int = getBusID(parent)
    override val listeners: ConcurrentHashMap<Any, MutableList<IEventListener>> = getListeners(parent)

    // reflection stuff
    private companion object {
        private val GET_BUS_ID = EventBus::class.java.getDeclaredField("busID").also { it.isAccessible = true }
        private val GET_LISTENERS = EventBus::class.java.getDeclaredField("listeners").also { it.isAccessible = true }
        private val GET_EXCEPTION_HANDLER = EventBus::class.java.getDeclaredField("exceptionHandler").also { it.isAccessible = true }
        private val GET_TRACK_PHASES = EventBus::class.java.getDeclaredField("trackPhases").also { it.isAccessible = true }
        private val GET_SHUTDOWN = EventBus::class.java.getDeclaredField("shutdown").also { it.isAccessible = true }

        private fun getBusID(eventBus: EventBus): Int {
            return GET_BUS_ID[eventBus] as Int
        }

        @Suppress("UNCHECKED_CAST")
        private fun getListeners(eventBus: EventBus): ConcurrentHashMap<Any, MutableList<IEventListener>> {
            return GET_LISTENERS[eventBus] as ConcurrentHashMap<Any, MutableList<IEventListener>>
        }

        private fun getExceptionHandler(eventBus: EventBus): IEventExceptionHandler {
            return GET_EXCEPTION_HANDLER[eventBus] as IEventExceptionHandler
        }

        private fun getTrackPhases(eventBus: EventBus): Boolean {
            return GET_TRACK_PHASES[eventBus] as Boolean
        }

        private fun getShutdown(eventBus: EventBus): Boolean {
            return GET_SHUTDOWN[eventBus] as Boolean
        }
    }
}