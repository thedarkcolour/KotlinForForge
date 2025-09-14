import net.minecraftforge.gradle.common.util.RunConfig
import java.time.LocalDateTime

plugins {
    id("kff.forge-conventions")
}

val mc_version: String by project
val forge_version: String by project

minecraft {
    mappings("official", mc_version)
    copyIdeResources.set(true)

    runs {
        create("client", Action<RunConfig> {
            workingDirectory(project.file("run"))

            ideaModule = "KotlinForForge.forge.kfflang.test"

            property("forge.logging.markers", "LOADING,CORE")
            property("forge.logging.console.level", "debug")

            mods {
                create("kfflang") {
                    source(sourceSets.main.get())
                }

                create("kfflangtest") {
                    source(sourceSets.test.get())
                }
            }
        })

        create("server") {
            workingDirectory(project.file("run/server"))

            ideaModule = "KotlinForForge.forge.kfflang.test"

            property("forge.logging.markers", "LOADING,CORE")
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

    // Default classpath
    api(kotlin("stdlib"))
    api(kotlin("stdlib-common"))
    api(kotlin("stdlib-jdk8"))
    api(kotlin("stdlib-jdk7"))
    api(kotlin("reflect"))
}

tasks {
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
                "Automatic-Module-Name" to "thedarkcolour.kotlinforforge.lang",
                "FMLModType" to "LANGPROVIDER",
            )
        }
    }
}

// Workaround to remove build\classes\java from MOD_CLASSES because SJH doesn't like nonexistent dirs
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
        }
    }
}
