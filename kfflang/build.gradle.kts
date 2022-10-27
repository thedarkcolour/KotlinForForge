import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDateTime

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("net.minecraftforge.gradle")
    id("com.modrinth.minotaur") version "2.+"
    `maven-publish`
}

val mc_version: String by project
val forge_version: String by project
val kotlin_version: String by project
val coroutines_version: String by project
val serialization_version: String by project
val max_kotlin: String by project
val max_coroutines: String by project
val max_serialization: String by project

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

// Enable JarInJar
jarJar.enable()

val library: Configuration by configurations.creating {
    exclude("org.jetbrains", "annotations")
    isTransitive = false
}

minecraft {
    mappings("official", mc_version)

    runs {
        create("client") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")

            mods {
                create("kfflang") {
                    source(sourceSets.main.get())
                }

                create("kfflangtest") {
                    source(sourceSets.test.get())
                }
            }
        }

        create("server") {
            workingDirectory(project.file("run/server"))

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")

            mods {
                create("kfflang") {
                    source(sourceSets.main.get())
                }

                create("kfflangtest") {
                    source(sourceSets.test.get())
                }
            }
        }
    }
}

configurations {
    api {
        extendsFrom(library)
    }
    minecraftLibrary {
        extendsFrom(library)
    }

    runtimeElements {
        // Remove Minecraft from transitive maven dependencies
        exclude(group = "net.minecraftforge", module = "forge")

        // Include obf jar in the final JarJar
        outgoing {
            artifacts.clear()
            artifact(tasks.jarJar)
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
    // Inherited
    library("org.jetbrains.kotlin", "kotlin-stdlib-jdk7", kotlin_version)
    library("org.jetbrains.kotlinx", "kotlinx-serialization-core", serialization_version)
    library("org.jetbrains.kotlin", "kotlin-stdlib", kotlin_version)
    library("org.jetbrains.kotlin", "kotlin-stdlib-common", kotlin_version)
}

tasks {
    jar {
        archiveClassifier.set("slim")
    }

    jarJar.configure {
        archiveClassifier.set("")
    }

    withType<Jar> {
        manifest {
            attributes(
                "Specification-Title" to "Kotlin for Forge",
                "Specification-Vendor" to "Forge",
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "thedarkcolour",
                "Implementation-Timestamp" to LocalDateTime.now(),
                "Automatic-Module-Name" to "kfflang",
                "FMLModType" to "LANGPROVIDER",
            )
        }
    }

    // Only require the lang provider to use explicit visibility modifiers, not the test mod
    withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs = listOf("-Xexplicit-api=warning", "-Xjvm-default=all")
    }
}

// Workaround to remove build\java from MOD_CLASSES because SJH doesn't like nonexistent dirs
setOf(sourceSets.main, sourceSets.test)
    .map(Provider<SourceSet>::get)
    .forEach { sourceSet ->
        val mutClassesDirs = sourceSet.output.classesDirs as ConfigurableFileCollection
        val javaClassDir = sourceSet.java.classesDirectory.get()
        val mutClassesFrom = mutClassesDirs.from
            .filter {
                val toCompare = (it as? Provider<*>)?.get()
                return@filter javaClassDir != toCompare
            }
            .toMutableSet()
        mutClassesDirs.setFrom(mutClassesFrom)
    }

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
            jarJar.component(this)
        }
    }
}

modrinth {
    projectId.set("ordsPcFz")
    versionNumber.set(project.version.toString())
    versionType.set("release")
    gameVersions.addAll("1.18", "1.18.1", "1.19")
    loaders.add("forge")
}
