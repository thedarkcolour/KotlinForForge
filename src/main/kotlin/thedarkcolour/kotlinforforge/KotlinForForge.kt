package thedarkcolour.kotlinforforge

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.AdvancementEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS

/**
 * Set `modLoader` in mods.toml to
 * `"kotlinforforge"` and loaderVersion to `"[1.7,)"`.
 *
 * Make sure to use [KotlinModLoadingContext]
 * instead of [FMLJavaModLoadingContext].
 *
 * For a more detailed example mod,
 * check out the [KotlinModdingSkeleton repository](https://github.com/thedarkcolour/KotlinModdingSkeleton).
 */
@Mod("kotlinforforge")
public object KotlinForForge {
    init {
        MOD_BUS.addListener { event: FMLLoadCompleteEvent ->
            val a = FORGE_BUS.post(AdvancementEvent(null, null))
            val b = MinecraftForge.EVENT_BUS.post(AdvancementEvent(null, null))

            println("POST TEST: $a")
            println("POST TEST: $b")
        }
    }
}