package thedarkcolour.kotlinforforge

import net.minecraftforge.fml.Logging
import net.minecraftforge.fml.javafmlmod.FMLJavaModLanguageProvider
import net.minecraftforge.forgespi.language.ILifecycleEvent
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.ModFileScanData
import java.util.function.Consumer
import java.util.function.Supplier
import java.util.stream.Collectors

public class KotlinLanguageProvider : FMLJavaModLanguageProvider() {
    override fun getFileVisitor(): Consumer<ModFileScanData> {
        return Consumer { scanResult ->
            val target = scanResult.annotations.stream()
                    .filter { data -> data.annotationType == MODANNOTATION }
                    .peek { data -> logger.debug(Logging.SCAN, "Found @Mod class ${data.classType.className} with id ${data.annotationData["value"]}") }
                    .map { data -> KotlinModTarget(data.classType.className, data.annotationData["value"] as String) }
                    .collect(Collectors.toMap({ target: KotlinModTarget -> target.modId }, { it }, { a, _ -> a }))
            scanResult.addLanguageLoader(target)
        }
    }

    override fun <R : ILifecycleEvent<R>?> consumeLifecycleEvent(consumeEvent: Supplier<R>?) {}

    override fun name(): String = "kotlinforforge"

    public class KotlinModTarget constructor(private val className: String, val modId: String) : IModLanguageProvider.IModLanguageLoader {
        override fun <T> loadMod(info: IModInfo, modClassLoader: ClassLoader, modFileScanResults: ModFileScanData): T {
            val ktContainer = Class.forName("thedarkcolour.kotlinforforge.KotlinModContainer", true, Thread.currentThread().contextClassLoader)
            logger.debug(Logging.LOADING, "Loading KotlinModContainer from classloader ${Thread.currentThread().contextClassLoader} - got ${ktContainer.classLoader}}")
            val constructor = ktContainer.declaredConstructors[0]//(IModInfo::class.java, String::class.java, ClassLoader::class.java, ModFileScanData::class.java)!!
            return constructor.newInstance(info, className, modClassLoader, modFileScanResults) as T
        }
    }
}