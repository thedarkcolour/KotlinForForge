pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.minecraftforge.net/")
    }
    val kotlin_version: String by settings
    plugins {
        kotlin("jvm") version kotlin_version
        id("org.jetbrains.kotlin.plugin.serialization") version kotlin_version
    }
}