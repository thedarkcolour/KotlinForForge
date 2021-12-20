buildscript {
    repositories {
        maven("https://maven.minecraftforge.net")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        mavenCentral()
    }
    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:5.1.+") {
            isChanging = true
        }
    }
}

val kotlin_version: String by project
val annotations_version: String by project
val coroutines_version: String by project
val serialization_version: String by project

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.1"
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}

apply(plugin = "net.minecraftforge.gradle")

version = "3.0.0"
group = "thedarkcolour.kotlinforforge"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
kotlin.jvmToolchain {}

val shadowJar = tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveClassifier.set("obf")

    dependencies {
        include(dependency("org.jetbrains.kotlin:kotlin-stdlib:${kotlin_version}"))
        include(dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${kotlin_version}"))
        include(dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlin_version}"))
        include(dependency("org.jetbrains.kotlin:kotlin-reflect:${kotlin_version}"))
        include(dependency("org.jetbrains:annotations:${annotations_version}"))
        include(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutines_version}"))
        include(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:${coroutines_version}"))
        include(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${coroutines_version}"))
        include(dependency("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:${serialization_version}"))
        include(dependency("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:${serialization_version}"))
    }
}

val kotlinSourceJar = tasks.register<Copy>("kotlinSourceJar") {
    val kotlinSourceSet = sourceSets.main.get() as org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
    from(kotlinSourceSet.kotlin.srcDirs)
}

tasks.build.get().dependsOn(kotlinSourceJar)
tasks.build.get().dependsOn(shadowJar)

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
    val library = this.maybeCreate("library")
    api.configure {
        extendsFrom(library)
    }
}
minecraft.runs.all {
    lazyToken("minecraft_classpath") {
        return@lazyToken configurations.getByName("library").copyRecursive().resolve()
            .joinToString(File.pathSeparator) { it.absolutePath }
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:1.18-38.0.8")

    library("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    library("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
    library("org.jetbrains:annotations:$annotations_version")
    library("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
    library("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutines_version")
    library("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutines_version")
    library("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version")
}

val Project.minecraft: net.minecraftforge.gradle.common.util.MinecraftExtension
    get() = extensions.getByType()

minecraft.let {
    it.mappings("official", "1.18")

    it.runs {
        create("client") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")

            mods {
                this.create("kotlinforforge") {
                    source(sourceSets.main.get())
                }
                this.create("kotlinforforgetest") {
                    source(sourceSets.test.get())
                }
            }
        }

        create("server") {
            workingDirectory(project.file("run/server"))

            property("forge.logging.console.level", "debug")
            property("forge.logging.markers", "scan,loading,core")

            mods {
                this.create("kotlinforforge") {
                    source(sourceSets.main.get())
                }
                this.create("kotlinforforgetest") {
                    source(sourceSets.test.get())
                }
            }
        }
    }
}



tasks.withType<Jar> {
    //archiveClassifier.set("slim")
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