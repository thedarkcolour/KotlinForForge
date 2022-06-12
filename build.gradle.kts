val kotlin_version: String by project
val annotations_version: String by project
val coroutines_version: String by project
val serialization_version: String by project

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.jetbrains.kotlin.jvm") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.serialization")
    id("net.minecraftforge.gradle") version "5.1.+"
}

version = "3.6.0"
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

val kotlinSourceJar by tasks.creating(Jar::class) {
    val kotlinSourceSet = kotlin.sourceSets.main.get()

    from(kotlinSourceSet.kotlin.srcDirs)
    archiveClassifier.set("sources")
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
    minecraft("net.minecraftforge:forge:1.19-41.0.1")

    library("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    library("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
    library("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
    library("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutines_version")
    library("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutines_version")
    library("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version")
}

val Project.minecraft: net.minecraftforge.gradle.common.util.MinecraftExtension
    get() = extensions.getByType()

minecraft.let {
    it.mappings("official", "1.19")

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