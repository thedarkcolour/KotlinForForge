package thedarkcolour.kotlinforforge.eventbus

import net.minecraftforge.eventbus.EventBus
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.IEventListener

internal class SpongeKotlinEventBusWrapper(private val spongeBus: IEventBus) : KotlinEventBusWrapper(getWrappedBus(spongeBus.also { println(it.javaClass) })) {
    override fun post(event: Event): Boolean {
        return spongeBus.post(event)
    }

    override fun post(wrapper: (IEventListener, Event) -> Unit, event: Event): Boolean {
        return spongeBus.post(event, wrapper)
    }

    private companion object {
        private val GET_BUS_ID = Class.forName("org.spongepowered.forge.launch.event.ForgeEventManager").getDeclaredField("wrappedEventBus").also { it.isAccessible = true }

        private fun getWrappedBus(eventBus: IEventBus): EventBus {
            return GET_BUS_ID[eventBus] as EventBus
        }
    }
}