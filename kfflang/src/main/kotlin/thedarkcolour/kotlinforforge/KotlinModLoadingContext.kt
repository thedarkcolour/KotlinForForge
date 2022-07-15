package thedarkcolour.kotlinforforge

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModLoadingContext

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
            return ModLoadingContext.get().extension()
        }
    }
}
