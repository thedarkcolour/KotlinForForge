plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.10"
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

minecraft.mappings("official", mc_version)

dependencies {
    minecraft("net.minecraftforge:forge:$mc_version-$forge_version")

    // Default classpath
    compileLibrary("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", kotlin_version, max_kotlin)
    compileLibrary("org.jetbrains.kotlin", "kotlin-reflect", kotlin_version, max_kotlin)
    compileLibrary("org.jetbrains.kotlinx", "kotlinx-coroutines-core", coroutines_version, max_coroutines)
    compileLibrary("org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm", coroutines_version, max_coroutines)
    compileLibrary("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", coroutines_version, max_coroutines)
    compileLibrary("org.jetbrains.kotlinx", "kotlinx-serialization-json", serialization_version, max_serialization)
    // Inherited
    compileLibrary("org.jetbrains.kotlin", "kotlin-stdlib-jdk7", kotlin_version, max_kotlin)
    compileLibrary("org.jetbrains.kotlinx", "kotlinx-serialization-core", serialization_version, max_serialization)
    compileLibrary("org.jetbrains.kotlin", "kotlin-stdlib", kotlin_version, max_kotlin)
    compileLibrary("org.jetbrains.kotlin", "kotlin-stdlib-common", kotlin_version, max_kotlin)

    // KFF Modules
    compileLibrary("thedarkcolour", "kfflib", "${project.version}", "4.0")
    compileLibrary("thedarkcolour", "kotlinforforge", "${project.version}", "4.0")
}

// Adds to JarJar without using as Gradle dependency
fun DependencyHandlerScope.compileLibrary(group: String, name: String, version: String, maxVersion: String, obf: Boolean = false) {
    val lib = this.create(group, name, version = "[$version,$maxVersion)")
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