import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    id("com.modrinth.minotaur") version "2.+"
    `maven-publish`
    id("com.matthewprenger.cursegradle") version "1.4.0"
}

// Current KFF version
val kff_version: String by project
val kffMaxVersion = "${kff_version.split('.')[0].toInt() + 1}.0.0"
val kffGroup = "thedarkcolour"

allprojects {
    version = kff_version
    group = kffGroup
}

evaluationDependsOnChildren()

val min_mc_version: String by project
val unsupported_mc_version: String by project
val mc_version: String by project

val min_forge_version: String by project
val forge_version: String by project

val coroutines_version: String by project
val serialization_version: String by project

val shadow: Configuration by configurations.creating {
    exclude("org.jetbrains", "annotations")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

jarJar.enable()

configurations {
    apiElements {
        artifacts.clear()
    }
    runtimeElements {
        setExtendsFrom(emptySet())
        // Publish the jarJar
        artifacts.clear()
        outgoing.artifact(tasks.jarJar)
    }
    minecraftLibrary {
        extendsFrom(shadow)
    }
}

extensions.getByType(net.minecraftforge.gradle.userdev.UserDevExtension::class).apply {
    mappings("official", mc_version)

    runs {
        create("client") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")
        }

        create("server") {
            workingDirectory(project.file("run/server"))

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft("net.minecraftforge:forge:$mc_version-$forge_version")

    shadow("org.jetbrains.kotlin:kotlin-reflect:${kotlin.coreLibrariesVersion}")
    shadow("org.jetbrains.kotlin:kotlin-stdlib:${kotlin.coreLibrariesVersion}")
    shadow("org.jetbrains.kotlin:kotlin-stdlib-common:${kotlin.coreLibrariesVersion}")
    shadow("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutines_version}")
    shadow("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:${coroutines_version}")
    shadow("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${coroutines_version}")
    shadow("org.jetbrains.kotlinx:kotlinx-serialization-core:${serialization_version}")
    shadow("org.jetbrains.kotlinx:kotlinx-serialization-json:${serialization_version}")

    // KFF Modules
    implementation(include(project(":forge:kfflang"), kffMaxVersion))
    implementation(include(project(":forge:kfflib"), kffMaxVersion))
    implementation(include(project(":forge:kffmod"), kffMaxVersion))
}

tasks {
    jar {
        enabled = false
    }

    jarJar.configure {
        from(provider { shadow.map(::zipTree).toTypedArray() })
        manifest {
            attributes(
                "Automatic-Module-Name" to "thedarkcolour.kotlinforforge",
                "FMLModType" to "LIBRARY"
            )
        }
    }

    whenTaskAdded {
        // Disable reobfJar
        if (name == "reobfJar") {
            enabled = false
        }
        // Fight ForgeGradle and Forge crashing when MOD_CLASSES don't exist
        if (name == "prepareRuns") {
            doFirst {
                sourceSets.main.get().output.files.forEach(File::mkdirs)
            }
        }
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    assemble {
        dependsOn(jarJar)
    }
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            suppressAllPomMetadataWarnings() // Shush
            from(components["java"])
        }
    }
}

fun DependencyHandler.minecraft(
    dependencyNotation: Any
): Dependency? = add("minecraft", dependencyNotation)

// maven.repo.local is set within the Julia script in the website branch
tasks.create("publishAllMavens") {
    for (proj in arrayOf(":", ":forge:kfflib", ":forge:kfflang", ":forge:kffmod")) {
        finalizedBy(project(proj).tasks.getByName("publishToMavenLocal"))
    }
}


fun DependencyHandler.include(dep: ModuleDependency, maxVersion: String? = null): ModuleDependency {
    api(dep) // Add module metadata compileOnly dependency
    jarJar(dep.copy()) {
        isTransitive = false
        jarJar.pin(this, version)
        if (maxVersion != null) {
            jarJar.ranged(this, "[$version,$maxVersion)")
        }
    }
    return dep
}