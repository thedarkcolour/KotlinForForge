import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import net.minecraftforge.gradle.userdev.tasks.JarJar

plugins {
    kotlin("jvm") version "1.7.20"
    id("net.minecraftforge.gradle") version "5.1.+"
}

val mc_version: String by project
val forge_version: String by project

val coroutines_version: String by project
val serialization_version: String by project

// Current KFF version
val kffVersion = "3.7.0"
val kffGroup = "thedarkcolour"

allprojects {
    version = kffVersion
    group = kffGroup
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
jarJar.enable()

val library: Configuration by configurations.creating {
    exclude("org.jetbrains", "annotations")
    isTransitive = false
}

configurations {
    api {
        extendsFrom(library)
    }
    minecraftLibrary {
        extendsFrom(library)
    }
}

minecraft {
    mappings("official", mc_version)

    runs {
        register("client") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")
        }

        register("server") {
            workingDirectory(project.file("run/server"))

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")
        }
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:$mc_version-$forge_version")

    // Default classpath
    library(kotlin("stdlib-jdk8"))
    library(kotlin("stdlib-jdk7"))
    library(kotlin("reflect"))
    library(kotlin("stdlib"))
    library(kotlin("stdlib-common"))
    library("org.jetbrains.kotlinx", "kotlinx-coroutines-core", coroutines_version)
    library("org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm", coroutines_version)
    library("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", coroutines_version)
    library("org.jetbrains.kotlinx", "kotlinx-serialization-json", serialization_version)
    library("org.jetbrains.kotlinx", "kotlinx-serialization-core", serialization_version)

    // KFF Modules
    include(project(":kfflib"))
    include(project(":kfflang"))
}

tasks {
    // Sets final jar name to match old name
    withType<JarJar> {
        archiveBaseName.set("kotlinforforge")
        archiveClassifier.set("obf")
    }
    
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}

fun DependencyHandler.include(dep: ModuleDependency): ModuleDependency {
    jarJar(dep) {
        isTransitive = false
        jarJar.pin(this, version)
    }
    return dep
}