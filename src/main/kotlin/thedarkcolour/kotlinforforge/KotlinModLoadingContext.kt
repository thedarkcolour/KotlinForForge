package thedarkcolour.kotlinforforge

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModLoadingContext

/**
 * Functions as [net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext] for Kotlin
 */
class KotlinModLoadingContext constructor(private val container: KotlinModContainer) {
    fun getEventBus(): IEventBus {
        return container.eventBus
    }

    companion object {
        fun get(): KotlinModLoadingContext {
            return ModLoadingContext.get().extension()
        }
    }
}