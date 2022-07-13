package thedarkcolour.kotlinforforge

import net.minecraftforge.eventbus.api.IEventBus
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT

/**
 * Mod loading context for mods made with Kotlin for Forge.
 */
public class KotlinModLoadingContext constructor(private val container: KotlinModContainer) {
    /** Mods should access through [MOD_BUS] */
    public fun getKEventBus(): IEventBus {
        return container.eventBus
    }

    public companion object {
        /** Mods should access through [MOD_CONTEXT] */
        public fun get(): KotlinModLoadingContext {
            return LOADING_CONTEXT.extension()
        }
    }
}
