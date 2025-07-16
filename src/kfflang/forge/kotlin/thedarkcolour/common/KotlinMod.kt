package thedarkcolour.common

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.eventbus.api.bus.BusGroup
import net.minecraftforge.fml.Bindings
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.KotlinModLoadingContext
import java.util.function.Supplier

/**
 *  This defines a Kotlin Mod Class
 *  Any class found with this annotation applied will be loaded as a Mod. The instance that is loaded will
 *  represent the mod to other Mods in the system. It will be sent various subclasses of [ModLifecycleEvent]
 *  at pre-defined times during the loading of the game.
 *  @author Chidoziealways
 *  @since 6.0.0
 */

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public annotation class KotlinMod(val value: String) {

    public annotation class KotlinEventBusSubscriber(
        val value: Array<Dist> = [Dist.CLIENT, Dist.DEDICATED_SERVER],
        val modId: String = "",
        val bus: KotlinBus = KotlinBus.BOTH
    ) {
    }
}

public enum class KotlinBus(public val eventBusSupplier: Supplier<BusGroup?>) {
    /**
     * The main BusGroup that most game events are fired on.
     */
    FORGE(Bindings.getForgeBus()),

    /**
     * The Mod-Specific event BusGroup, usually for mod lifecycle events.
     * @see KotlinModLoadingContext.getKBusGroup()
     */
    MOD({ KotlinModLoadingContext.get().getKBusGroup() }),

    /**
     * Both the [FORGE] and [MOD] buses. This is slower to register events in your class but
     * allows you to listen to events from different BusGroup types without needing separate classes annotated
     * with [thedarkcolour.common.KotlinMod.KotlinEventBusSubscriber].
     */
    BOTH({null});

    public fun bus(): Supplier<BusGroup?> = eventBusSupplier
}
