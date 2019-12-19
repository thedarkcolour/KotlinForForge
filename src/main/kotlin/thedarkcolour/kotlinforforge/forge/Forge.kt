package thedarkcolour.kotlinforforge.forge

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.loading.FMLEnvironment
import thedarkcolour.kotlinforforge.KotlinModLoadingContext

/** @since 1.0.0
 * The forge EventBus.
 * Many events that occur during the game are fired on this bus.
 *
 * Examples:
 *   @see net.minecraftforge.event.entity.player.PlayerEvent
 *   @see net.minecraftforge.event.entity.living.LivingEvent
 *   @see net.minecraftforge.event.world.BlockEvent
 */
val FORGE_BUS: IEventBus
    inline get() = MinecraftForge.EVENT_BUS

/** @since 1.0.0
 * The mod-specific EventBus.
 * Setup events are typically fired on this bus.
 *
 * Examples:
 *   @see net.minecraftforge.fml.event.lifecycle.InterModProcessEvent
 *   @see net.minecraftforge.event.AttachCapabilitiesEvent
 *   @see net.minecraftforge.event.RegistryEvent
 */
val MOD_BUS: IEventBus
    inline get() = KotlinModLoadingContext.get().getEventBus()

/** @since 1.0.0
 * Mod context for Kotlin mods.
 *
 * Used in place of [net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext]
 */
val MOD_CONTEXT: KotlinModLoadingContext
    inline get() = KotlinModLoadingContext.get()

val LOADING_CONTEXT: ModLoadingContext
    inline get() = ModLoadingContext.get()

/** @since 1.0.0
 * The current [Dist] of this environment.
 */
val DIST: Dist = FMLEnvironment.dist

/** @since 1.0.0
 * An alternative to [net.minecraftforge.fml.DistExecutor.callWhenOn]
 * that inlines the callable.
 */
inline fun <T> callWhenOn(dist: Dist, toRun: () -> T): T? {
    return if (DIST == dist) {
        try {
            toRun()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    } else {
        null
    }
}

/** @since 1.0.0
 * An alternative to [net.minecraftforge.fml.DistExecutor.runWhenOn]
 * that uses Kotlin functions instead of Java functional interfaces.
 */
inline fun runWhenOn(dist: Dist, toRun: () -> Unit) {
    if (DIST == dist) {
        toRun()
    }
}

/** @since 1.0.0
 * An alternative to [net.minecraftforge.fml.DistExecutor.runForDist]
 * that inlines the method call.
 */
inline fun <T> runForDist(clientTarget: () -> T, serverTarget: () -> T): T {
    return when (DIST) {
        Dist.CLIENT -> clientTarget()
        Dist.DEDICATED_SERVER -> serverTarget()
    }
}

/** @since 1.0.0
 * Registers a config.
 */
fun registerConfig(type: ModConfig.Type, spec: ForgeConfigSpec, fileName: String? = null) {
    if (fileName == null) {
        LOADING_CONTEXT.registerConfig(type, spec)
    } else {
        LOADING_CONTEXT.registerConfig(type, spec, fileName)
    }
}