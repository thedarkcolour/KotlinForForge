package thedarkcolour.kotlinforforge

import thedarkcolour.kotlinforforge.eventbus.KotlinEventBus
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT

/**
 * Functions as [net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext] for Kotlin
 */
class KotlinModLoadingContext constructor(private val container: KotlinModContainer) {
    /**
     * @see thedarkcolour.kotlinforforge.forge.MOD_BUS
     */
    fun getEventBus(): KotlinEventBus {
        return container.eventBus
    }

    companion object {
        fun get(): KotlinModLoadingContext {
            return LOADING_CONTEXT.extension()
        }
    }
}