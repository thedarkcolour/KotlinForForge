package thedarkcolour.kotlinforforge.forge

import net.minecraft.resources.ResourceLocation
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.EventBus
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.registries.GameData
import net.minecraftforge.registries.IForgeRegistryEntry
import thedarkcolour.kotlinforforge.eventbus.KotlinEventBusWrapper
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
public val FORGE_BUS: KotlinEventBusWrapper = KotlinEventBusWrapper(MinecraftForge.EVENT_BUS as EventBus)

/**
 * TODO: Uncomment when Forge fixes language providers for 1.17
 * Mod-specific event bus.
 * Mod lifecycle events are fired on this bus.
 *
 * Examples:
 *   @see net.minecraftforge.fml.event.lifecycle.InterModProcessEvent
 *   @see net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
 *   @see net.minecraftforge.event.RegistryEvent
 */
//public val MOD_BUS: KotlinEventBus
//    inline get() = KotlinModLoadingContext.get().getKEventBus()

/**
 * TODO: Uncomment when Forge fixes language providers for 1.17
 * Used in place of [net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext]
 */
//public val MOD_CONTEXT: KotlinModLoadingContext
//    inline get() = KotlinModLoadingContext.get()

public val LOADING_CONTEXT: ModLoadingContext
    inline get() = ModLoadingContext.get()

/** Current dist */
public val DIST: Dist = FMLEnvironment.dist

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
public fun runWhenOn(dist: Dist, toRun: () -> Unit) {
    if (DIST == dist) {
        toRun()
    }
}

/**
 * An alternative to using [net.minecraftforge.fml.DistExecutor]
 */
public fun <T> runForDist(clientTarget: () -> T, serverTarget: () -> T): T {
    return when (DIST) {
        Dist.CLIENT -> clientTarget()
        Dist.DEDICATED_SERVER -> serverTarget()
    }
}

/**
 * Register a config
 */
public fun registerConfig(type: ModConfig.Type, spec: ForgeConfigSpec, fileName: String? = null) {
    if (fileName == null) {
        LOADING_CONTEXT.registerConfig(type, spec)
    } else {
        LOADING_CONTEXT.registerConfig(type, spec, fileName)
    }
}

/**
 * Sided delegate with lazy values. This works well with proxies, if you are still using those.
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
    return SidedDelegate(clientValue, serverValue)
}

/**
 * Provides ObjectHolder as a property delegate instead of magic annotations.
 */
public inline fun <reified T : IForgeRegistryEntry<in T>> objectHolder(registryName: ResourceLocation): ReadOnlyProperty<Any?, T> {
    return ObjectHolderDelegate(registryName, ObjectHolderDelegate.getRegistry(T::class.java))
}

/**
 * Provides ObjectHolder as a property delegate instead of magic annotations.
 */
public inline fun <reified T : IForgeRegistryEntry<in T>> objectHolder(namespace: String, registryName: String): ReadOnlyProperty<Any?, T> {
    return ObjectHolderDelegate(ResourceLocation(namespace, registryName), ObjectHolderDelegate.getRegistry(T::class.java))
}

/**
 * Provides ObjectHolder as a property delegate instead of magic annotations.
 */
public inline fun <reified T : IForgeRegistryEntry<in T>> objectHolder(registryName: String): ReadOnlyProperty<Any?, T> {
    return ObjectHolderDelegate(
        registryName = GameData.checkPrefix(registryName, true),
        registry = ObjectHolderDelegate.getRegistry(T::class.java)
    )
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
private class SidedDelegate<T>(private val clientValue: () -> T, private val serverValue: () -> T) : ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return when (DIST) {
            Dist.CLIENT -> clientValue()
            Dist.DEDICATED_SERVER -> serverValue()
        }
    }
}