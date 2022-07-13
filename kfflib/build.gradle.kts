plugins {
    id("org.jetbrains.kotlin.jvm")
    id("net.minecraftforge.gradle")
    `maven-publish`
}

group = "thedarkcolour.kotlinforforge"
version = "3.7.0"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
kotlin.jvmToolchain {}

val kotlinSourceJar by tasks.creating(Jar::class) {
    val kotlinSourceSet = kotlin.sourceSets.main.get()

    from(kotlinSourceSet.kotlin.srcDirs)
    archiveClassifier.set("sources")
}

tasks.build.get().dependsOn(kotlinSourceJar)

repositories {
    mavenCentral()
    // For testing with kfflib
    mavenLocal()
}

dependencies {
    minecraft("net.minecraftforge:forge:1.19-41.0.91")
}

minecraft.run {
    mappings("official", "1.19")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs = listOf("-Xexplicit-api=warning", "-Xjvm-default=all")
}
