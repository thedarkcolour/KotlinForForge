package thedarkcolour.kotlinforforge.forge

import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.EventBus
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.registries.*
import thedarkcolour.kotlinforforge.KotlinModLoadingContext
import thedarkcolour.kotlinforforge.LOGGER
import thedarkcolour.kotlinforforge.eventbus.KotlinEventBus
import thedarkcolour.kotlinforforge.eventbus.KotlinEventBusWrapper
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/** @since 1.0.0
 * The Forge [EventBus].
 * Many game events are fired on this bus.
 *
 *  @since 1.2.0
 * This event bus supports [EventBus.addListener]
 * and [EventBus.addGenericListener]
 * for Kotlin SAM interfaces.
 *
 * Examples:
 *   @see net.minecraftforge.event.entity.player.PlayerEvent
 *   @see net.minecraftforge.event.entity.living.LivingEvent
 *   @see net.minecraftforge.event.world.BlockEvent
 */
val FORGE_BUS = KotlinEventBusWrapper(MinecraftForge.EVENT_BUS as EventBus)

/** @since 1.0.0
 * The mod-specific [EventBus].
 * Setup events are typically fired on this bus.
 *
 *  @since 1.2.0
 * This event bus supports [EventBus.addListener]
 * and [EventBus.addGenericListener]
 * for Kotlin SAM interfaces.
 *
 * Examples:
 *   @see net.minecraftforge.fml.event.lifecycle.InterModProcessEvent
 *   @see net.minecraftforge.event.AttachCapabilitiesEvent
 *   @see net.minecraftforge.event.RegistryEvent
 */
val MOD_BUS: KotlinEventBus
    inline get() = KotlinModLoadingContext.get().getKEventBus()

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

/** @since 1.2.2
 * The instance of Minecraft.
 * Make sure to only call this on the client side.
 */
val MINECRAFT: Minecraft
    @OnlyIn(Dist.CLIENT)
    inline get() = Minecraft.getInstance()

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
 * that inlines the runnable.
 */
inline fun runWhenOn(dist: Dist, toRun: () -> Unit) {
    if (DIST == dist) {
        toRun()
    }
}

/** @since 1.0.0
 * An alternative to [net.minecraftforge.fml.DistExecutor.runForDist]
 * that inlines the function call.
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

/** @since 1.2.2
 * Sided delegate with lazy values. This works well with proxies.
 * It is safe to use client-only types for [clientValue]
 * and server-only types for [serverValue].
 *
 * @param clientValue the value of this property on the client side.
 * @param serverValue the value of this property on the server side.
 * @param T the common type of both values. It is recommended to not use [Any] when possible.
 *
 * @see sidedDelegate if you'd like a sided value that is computed each time it is accessed
 */
fun <T> lazySidedDelegate(clientValue: () -> T, serverValue: () -> T): ReadOnlyProperty<Any?, T> {
    return LazySidedDelegate(clientValue, serverValue)
}

/** @since 1.2.2
 * Sided delegate with values that are evaluated each time they are accessed.
 * It is safe to use client-only types for [clientValue]
 * and server-only types for [serverValue].
 *
 * @param clientValue the value of this property on the client side.
 * @param serverValue the value of this property on the server side.
 * @param T the common type of both values. It is recommended to not use [Any] when possible.
 */
fun <T> sidedDelegate(clientValue: () -> T, serverValue: () -> T): ReadOnlyProperty<Any?, T> {
    return SidedDelegate(clientValue, serverValue)
}

/** @since 1.2.2
 * Creates a new [ObjectHolderDelegate] with the specified [registryName].
 *
 * This delegate serves as an alternative to using the
 * `@ObjectHolder` annotation, making it easier to use in Kotlin.
 */
inline fun <reified T : IForgeRegistryEntry<T>> objectHolder(registryName: ResourceLocation): ReadOnlyProperty<Any?, T> {
    return ObjectHolderDelegate(registryName, RegistryManager.ACTIVE.getRegistry(T::class.java) as ForgeRegistry<T>)
}

/** @since 1.2.2
 * Creates a new [ObjectHolderDelegate].
 * This overload uses a string instead of a ResourceLocation.
 *
 * This delegate serves as an alternative to using the
 * `@ObjectHolder` annotation, making it easier to use in Kotlin.
 */
inline fun <reified T : IForgeRegistryEntry<T>> objectHolder(registryName: String): ReadOnlyProperty<Any?, T> {
    return ObjectHolderDelegate(
            registryName = GameData.checkPrefix(registryName, true),
            registry = RegistryManager.ACTIVE.getRegistry(T::class.java) as ForgeRegistry<T>
    )
}

/** @since 1.2.2
 * Lazy sided delegate.
 * Values are initialized lazily.
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

/** @since 1.2.2
 * An alternative to the `@ObjectHolder` annotation.
 *
 * This property delegate is for people who would like to avoid
 * using annotations all over their non-static Kotlin code.
 *
 * This class has proper implementations of
 * [copy], [hashCode], [equals], and [toString].
 *
 * @param T the type of object this delegates to
 * @property registryName the registry name of the object this delegate references
 * @property registry the registry the object of this delegate is in
 * @property value the current value of this object holder.
 */
data class ObjectHolderDelegate<T : IForgeRegistryEntry<T>>(
        private val registryName: ResourceLocation,
        private val registry: ForgeRegistry<T>,
) : ReadOnlyProperty<Any?, T>, Consumer<Predicate<ResourceLocation>> {
    /**
     * Should be initialized by [accept]. If you don't register
     * a value for [registryName] during the appropriate registry event
     * then reading this property is unsafe.
     */
    private lateinit var value: T

    init {
        ObjectHolderRegistry.addHandler(this)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    /**
     * Refreshes the value of this ObjectHolder.
     *
     * This **does not** account for dummy entries.
     *
     * If the [registry] no longer contains [registryName],
     * the value will remain unchanged.
     */
    override fun accept(filter: Predicate<ResourceLocation>) {
        if (!filter.test(registry.registryName)) {
            return
        }

        if (registry.containsKey(registryName)) {
            val tempValue = registry.getValue(registryName)

            if (tempValue != null) {
                value = tempValue
            } else {
                LOGGER.debug("Unable to lookup value for $this, likely just mod options.")
            }
        }
    }
}