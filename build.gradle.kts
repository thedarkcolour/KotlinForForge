val kotlin_version: String by project
val annotations_version: String by project
val coroutines_version: String by project
val serialization_version: String by project

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.serialization")
    id("net.minecraftforge.gradle") version "5.1.+"
    id("com.modrinth.minotaur") version "2.+"
}

version = "3.6.0"
group = "thedarkcolour.kotlinforforge"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
kotlin.jvmToolchain {}
jarJar.enable()

val kotlinSourceJar by tasks.creating(Jar::class) {
    val kotlinSourceSet = kotlin.sourceSets.main.get()

    from(kotlinSourceSet.kotlin.srcDirs)
    archiveClassifier.set("sources")
}

tasks.build.get().dependsOn(kotlinSourceJar)

repositories {
    mavenCentral()
}

// Workaround to remove build\java from MOD_CLASSES because SJH doesn"t like nonexistent dirs
for (s in arrayOf(sourceSets.main, sourceSets.test)) {
    val sourceSet = s.get()
    val mutClassesDirs = sourceSet.output.classesDirs as ConfigurableFileCollection
    val javaClassDir = sourceSet.java.classesDirectory.get()
    val mutClassesFrom = HashSet(mutClassesDirs.from.filter {
        val provider = it as Provider<*>?
        val toCompare = if (it != null) provider!!.get() else it
        return@filter javaClassDir != toCompare
    })
    mutClassesDirs.setFrom(mutClassesFrom)
}

configurations {
    val library = maybeCreate("library")
    api.configure {
        extendsFrom(library)
    }
}
minecraft.runs.all {
    lazyToken("minecraft_classpath") {
        return@lazyToken configurations["library"].copyRecursive().resolve()
            .joinToString(File.pathSeparator) { it.absolutePath }
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:1.19-41.0.91")

    val library = configurations["library"]

    library("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version") {
        exclude(group = "org.jetbrains", module = "annotations")
        jarJar(group = "org.jetbrains", name = name, version = "[$kotlin_version, 1.8)") {
            exclude(group = "org.jetbrains", module = "annotations")
        }
    }
    library("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version") {
        exclude(group = "org.jetbrains", module = "annotations")
        jarJar(group = "org.jetbrains", name = name, version = "[$kotlin_version, 1.8)") {
            exclude(group = "org.jetbrains", module = "annotations")
        }
    }
    library("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version") {
        exclude(group = "org.jetbrains", module = "annotations")
        jarJar(group = "org.jetbrains", name = name, version = "[$coroutines_version, 1.7)") {
            exclude(group = "org.jetbrains", module = "annotations")
        }
    }
    library("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutines_version") {
        exclude(group = "org.jetbrains", module = "annotations")
        jarJar(group = "org.jetbrains", name = name, version = "[$coroutines_version, 1.7)") {
            exclude(group = "org.jetbrains", module = "annotations")
        }
    }
    library("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutines_version") {
        exclude(group = "org.jetbrains", module = "annotations")
        jarJar(group = "org.jetbrains", name = name, version = "[$coroutines_version, 1.7)") {
            exclude(group = "org.jetbrains", module = "annotations")
        }
    }
    library("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version") {
        exclude(group = "org.jetbrains", module = "annotations")
        jarJar(group = "org.jetbrains", name = name, version = "[$serialization_version, 1.4)") {
            exclude(group = "org.jetbrains", module = "annotations")
        }
    }

    jarJar(group = "org.jetbrains", name = "annotations", version = "[$annotations_version, 24.0.0)")
}

val Project.minecraft: net.minecraftforge.gradle.common.util.MinecraftExtension
    get() = extensions.getByType()

minecraft.run {
    mappings("official", "1.19")

    runs {
        create("client") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")

            mods {
                create("kotlinforforge") {
                    source(sourceSets.main.get())
                }
                create("kotlinforforgetest") {
                    source(sourceSets.test.get())
                }
            }
        }

        create("server") {
            workingDirectory(project.file("run/server"))

            property("forge.logging.console.level", "debug")
            property("forge.logging.markers", "SCAN,LOADING,CORE")

            mods {
                create("kotlinforforge") {
                    source(sourceSets.main.get())
                }
                create("kotlinforforgetest") {
                    source(sourceSets.test.get())
                }
            }
        }
    }
}

tasks.withType<Jar> {
    archiveBaseName.set("kotlinforforge")

    manifest {
        attributes(
            mapOf(
                "FMLModType" to "LANGPROVIDER",
                "Specification-Title" to "Kotlin for Forge",
                "Specification-Vendor" to "Forge",
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to "${project.version}",
                "Implementation-Vendor" to "thedarkcolour",
                "Implementation-Timestamp" to `java.text`.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .format(`java.util`.Date())
            )
        )
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs = listOf("-Xexplicit-api=warning", "-Xjvm-default=all")
}

fun DependencyHandler.minecraft(
    dependencyNotation: Any
): Dependency? = add("minecraft", dependencyNotation)

fun DependencyHandler.library(
    dependencyNotation: Any
): Dependency? = add("library", dependencyNotation)

modrinth {
    projectId.set("ordsPcFz")
    versionNumber.set("${project.version}")
    versionType.set("release")
    uploadFile.set(tasks.shadowJar as Any)
    gameVersions.addAll("1.18", "1.18.1", "1.19")
    loaders.add("forge")
}
