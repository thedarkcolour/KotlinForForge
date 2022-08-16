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

val library: Configuration by configurations.creating {
    exclude("org.jetbrains", "annotations")
    isTransitive = false
}

configurations {
    api.configure {
        extendsFrom(library)
    }
}
minecraft.runs.all {
    lazyToken("minecraft_classpath") {
        return@lazyToken library.copyRecursive().resolve()
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
    library("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", kotlin_version)
    library("org.jetbrains.kotlin", "kotlin-reflect", kotlin_version)
    library("org.jetbrains.kotlinx", "kotlinx-coroutines-core", coroutines_version)
    library("org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm", coroutines_version)
    library("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", coroutines_version)
    library("org.jetbrains.kotlinx", "kotlinx-serialization-json", serialization_version)
    library("org.jetbrains.kotlin", "kotlin-stdlib-jdk7", kotlin_version)
    library("org.jetbrains.kotlinx", "kotlinx-serialization-core", serialization_version)
    library("org.jetbrains.kotlin", "kotlin-stdlib", kotlin_version)
    library("org.jetbrains.kotlin", "kotlin-stdlib-common", kotlin_version)

    // KFF Modules
    val kfflib = project(":kfflib")
    val kfflang = project(":kfflang")
    implementation(kfflib) {
        isTransitive = false
        jarJar(kfflib) {
            jarJar.pin(kfflib, "${project.version}")
            jarJar.ranged(kfflib, "[${project.version}, 4.0)")
        }
    }

    implementation(kfflang) {
        isTransitive = false
        jarJar(kfflang) {
            jarJar.pin(kfflang, "${project.version}")
            jarJar.ranged(kfflang, "[${project.version}, 4.0)")
        }
    }
}

// Sets final jar name to match old name
tasks.withType<net.minecraftforge.gradle.userdev.tasks.JarJar> {
    archiveBaseName.set("kotlinforforge")
    archiveClassifier.set("obf")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "17"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "17"
}