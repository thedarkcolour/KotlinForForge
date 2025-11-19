import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDateTime

plugins {
    id("kff.forge-conventions")
}

val mc_version: String by project

minecraft {
    mappings("official", mc_version)
    copyIdeResources.set(true)

    runs {
        runs {
            create("client") {
                workingDirectory(project.file("run"))

                ideaModule = "${project.parent!!.name}.${project.name}.test"

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

                ideaModule = "${project.parent!!.name}.${project.name}.test"

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

            create("gameTestServer") {
                workingDirectory(project.file("run/server"))

                ideaModule = "${project.parent!!.name}.${project.name}.test"

                property("forge.logging.markers", "SCAN,LOADING,CORE")
                property("forge.logging.console.level", "warn")
                property("forge.enabledGameTestNamespaces", "kfflibtest")

                mods {
                    create("kfflib") {
                        source(sourceSets.main.get())
                    }

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
    // Default classpath
    api(libs.kotlin.stdlib.jdk8)
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.core.jvm)
    api(libs.kotlinx.coroutines.jdk8)
    api(libs.kotlinx.serialization.json)

    implementation(projects.forge.kfflang)
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
        compilerOptions.freeCompilerArgs.set(listOf("-Xexplicit-api=warning", "-Xjvm-default=all"))
    }
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
