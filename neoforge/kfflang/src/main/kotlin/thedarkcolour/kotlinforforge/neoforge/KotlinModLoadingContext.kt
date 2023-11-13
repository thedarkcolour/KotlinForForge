package thedarkcolour.kotlinforforge.neoforge

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModLoadingContext

/**
 * Mod loading context for mods made with Kotlin for Forge.
 */
public class KotlinModLoadingContext(private val container: KotlinModContainer) {
    /** Mods should access through [MOD_BUS] */
    public fun getKEventBus(): IEventBus {
        return container.eventBus
    }

    public companion object {
        /** Mods should access through [MOD_CONTEXT] */
        public fun get(): KotlinModLoadingContext {
            return ModLoadingContext.get().extension()
        }
    }
}
