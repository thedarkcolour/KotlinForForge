import thedarkcolour.kotlinforforge.plugin.getKffMaxVersion
import thedarkcolour.kotlinforforge.plugin.getPropertyString

plugins {
    id("kff.common-conventions")
    alias(libs.plugins.neogradle)
    alias(libs.plugins.minotaur)
    alias(libs.plugins.cursegradle)
}

base.archivesName.set("kotlinforforge")

evaluationDependsOnChildren()

configurations {
    apiElements {
        artifacts.clear()
    }
    runtimeElements {
        setExtendsFrom(emptySet())
        artifacts.clear()
        outgoing.artifact(tasks.jarJar)
    }
}

repositories {
    mavenCentral()
}

jarJar.enable()

val shadow: Configuration by configurations.creating {
    exclude("org.jetbrains", "annotations")
}

dependencies {
    shadow(libs.kotlin.reflect)
    shadow(libs.kotlin.stdlib)
    shadow(libs.kotlinx.coroutines.core)
    shadow(libs.kotlinx.coroutines.core.jvm)
    shadow(libs.kotlinx.coroutines.jdk8)
    shadow(libs.kotlinx.serialization.core)
    shadow(libs.kotlinx.serialization.json)

    // KFF Modules
    include(projects.combined.kfflang)
    include(projects.combined.kfflib)
    include(projects.combined.kffmod)
}

fun DependencyHandler.include(dep: ModuleDependency) {
    val version = project.version.toString()
    val kffMaxVersion = getKffMaxVersion()
    api(dep)

    jarJar(dep.copy()) {
        isTransitive = false
        jarJar.pin(this, version)
        jarJar.ranged(this, "[$version,$kffMaxVersion)")
    }
}

tasks {
    jarJar.configure {
        from(provider { shadow.map(::zipTree).toTypedArray() })
        manifest {
            attributes(
                "Automatic-Module-Name" to "thedarkcolour.kotlinforforge",
                "FMLModType" to "LIBRARY"
            )
        }
    }

    assemble {
        dependsOn(":combined:kfflang:build")
        dependsOn(":combined:kfflib:build")
        dependsOn(":combined:kffmod:build")
        dependsOn(jarJar)
    }
}


val supportedMcVersions = listOf("1.19.3", "1.19.4", "1.20", "1.20.1", "1.20.2")

curseforge {
    // Use the command line on Linux because IntelliJ doesn't pick up from .bashrc
    apiKey = System.getenv("CURSEFORGE_API_KEY") ?: "no-publishing-allowed"

    project(closureOf<com.matthewprenger.cursegradle.CurseProject> {
        id = "351264"
        releaseType = "release"
        gameVersionStrings.add("Forge")
        gameVersionStrings.add("NeoForge")
        gameVersionStrings.add("Java 17")
        gameVersionStrings.addAll(supportedMcVersions)

        // from Modrinth's Util.resolveFile
        mainArtifact(
            project(":combined").tasks.jarJar.get().archiveFile,
            closureOf<com.matthewprenger.cursegradle.CurseArtifact> {
                displayName = "Kotlin for Forge ${project.version}"
            })
    })
}

modrinth {
    projectId.set("ordsPcFz")
    versionName.set("Kotlin for Forge ${project.version}")
    versionNumber.set("${project.version}")
    versionType.set("release")
    gameVersions.addAll(supportedMcVersions)
    loaders.add("forge")
    loaders.add("neoforge")
    uploadFile.provider(project(":combined").tasks.jarJar)
}
tasks.register("publishModPlatforms") {
    finalizedBy(tasks.register("printPublishingMessage") {
        doFirst {
            println("Publishing Kotlin for Forge ${getPropertyString("kff_version")} to Modrinth and CurseForge")
        }
    })
    finalizedBy(tasks.modrinth)
    finalizedBy(tasks.curseforge)
}

// Kotlin function ambiguity fix
fun <T> Property<T>.provider(value: T) {
    set(value)
}
