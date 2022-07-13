plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.10" apply false
    id("net.minecraftforge.gradle") version "5.1.+" apply false
}

// Current KFF version
val kffVersion = "3.7.0"
val kffGroup = "thedarkcolour.kotlinforforge"

allprojects {
    version = kffVersion
    group = kffGroup
}