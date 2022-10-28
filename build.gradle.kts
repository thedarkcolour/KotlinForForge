import net.minecraftforge.gradle.common.util.RunConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("net.minecraftforge.gradle") version "5.1.+"
    `maven-publish`
}

// Current KFF version
val kffVersion = "3.8.0"
val kffMaxVersion = "3.9.0"
val kffGroup = "thedarkcolour"

allprojects {
    version = kffVersion
    group = kffGroup
}

evaluationDependsOnChildren()

val mc_version: String by project
val forge_version: String by project

val coroutines_version: String by project
val max_coroutines: String by project
val serialization_version: String by project

val shadow: Configuration by configurations.creating {
    exclude("org.jetbrains", "annotations")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

jarJar.enable()

configurations {
    runtimeElements {
        setExtendsFrom(emptySet())
        // Publish the jarJar
        artifacts.clear()
        outgoing.artifact(tasks.jarJar)
    }
}

minecraft {
    mappings("official", mc_version)

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
    minecraft("net.minecraftforge:forge:$mc_version-$forge_version")

    // Default classpath
    shadow(kotlin("stdlib-jdk8"))
    shadow(kotlin("stdlib-jdk7"))
    shadow(kotlin("reflect"))
    shadow(kotlin("stdlib"))
    shadow(kotlin("stdlib-common"))
    shadow("org.jetbrains.kotlinx", "kotlinx-coroutines-core", coroutines_version)
    shadow("org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm", coroutines_version)
    shadow("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", coroutines_version)
    shadow("org.jetbrains.kotlinx", "kotlinx-serialization-core", serialization_version)
    shadow("org.jetbrains.kotlinx", "kotlinx-serialization-json", serialization_version)
    
    // KFF Modules
    api(include(project(":kfflang"), kffMaxVersion))
    api(include(project(":kfflib"), kffMaxVersion))
}

tasks {
    // Sets final jar name to match old name
    jarJar.configure {
        from(shadow.map(::zipTree).toTypedArray())
        manifest { 
            attributes(
                "Automatic-Module-Name" to "thedarkcolour.kotlinforforge",
                "FMLModType" to "LIBRARY"
            )
        }
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
    
    assemble {
        dependsOn(jarJar)
    }
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            suppressAllPomMetadataWarnings() // Shush
            from(components["java"])
        }
    }
}

fun DependencyHandler.include(dep: ModuleDependency, maxVersion: String? = null): ModuleDependency {
    jarJar(dep) {
        isTransitive = false
        jarJar.pin(this, version)
        if (maxVersion != null) {
            jarJar.ranged(this, "[$version,$maxVersion)")
        }
    }
    return dep
}