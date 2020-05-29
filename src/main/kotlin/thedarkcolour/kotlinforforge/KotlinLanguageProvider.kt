package thedarkcolour.kotlinforforge

import net.minecraftforge.fml.Logging
import net.minecraftforge.fml.javafmlmod.FMLJavaModLanguageProvider
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.ModFileScanData
import java.util.function.Consumer

/**
 * Reuse a bit of code from FMLJavaModLanguageProvider
 */
class KotlinLanguageProvider : FMLJavaModLanguageProvider() {
    override fun name() = "kotlinforforge"

    override fun getFileVisitor(): Consumer<ModFileScanData> {
        return Consumer { scanData ->
            val id2TargetMap = scanData.annotations.filter { data ->
                data.annotationType == MODANNOTATION
            }.map { data ->
                val modid = data.annotationData["value"] as String
                val modClass = data.classType.className
                LOGGER.debug(Logging.SCAN, "Found @Mod class $modClass with mod id $modid")
                modid to KotlinModTarget(modClass)
            }.toMap()

            scanData.addLanguageLoader(id2TargetMap)
        }
    }

    class KotlinModTarget constructor(private val className: String) : IModLanguageProvider.IModLanguageLoader {
        override fun <T> loadMod(info: IModInfo, modClassLoader: ClassLoader, modFileScanResults: ModFileScanData): T {
            val ktContainer = Class.forName("thedarkcolour.kotlinforforge.KotlinModContainer", true, Thread.currentThread().contextClassLoader)
            val constructor = ktContainer.declaredConstructors[0]

            LOGGER.debug(Logging.LOADING, "Loading KotlinModContainer from classloader ${Thread.currentThread().contextClassLoader} - got ${ktContainer.classLoader}}")

            return constructor.newInstance(info, className, modClassLoader, modFileScanResults) as T
        }
    }
}