package thedarkcolour.kotlinforforge.forge

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.GenericEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.loading.FMLEnvironment
import thedarkcolour.kotlinforforge.KotlinModLoadingContext
import java.util.function.Consumer
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Forge event bus.
 * Game events are fired on this bus.
 *
 * Examples:
 *   @see net.minecraftforge.event.entity.player.PlayerEvent
 *   @see net.minecraftforge.event.entity.living.LivingEvent
 *   @see net.minecraftforge.event.world.BlockEvent
 */
public inline val FORGE_BUS: IEventBus
    get() = MinecraftForge.EVENT_BUS

/**
 * Mod-specific event bus.
 * Mod lifecycle events are fired on this bus.
 *
 * Examples:
 *   @see net.minecraftforge.fml.event.lifecycle.InterModProcessEvent
 *   @see net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
 *   @see net.minecraftforge.registries.NewRegistryEvent
 */
public inline val MOD_BUS: IEventBus
    get() = KotlinModLoadingContext.get().getKEventBus()

/**
 * Used in place of [net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext]
 */
public inline val MOD_CONTEXT: KotlinModLoadingContext
    get() = KotlinModLoadingContext.get()

public inline val LOADING_CONTEXT: ModLoadingContext
    get() = ModLoadingContext.get()

/** Current dist */
public inline val DIST: Dist
    get() = FMLEnvironment.dist

/**
 * An alternative to using [net.minecraftforge.fml.DistExecutor]
 */
public fun <T> callWhenOn(dist: Dist, toRun: () -> T): T? {
    return if (DIST == dist) {
        try {
            toRun()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    } else null
}

/**
 * An alternative to using [net.minecraftforge.fml.DistExecutor]
 */
public inline fun runWhenOn(dist: Dist, toRun: () -> Unit) {
    if (DIST == dist) {
        toRun()
    }
}

/**
 * An alternative to using [net.minecraftforge.fml.DistExecutor]
 */
public inline fun <T> runForDist(clientTarget: () -> T, serverTarget: () -> T): T {
    return if (DIST == Dist.CLIENT) {
        clientTarget()
    } else {
        serverTarget()
    }
}

/**
 * Register a config
 */
public inline fun registerConfig(type: ModConfig.Type, spec: ForgeConfigSpec, fileName: String) {
    LOADING_CONTEXT.registerConfig(type, spec, fileName)
}

public inline fun registerConfig(type: ModConfig.Type, spec: ForgeConfigSpec) {
    LOADING_CONTEXT.registerConfig(type, spec)
}

public inline fun <T : GenericEvent<out F>, reified F> IEventBus.addGenericListener(
    listener: Consumer<T>,
    priority: EventPriority = EventPriority.NORMAL,
    receiveCancelled: Boolean = false
) {
    addGenericListener(F::class.java, priority, receiveCancelled, listener)
}

/**
 * Sided delegate with lazy values. This works well with proxies, if you still use those.
 * It is safe to use side-specific values in [clientValue] and [serverValue].
 *
 * @param clientValue the value of this property on the client side.
 * @param serverValue the value of this property on the server side.
 * @param T the common type of both values. It is recommended to not use [Any] when possible.
 *
 * @see sidedDelegate if you'd like a sided value that is computed each time it is accessed
 */
public fun <T> lazySidedDelegate(clientValue: () -> T, serverValue: () -> T): ReadOnlyProperty<Any?, T> {
    return LazySidedDelegate(clientValue, serverValue)
}

/**
 * Sided delegate with values that are computed per access.
 * It is safe to use client-only types for [clientValue]
 * and server-only types for [serverValue].
 *
 * @param clientValue the value of this property on the client side.
 * @param serverValue the value of this property on the server side.
 * @param T the common type of both values. It is recommended to not use [Any] when possible.
 */
public fun <T> sidedDelegate(clientValue: () -> T, serverValue: () -> T): ReadOnlyProperty<Any?, T> {
    return SidedGetterDelegate(clientValue, serverValue)
}

/**
 * Lazy sided delegate whose values are initialized lazily.
 */
private class LazySidedDelegate<T>(clientValue: () -> T, serverValue: () -> T) : ReadOnlyProperty<Any?, T> {
    private val clientValue by lazy(clientValue)
    private val serverValue by lazy(serverValue)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return when (DIST) {
            Dist.CLIENT -> clientValue
            Dist.DEDICATED_SERVER -> serverValue
        }
    }
}

/** @since 1.2.2
 * Sided delegate for things like proxies,
 * or just a null checker for values that only exist on one side.
 * Values are computed each time they are accessed.
 */
private class SidedGetterDelegate<T>(private val clientValue: () -> T, private val serverValue: () -> T) : ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return runForDist(clientValue, serverValue)
    }
}