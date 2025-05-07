package thedarkcolour.kotlinforforge.neoforge

import net.neoforged.fml.ModContainer
import net.neoforged.fml.ModLoadingIssue
import net.neoforged.fml.common.Mod
import net.neoforged.fml.javafmlmod.AutomaticEventSubscriber
import net.neoforged.fml.loading.FMLLoader
import net.neoforged.fml.loading.JarVersionLookupHandler
import net.neoforged.neoforgespi.IIssueReporting
import net.neoforged.neoforgespi.language.IModInfo
import net.neoforged.neoforgespi.language.IModLanguageLoader
import net.neoforged.neoforgespi.language.ModFileScanData
import net.neoforged.neoforgespi.language.ModFileScanData.AnnotationData
import net.neoforged.neoforgespi.locating.IModFile
import org.objectweb.asm.Type
import java.lang.annotation.ElementType

public class KotlinLanguageLoader : IModLanguageLoader {
    override fun name(): String = "kotlinforforge"

    // ? to avoid classloading kotlin Intrinsics
    override fun version(): String? {
        return JarVersionLookupHandler.getVersion(this.javaClass).orElse("5.1.0")
    }

    override fun loadMod(info: IModInfo, modFileScanResults: ModFileScanData, layer: ModuleLayer): ModContainer {
        // avoid usage of stream
        val modClasses = modFileScanResults.annotations
            .filter { data ->
                isAnnotatedByMod(data)
                        && info.modId == data.annotationData.get("value")
                        && AutomaticEventSubscriber.getSides(data.annotationData.get("dist")).contains(FMLLoader.getDist())
            }
            .map { data -> data.clazz.className }

        return KotlinModContainer(info, modClasses, modFileScanResults, layer)
    }

    override fun validate(file: IModFile, loadedContainers: MutableCollection<ModContainer>, reporter: IIssueReporting) {
        val modids = file.modInfos
            .filter { info -> info.loader == this }
            .map { it.modId }
            .toSet()

        file.scanResult.annotations
            .filter { isAnnotatedByMod(it) }
            .forEach { data ->
                val modid = data.annotationData.get("value")

                if (!modids.contains(modid)) {
                    reporter.addIssue(ModLoadingIssue.error("fml.modloadingissue.javafml.dangling_entrypoint", modid, data.clazz.className, file.filePath).withAffectedModFile(file))
                }
            }
    }

    private fun isAnnotatedByMod(data: AnnotationData) = data.targetType == ElementType.TYPE && data.annotationType == MOD_TYPE

    private companion object {
        @JvmStatic
        private val MOD_TYPE = Type.getType(Mod::class.java)
    }
}