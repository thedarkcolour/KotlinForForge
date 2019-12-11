package thedarkcolour.kotlinforforge.forge

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.IEventBus

/**
 * The forge event bus.
 * Many events that occur during the game are fired on this bus.
 *
 * Examples:
 *   @see net.minecraftforge.event.entity.player.PlayerEvent
 *   @see net.minecraftforge.event.entity.living.LivingEvent
 *   @see net.minecraftforge.event.world.BlockEvent
 */
val FORGE_BUS: IEventBus
    inline get() = MinecraftForge.EVENT_BUS