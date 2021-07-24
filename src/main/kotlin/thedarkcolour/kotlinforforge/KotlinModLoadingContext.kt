package thedarkcolour.kotlinforforge

import thedarkcolour.kotlinforforge.eventbus.KotlinEventBus
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT

/**
 * Mod loading context for mods made with Kotlin for Forge.
 */
public class KotlinModLoadingContext constructor(private val container: KotlinModContainer) {
    /** Mods should access through [MOD_BUS] */
    public fun getKEventBus(): KotlinEventBus {
        return container.eventBus
    }

    public companion object {
        /** Mods should access through [MOD_CONTEXT] */
        public fun get(): KotlinModLoadingContext {
            return LOADING_CONTEXT.extension()
        }
    }
}