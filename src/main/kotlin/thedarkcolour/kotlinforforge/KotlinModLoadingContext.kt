package thedarkcolour.kotlinforforge

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModLoadingContext

class KotlinModLoadingContext internal constructor(private val container: KotlinModContainer) {
    fun getEventBus(): IEventBus {
        return container.eventBus
    }

    companion object {
        @JvmStatic
        fun get(): KotlinModLoadingContext {
            return ModLoadingContext.get().extension()
        }
    }
}