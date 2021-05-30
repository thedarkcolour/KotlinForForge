package thedarkcolour.kotlinforforge.eventbus

import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.IEventBusInvokeDispatcher
import net.minecraftforge.eventbus.api.IEventListener

/** @since 1.7.1
 * Maintain compatibility between old versions of event bus.
 */
public interface IKotlinEventBus : IEventBus {
    /**
     * Implement by default so classloading [KotlinEventBus]
     * does not try to load [IEventBusInvokeDispatcher].
     */
    override fun post(event: Event, wrapper: IEventBusInvokeDispatcher): Boolean {
        return post(wrapper::invoke, event)
    }

    public fun post(wrapper: (IEventListener, Event) -> Unit, event: Event): Boolean
}