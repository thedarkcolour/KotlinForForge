package thedarkcolour.kotlinforforge

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.registries.DeferredRegister
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_BUS

/**
 * Set 'modLoader' in mods.toml to "kotlinforforge" and loaderVersion to "[1,)".
 */
@Mod("kotlinforforge")
object KotlinForForge {
    internal val logger: Logger = LogManager.getLogger()
}