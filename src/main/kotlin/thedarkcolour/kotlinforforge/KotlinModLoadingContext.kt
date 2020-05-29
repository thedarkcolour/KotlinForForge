package thedarkcolour.kotlinforforge

import net.minecraftforge.eventbus.api.IEventBus
import thedarkcolour.kotlinforforge.eventbus.KotlinEventBus
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT

/**
 * Mod loading context for mods made with Kotlin for Forge.
 */
class KotlinModLoadingContext constructor(private val container: KotlinModContainer) {
    /** @since 1.2.1
     * @see thedarkcolour.kotlinforforge.forge.MOD_BUS
     */
    fun getKEventBus(): KotlinEventBus {
        return container.eventBus
    }

    /** @since 1.2.1
     * Required to make mods that use an older version of KFF work.
     *
     * @see thedarkcolour.kotlinforforge.forge.MOD_BUS
     */
    @Deprecated(
            message = "Use the KotlinEventBus version. This will be an error in Kotlin for Forge 1.3",
            replaceWith = ReplaceWith("getKEventBus()"),
            level = DeprecationLevel.WARNING,
    )
    fun getEventBus(): IEventBus {
        return container.eventBus
    }

    companion object {
        /**
         * Returns the [KotlinModLoadingContext] for the current mod
         */
        fun get(): KotlinModLoadingContext {
            return LOADING_CONTEXT.extension()
        }
    }
}