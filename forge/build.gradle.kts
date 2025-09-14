import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import thedarkcolour.kotlinforforge.plugin.getKffMaxVersion
import thedarkcolour.kotlinforforge.plugin.getPropertyString

plugins {
    alias(libs.plugins.forgegradle)
    id("kff.forge-conventions")
}

val shadow: Configuration by configurations.creating {
    exclude("org.jetbrains", "annotations")
}

jarJar.enable()

configurations {
    apiElements {
        artifacts.clear()
    }
    runtimeElements {
        setExtendsFrom(emptySet())
        // Publish the jarJar
        artifacts.clear()
        outgoing.artifact(tasks.jarJar)
    }
    minecraftLibrary {
        extendsFrom(shadow)
    }
}

extensions.getByType(net.minecraftforge.gradle.userdev.UserDevExtension::class).apply {
    mappings("official", getPropertyString("mc_version"))

    runs {
        create("client") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")
        }

        create("server") {
            workingDirectory(project.file("run/server"))

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")
        }
    }
}

repositories {
    mavenCentral()
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
    implementation(include(project(":forge:kfflang")))
    implementation(include(project(":forge:kfflib")))
    implementation(include(project(":forge:kffmod")))
}


fun DependencyHandler.include(dep: ModuleDependency): ModuleDependency {
    val version = project.version.toString()
    val kffMaxVersion = getKffMaxVersion()

    api(dep) // Add module metadata compileOnly dependency
    jarJar(dep.copy()) {
        isTransitive = false
        jarJar.pin(this, version)
        jarJar.ranged(this, "[$version,$kffMaxVersion)")
    }
    return dep
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

    whenTaskAdded {
        // Disable reobfJar
        if (name == "reobfJar") {
            enabled = false
        }
        // Fight ForgeGradle and Forge crashing when MOD_CLASSES don't exist
        if (name == "prepareRuns") {
            doFirst {
                sourceSets.main.get().output.files.forEach(File::mkdirs)
            }
        }
    }

    assemble {
        dependsOn(jarJar)
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            suppressAllPomMetadataWarnings() // Shush
            from(components["java"])
            artifactId = "kotlinforforge"
        }
    }
}

// maven.repo.local is set within the Julia script in the website branch
tasks.register("publishAllMavens") {
    dependsOn(":forge:publishToMavenLocal")
    dependsOn(":forge:kfflib:publishToMavenLocal")
    dependsOn(":forge:kfflang:publishToMavenLocal")
    dependsOn(":forge:kffmod:publishToMavenLocal")
}
