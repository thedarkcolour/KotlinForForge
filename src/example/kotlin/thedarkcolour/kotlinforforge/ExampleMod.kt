package thedarkcolour.kotlinforforge

import net.minecraft.block.Block
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.server.FMLServerStartingEvent
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.lazySidedDelegate
import thedarkcolour.kotlinforforge.proxy.ClientProxy
import thedarkcolour.kotlinforforge.proxy.IProxy
import thedarkcolour.kotlinforforge.proxy.ServerProxy

/**
 * Example mod for anyone who'd like to see
 * how a mod would be made with Kotlin for Forge.
 *
 * This mod has a modid of "examplemod", listens
 * for the ``RegistryEvent.Register<Block>`` and
 * for the ``FMLServerStartingEvent``.
 *
 * It registers event listeners by adding event listeners
 * directly to the event buses KFF provides and
 * by using the ``@EventBusSubscriber`` annotation.
 */
@Mod(ExampleMod.ID)
object ExampleMod {
    /**
     * Your mod's ID
     */
    const val ID = "examplemod"

    /**
     * The sided proxy. Since we use a lazy sided delegate,
     * the supplier parameters are invoked only once.
     */
    private val proxy by lazySidedDelegate(::ClientProxy, ::ServerProxy)

    /**
     * Example of using the KotlinEventBus
     * to register a function reference.
     *
     * Event classes with a generic type
     * should be registered using ``addGenericListener``
     * instead of ``addListener``.
     */
    init {
        MOD_BUS.addGenericListener(::registerBlocks)

        proxy.modConstruction()
    }

    /**
     * Handle block registry here.
     */
    private fun registerBlocks(event: RegistryEvent.Register<Block>) {
        // ...
    }

    /**
     * Example of an object class using the
     * ``@Mod.EventBusSubscriber`` annotation
     * to automatically subscribe functions
     * to the forge event bus.
     *
     * Even though the ``Bus.FORGE`` event bus
     * is default, I think that it's still
     * a good practice to specify the bus explicitly.
     */
    @Mod.EventBusSubscriber(modid = ExampleMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    object EventHandler {
        /**
         * Handles things like registering commands.
         */
        @SubscribeEvent
        fun onServerStarting(event: FMLServerStartingEvent) {
            // ...
        }
    }
}