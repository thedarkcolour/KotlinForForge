package thedarkcolour.kotlinforforge

import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.IEventExceptionHandler
import net.minecraftforge.fml.event.IModBusEvent

// todo remove in 5.x.x
internal sealed interface EventBusMaker {
    fun make(exceptionHandler: IEventExceptionHandler): IEventBus
}

// Reflection is needed since KFF compiled on 1.19.2 assumes BusBuilder methods are interface methods
// which produces bytecode incompatible with older versions that assume the methods are not interface
internal object OldEventBusMaker : EventBusMaker {
    private val builderMethod = BusBuilder::class.java.getDeclaredMethod("builder")
    private val setExceptionHandlerMethod = BusBuilder::class.java.getDeclaredMethod("setExceptionHandler", IEventExceptionHandler::class.java)
    private val setTrackPhasesMethod = BusBuilder::class.java.getDeclaredMethod("setTrackPhases", Boolean::class.java)
    private val markerTypeMethod = BusBuilder::class.java.getDeclaredMethod("markerType", Class::class.java)
    private val buildMethod = BusBuilder::class.java.getDeclaredMethod("build")

    override fun make(exceptionHandler: IEventExceptionHandler): IEventBus {
        val builder = builderMethod.invoke(null)

        setExceptionHandlerMethod.invoke(builder, exceptionHandler)
        setTrackPhasesMethod.invoke(builder, false)
        markerTypeMethod.invoke(builder, IModBusEvent::class.java)

        return buildMethod.invoke(builder) as IEventBus
    }
}

internal object NewEventBusMaker: EventBusMaker {
    override fun make(exceptionHandler: IEventExceptionHandler): IEventBus {
        return BusBuilder.builder().setExceptionHandler(exceptionHandler).setTrackPhases(false).markerType(IModBusEvent::class.java).useModLauncher().build()
    }
}