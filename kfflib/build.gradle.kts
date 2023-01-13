import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDateTime

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("net.minecraftforge.gradle")
    `maven-publish`
}

val mc_version: String by project
val forge_version: String by project
val kotlin_version: String by project
val coroutines_version: String by project
val serialization_version: String by project

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

minecraft {
    mappings("official", mc_version)

    runs {
        runs {
            create("client") {
                workingDirectory(project.file("run"))

                property("forge.logging.markers", "SCAN,LOADING,CORE")
                property("forge.logging.console.level", "debug")

                mods {
                    create("kfflib") {
                        source(sourceSets.main.get())
                    }
                }

                mods {
                    create("kfflibtest") {
                        source(sourceSets.test.get())
                    }
                }
            }


            create("server") {
                workingDirectory(project.file("run/server"))

                property("forge.logging.markers", "SCAN,LOADING,CORE")
                property("forge.logging.console.level", "debug")

                args("--nogui")

                mods {
                    create("kfflib") {
                        source(sourceSets.main.get())
                    }
                }

                mods {
                    create("kfflibtest") {
                        source(sourceSets.test.get())
                    }
                }
            }
        }
    }
}

configurations {
    runtimeElements {
        setExtendsFrom(emptySet())
    }
    api {
        minecraftLibrary.get().extendsFrom(this)
        minecraftLibrary.get().exclude("org.jetbrains", "annotations")
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:$mc_version-$forge_version")

    api(kotlin("stdlib-jdk8"))
    api(kotlin("reflect"))
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-core", coroutines_version)
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm", coroutines_version)
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", coroutines_version)
    api("org.jetbrains.kotlinx", "kotlinx-serialization-json", serialization_version)

    implementation(project(":kfflang"))
}

tasks {
    withType<Jar> {
        manifest {
            attributes(
                "Specification-Title" to "kfflib",
                "Specification-Vendor" to "Forge",
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "thedarkcolour",
                "Implementation-Timestamp" to LocalDateTime.now(),
                "Automatic-Module-Name" to "thedarkcolour.kotlinforforge.lib",
                "FMLModType" to "GAMELIBRARY"
            )
        }
    }
    
    // Only require the lang provider to use explicit visibility modifiers, not the test mod
    withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs = listOf("-Xexplicit-api=warning", "-Xjvm-default=all")
    }
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
