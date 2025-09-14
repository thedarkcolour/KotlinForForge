import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import thedarkcolour.kotlinforforge.plugin.getPropertyString

plugins {
    java
    idea
    `maven-publish`
    kotlin("jvm")
}

val jvmTarget = JvmTarget.JVM_17

project.extensions.getByType<JavaPluginExtension>().toolchain.languageVersion.set(JavaLanguageVersion.of(jvmTarget.target))

project.version = getPropertyString("kff_version")
project.group = "thedarkcolour"

val replacements: MutableMap<String, Any> = mutableMapOf(
    "min_mc_version" to getPropertyString("min_mc_version"),
    "unsupported_mc_version" to getPropertyString("unsupported_mc_version"),
    "min_forge_version" to getPropertyString("min_forge_version"),
    "min_neo_version" to getPropertyString("min_neo_version"),
    "kff_version" to getPropertyString("kff_version")
)
val targets = mutableListOf("META-INF/mods.toml")

project.tasks {
    withType<ProcessResources> {
        inputs.properties(replacements)

        filesMatching(targets) {
            expand(replacements)
        }
    }
    withType<KotlinJvmCompile> {
        compilerOptions.jvmTarget.set(jvmTarget)
        compilerOptions.freeCompilerArgs.set(listOf("-Xexplicit-api=warning", "-Xjvm-default=all"))
    }
}
