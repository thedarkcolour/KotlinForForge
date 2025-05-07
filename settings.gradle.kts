pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven("https://maven.neoforged.net/releases")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("0.9.0")
}
// Really wanted to avoid subprojects, but oh well. Needed for proper JarJar metadata
include("combined", "combined:kfflang", "combined:kfflib", "combined:kffmod")

rootProject.name = "KotlinForForge"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

