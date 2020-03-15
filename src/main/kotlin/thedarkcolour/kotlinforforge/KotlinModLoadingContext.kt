package thedarkcolour.kotlinforforge

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModLoadingContext

/**
 * Functions as [net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext] for Kotlin
 */
public class KotlinModLoadingContext constructor(private val container: KotlinModContainer) {
    public fun getEventBus(): IEventBus {
        return container.eventBus
    }

    public companion object {
        public fun get(): KotlinModLoadingContext {
            return ModLoadingContext.get().extension()
        }
    }
}