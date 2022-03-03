package thedarkcolour.kotlinforforge.forge

import net.minecraft.util.ResourceLocation
import net.minecraftforge.api.distmarker.Dist
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
import thedarkcolour.kotlinforforge.eventbus.SpongeKotlinEventBusWrapper
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.Supplier
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
public val FORGE_BUS: KotlinEventBusWrapper = if (MinecraftForge.EVENT_BUS !is EventBus) SpongeKotlinEventBusWrapper(MinecraftForge.EVENT_BUS) else KotlinEventBusWrapper(MinecraftForge.EVENT_BUS as EventBus)

/** @since 1.0.0
 * The mod-specific [EventBus].
 * Mod lifecycle events are fired on this bus.
 *
 *  @since 1.2.0
 * This event bus supports [EventBus.addListener]
 * and [EventBus.addGenericListener]
 * for Kotlin SAM interfaces.
 *
 * Examples:
 *   @see net.minecraftforge.fml.event.lifecycle.InterModProcessEvent
 *   @see net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
 *   @see net.minecraftforge.event.RegistryEvent
 */
public val MOD_BUS: KotlinEventBus
    inline get() = KotlinModLoadingContext.get().getKEventBus()

/** @since 1.0.0
 * Mod context for Kotlin mods.
 *
 * Used in place of [net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext]
 */
public val MOD_CONTEXT: KotlinModLoadingContext
    inline get() = KotlinModLoadingContext.get()

public val LOADING_CONTEXT: ModLoadingContext
    inline get() = ModLoadingContext.get()

/** @since 1.0.0
 * The current [Dist] of this environment.
 */
public val DIST: Dist = FMLEnvironment.dist

/** @since 1.0.0
 * An alternative to [net.minecraftforge.fml.DistExecutor.callWhenOn]
 * that inlines the callable.
 *
 *  @since 1.6.1
 * No longer an inline function to maintain side safety
 */
public fun <T> callWhenOn(dist: Dist, toRun: () -> T): T? {
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
 *
 *  @since 1.6.1
 * No longer an inline function to maintain side safety
 */
public fun runWhenOn(dist: Dist, toRun: () -> Unit) {
    if (DIST == dist) {
        toRun()
    }
}

/** @since 1.0.0
 * An alternative to [net.minecraftforge.fml.DistExecutor.runForDist]
 * that inlines the function call.
 *
 *  @since 1.6.1
 * No longer an inline function to maintain side safety
 */
public fun <T> runForDist(clientTarget: () -> T, serverTarget: () -> T): T {
    return when (DIST) {
        Dist.CLIENT -> clientTarget()
        Dist.DEDICATED_SERVER -> serverTarget()
    }
}

/** @since 1.0.0
 * Registers a config.
 */
public fun registerConfig(type: ModConfig.Type, spec: ForgeConfigSpec, fileName: String? = null) {
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
public fun <T> lazySidedDelegate(clientValue: () -> T, serverValue: () -> T): ReadOnlyProperty<Any?, T> {
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
public fun <T> sidedDelegate(clientValue: () -> T, serverValue: () -> T): ReadOnlyProperty<Any?, T> {
    return SidedDelegate(clientValue, serverValue)
}

/** @since 1.2.2
 * Creates a new [ObjectHolderDelegate] with the specified [registryName].
 *
 * Provides ObjectHolders as property delegates instead of magic annotations.
 */
public inline fun <reified T : IForgeRegistryEntry<in T>> objectHolder(registryName: ResourceLocation): ReadOnlyProperty<Any?, T> {
    return ObjectHolderDelegate(registryName, ObjectHolderDelegate.getRegistry(T::class.java))
}

/** @since 1.2.2
 * Creates a new with the specified [namespace] and [registryName].
 *
 * Provides ObjectHolders as property delegates instead of magic annotations.
 */
public inline fun <reified T : IForgeRegistryEntry<in T>> objectHolder(namespace: String, registryName: String): ReadOnlyProperty<Any?, T> {
    return ObjectHolderDelegate(ResourceLocation(namespace, registryName), ObjectHolderDelegate.getRegistry(T::class.java))
}

/** @since 1.2.2
 * Creates a new [ObjectHolderDelegate].
 * This overload uses a string instead of a ResourceLocation.
 *
 * Provides ObjectHolders as property delegates instead of magic annotations.
 */
public inline fun <reified T : IForgeRegistryEntry<in T>> objectHolder(registryName: String): ReadOnlyProperty<Any?, T> {
    return ObjectHolderDelegate(
        registryName = GameData.checkPrefix(registryName, true),
        registry = ObjectHolderDelegate.getRegistry(T::class.java)
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
 * This property delegate is for those who would like to avoid
 * using annotations all over their non-static Kotlin code.
 *
 * [ObjectHolderDelegate] delegates to a non-null
 * `IForgeRegistryEntry` value with registry name [registryName]
 * in an `IForgeRegistry` [registry] of type [T].
 *
 * This class has proper implementations of
 * [copy], [hashCode], [equals], and [toString].
 *
 *  @since 1.4.0
 * [ObjectHolderDelegate] can now be used with the [KDeferredRegister].
 *
 *  @since 1.7.0
 * [ObjectHolderDelegate] now implements () -> T.
 *
 * @since 1.12.2
 * [registryName] and [registry] are now public.
 *
 * @param T the type of object this delegates to
 * @property registryName the registry name of the object this delegate references
 * @property registry the registry the object of this delegate is in
 * @property value the current value of this object holder.
 */
public data class ObjectHolderDelegate<T : IForgeRegistryEntry<in T>>(
        public val registryName: ResourceLocation,
        public val registry: IForgeRegistry<*>,
) : ReadOnlyProperty<Any?, T>, Consumer<Predicate<ResourceLocation>>, Supplier<T>, () -> T {
    /**
     * Should be initialized by [accept]. If you don't register
     * a value for [registryName] during the appropriate registry event
     * then reading this property is unsafe.
     */
    private lateinit var value: T

    init {
        ObjectHolderRegistry.addHandler(this)
    }

    override fun get(): T {
        return if (::value.isInitialized) {
            value
        } else {
            throw UninitializedPropertyAccessException("ObjectHolderDelegate $registryName of type ${registry.registryName} has not been initialized")
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return get()
    }

    override fun invoke(): T {
        return get()
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
                value = tempValue as T
            } else {
                LOGGER.debug("Unable to lookup value for $this, likely just mod options.")
            }
        }
    }

    public companion object {
        private val TYPE_2_REGISTRY = HashMap<Class<*>, IForgeRegistry<*>>()

        public fun getRegistry(clazz: Class<*>): IForgeRegistry<*> {
            return TYPE_2_REGISTRY.computeIfAbsent(clazz, ::findRegistry)
        }

        private fun findRegistry(clazz: Class<*>): IForgeRegistry<*> {
            val typeQueue = LinkedList<Class<*>>()
            var registry: IForgeRegistry<*>? = null

            typeQueue.add(clazz)

            while (typeQueue.isNotEmpty() && registry == null) {
                val type = typeQueue.remove()
                typeQueue.addAll(type.interfaces)

                if (IForgeRegistryEntry::class.java.isAssignableFrom(type)) {
                    registry = RegistryManager.ACTIVE.getRegistry(type)

                    val parent = type.superclass

                    if (parent != null) {
                        typeQueue.add(parent)
                    }
                }
            }

            return registry ?: throw IllegalArgumentException(
                    "ObjectHolderDelegate must represent " +
                    "a type that implements IForgeRegistryEntry"
            )
        }
    }
}