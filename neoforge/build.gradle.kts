import thedarkcolour.kotlinforforge.plugin.getKffMaxVersion

plugins {
    id("kff.neoforge-conventions")
}

evaluationDependsOnChildren()

val shadow: Configuration by configurations.creating {
    exclude("org.jetbrains", "annotations")
}

jarJar.enable()

// Only publish "-all" variant
configurations {
    apiElements {
        artifacts.clear()
    }
    runtimeElements {
        setExtendsFrom(emptySet())
        // Publish the jarJar ONLY
        artifacts.clear()
        outgoing.artifact(tasks.jarJar)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    shadow(libs.kotlin.reflect)
    shadow(libs.kotlin.stdlib)
    shadow(libs.kotlinx.coroutines.core)
    shadow(libs.kotlinx.coroutines.core.jvm)
    shadow(libs.kotlinx.coroutines.jdk8)
    shadow(libs.kotlinx.serialization.core)
    shadow(libs.kotlinx.serialization.json)

    // KFF Modules
    implementation(include(projects.neoforge.kfflang))
    implementation(include(projects.neoforge.kfflib))
    implementation(include(projects.neoforge.kffmod))
}

fun DependencyHandler.include(dep: ModuleDependency): ModuleDependency {
    val version = project.version.toString()
    val kffMaxVersion = getKffMaxVersion()

    api(dep) // Add module metadata compileOnly dependency
    jarJar(dep.copy()) {
        isTransitive = false
        jarJar.pin(this, version)
        jarJar.ranged(this, "[$version,$kffMaxVersion)")
    }
    return dep
}

// maven.repo.local is set within the Julia script in the website branch
tasks.register("publishAllMavens") {
    dependsOn(":neoforge:publishToMavenLocal")
    dependsOn(":neoforge:kfflib:publishToMavenLocal")
    dependsOn(":neoforge:kfflang:publishToMavenLocal")
    dependsOn(":neoforge:kffmod:publishToMavenLocal")
}

tasks {
    jarJar.configure {
        from(provider { shadow.map(::zipTree).toTypedArray() })
        manifest {
            attributes(
                "Automatic-Module-Name" to "thedarkcolour.kotlinforforge",
                "FMLModType" to "LIBRARY"
            )
        }
    }

    assemble {
        dependsOn(jarJar)
    }
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "kotlinforforge-neoforge"
        }
    }
}
