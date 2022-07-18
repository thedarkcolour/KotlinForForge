import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("net.minecraftforge.gradle") version "5.1.+"
}

val mc_version: String by project
val forge_version: String by project

val kotlin_version: String by project
val annotations_version: String by project
val coroutines_version: String by project
val serialization_version: String by project

val max_kotlin: String by project
val max_coroutines: String by project
val max_serialization: String by project

// Current KFF version
val kffVersion = "3.7.0"
val kffGroup = "thedarkcolour"

allprojects {
    version = kffVersion
    group = kffGroup
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
kotlin.jvmToolchain {}
jarJar.enable()

repositories {
    mavenCentral()
    mavenLocal()
}

configurations {
    val library = maybeCreate("library")
    api.configure {
        extendsFrom(library)
    }
}
minecraft.runs.all {
    lazyToken("minecraft_classpath") {
        return@lazyToken configurations["library"].copyRecursive().resolve()
            .joinToString(File.pathSeparator) { it.absolutePath }
    }
}

minecraft.run {
    mappings("official", mc_version)

    runs {
        create("client") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")

            mods {
                create("kotlinforforge") {
                    source(sourceSets.main.get())
                }
            }
        }

        create("server") {
            workingDirectory(project.file("run/server"))

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")

            mods {
                create("kotlinforforge") {
                    source(sourceSets.main.get())
                }
            }
        }
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:$mc_version-$forge_version")

    // Default classpath
    include("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", kotlin_version, max_kotlin)
    include("org.jetbrains.kotlin", "kotlin-reflect", kotlin_version, max_kotlin)
    include("org.jetbrains.kotlinx", "kotlinx-coroutines-core", coroutines_version, max_coroutines)
    include("org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm", coroutines_version, max_coroutines)
    include("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", coroutines_version, max_coroutines)
    include("org.jetbrains.kotlinx", "kotlinx-serialization-json", serialization_version, max_serialization)
    // Inherited
    include("org.jetbrains.kotlin", "kotlin-stdlib-jdk7", kotlin_version, max_kotlin, true)
    include("org.jetbrains.kotlinx", "kotlinx-serialization-core", serialization_version, max_serialization, true)
    include("org.jetbrains.kotlin", "kotlin-stdlib", kotlin_version, max_kotlin, true)
    include("org.jetbrains.kotlin", "kotlin-stdlib-common", kotlin_version, max_kotlin, true)

    // KFF Modules
    include("thedarkcolour", "kfflib", "${project.version}", "4.0", false)
    include("thedarkcolour", "kfflang", "${project.version}", "4.0", false)
}
configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("thedarkcolour:kfflib")).using(project(":kfflib"))
        substitute(module("thedarkcolour:kfflang")).using(project(":kfflang"))
    }
}

// Adds to JarJar without using as Gradle dependency
fun DependencyHandlerScope.include(group: String, name: String, version: String, maxVersion: String, isLibrary: Boolean = true) {
    val lib = if (isLibrary) {
        configurations["library"](group = group, name = name, "[$version, $maxVersion)")
    } else {
        implementation(group, name, "[$version, $maxVersion)")
    }
    jarJar(lib) {
        isTransitive = false
        jarJar.pin(this, version)
    }
}

// Sets final jar name to match old name
tasks.withType<net.minecraftforge.gradle.userdev.tasks.JarJar> {
    archiveBaseName.set("kotlinforforge")
    archiveClassifier.set("obf")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}