val kotlin_version: String by project
val annotations_version: String by project
val coroutines_version: String by project
val serialization_version: String by project
val max_kotlin: String by project
val max_coroutines: String by project
val max_serialization: String by project

evaluationDependsOn(":kfflib")

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("net.minecraftforge.gradle")
    id("com.modrinth.minotaur") version "2.+"
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
kotlin.jvmToolchain {}

// Enable JarInJar
jarJar.enable()

val kotlinSourceJar by tasks.creating(Jar::class) {
    val kotlinSourceSet = kotlin.sourceSets.main.get()

    from(kotlinSourceSet.kotlin.srcDirs)
    archiveClassifier.set("sources")
}

tasks.build.get().dependsOn(kotlinSourceJar)
// Workaround for JarJar not handling project dependencies
tasks.getByName("compileKotlin").dependsOn(project(":kfflib").tasks.getByName("publishToMavenLocal"))

// Workaround to remove build\java from MOD_CLASSES because SJH doesn't like nonexistent dirs
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

repositories {
    mavenCentral()
    // For testing with kfflib and making JarJar shut up
    mavenLocal()
}

dependencies {
    minecraft("net.minecraftforge:forge:1.19-41.0.91")

    fun library(dependencyNotation: String, maxVersion: String) {
        add("library", dependencyNotation) {
            exclude("org.jetbrains", "annotations")
            jarJar(group = group!!, name = name, version = "[$version, $maxVersion)") {
                exclude("org.jetbrains", "annotations")
            }
        }
    }

    library("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version", max_kotlin)
    library("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version", max_kotlin)
    library("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version", max_coroutines)
    library("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutines_version", max_coroutines)
    library("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutines_version", max_coroutines)
    library("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version", max_serialization)
    implementation(group = "thedarkcolour.kotlinforforge", name = "kfflib", version = "[${project.version}, 4.0)") {
        jarJar.pin(this, "${project.version}")
        isTransitive = false
    }

    implementation(group = "org.jetbrains", name = "annotations", version = "[$annotations_version,)") {
        jarJar.pin(this, annotations_version)
    }
}

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
    }
}

tasks.withType<Jar> {
    archiveBaseName.set("kotlinforforge")
    destinationDir = file("$rootDir/build/libs/thedarkcolour/kotlinforforge")

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

tasks.withType<net.minecraftforge.gradle.userdev.tasks.JarJar> {
    archiveClassifier.set("obf")
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
    uploadFile.set(tasks.jarJar as Any)
    gameVersions.addAll("1.18", "1.18.1", "1.19")
    loaders.add("forge")
}
