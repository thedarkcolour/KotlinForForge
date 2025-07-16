package thedarkcolour.kotlinforforge

import net.minecraftforge.eventbus.api.bus.BusGroup
import net.minecraftforge.fml.ModLoadingContext

/**
 * Mod loading context for mods made with Kotlin for Forge.
 */
public class KotlinModLoadingContext(private val container: KotlinModContainer) {
    /** Mods should access through [MOD_BUS] */
    public fun getKBusGroup(): BusGroup {
        return container.busGroup
    }

    public fun getContainer(): KotlinModContainer {
        return container
    }

    public companion object {
        /** Mods should access through [MOD_CONTEXT] */
        public fun get(): KotlinModLoadingContext {
            return ModLoadingContext.get().extension() as KotlinModLoadingContext
        }
    }
}
