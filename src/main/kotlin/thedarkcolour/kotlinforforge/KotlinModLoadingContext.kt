package thedarkcolour.kotlinforforge

import net.minecraftforge.eventbus.api.IEventBus
import thedarkcolour.kotlinforforge.eventbus.KotlinEventBus
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT

/**
 * Functions as [net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext] for Kotlin
 */
class KotlinModLoadingContext constructor(private val container: KotlinModContainer) {
    /**
     * @see thedarkcolour.kotlinforforge.forge.MOD_BUS
     */
    fun getKEventBus(): KotlinEventBus {
        return container.eventBus
    }

    /** @since 1.2.1
     * @see thedarkcolour.kotlinforforge.forge.MOD_BUS
     *
     * Required to make mods that use an older version of KFF work.
     */
    fun getEventBus(): IEventBus {
        return container.eventBus
    }

    companion object {
        fun get(): KotlinModLoadingContext {
            return LOADING_CONTEXT.extension()
        }
    }
}