pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
}

rootProject.name = "KotlinForForge"

// One subproject per published Maven artifact (needed so each gets its own Gradle module metadata and POM).
// Project names are the Maven artifact IDs. All configuration lives in the root build.gradle.kts.
for (module in listOf("kfflang", "kfflib", "kffmod")) {
    for (platform in listOf("forge", "neoforge")) {
        val name = if (platform == "forge") module else "$module-neoforge"
        include(name)
        project(":$name").projectDir = file("src/$module/$platform")
    }
}

// thedarkcolour:kotlinforforge and thedarkcolour:kotlinforforge-neoforge ("-all" jars)
include("kotlinforforge", "kotlinforforge-neoforge")
project(":kotlinforforge").projectDir = file("src/kotlinforforge/forge")
project(":kotlinforforge-neoforge").projectDir = file("src/kotlinforforge/neoforge")

// Stub Minecraft classes to compile kfflib against, replacing ForgeGradle/NeoGradle's decompiled Minecraft
include("fakecraft")
project(":fakecraft").projectDir = file("src/fakecraft")
