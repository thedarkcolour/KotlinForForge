import net.neoforged.gradle.dsl.common.extensions.RunnableSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.utils.extendsFrom
import java.time.LocalDateTime

plugins {
    kotlin("jvm")
    id("net.neoforged.gradle.userdev")
    `maven-publish`
    eclipse
    idea
}

val neo_version: String by project

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

// Tells NeoGradle to treat this source set as a separate mod
sourceSets["test"].extensions.getByType<RunnableSourceSet>().configure { runnable -> runnable.modIdentifier("kfflangtest") }

val nonmclibs: Configuration by configurations.creating {
}

runs {
    configureEach {
        modSource(sourceSets["main"])
        modSource(sourceSets["test"])
        dependencies {
            runtime(configuration(nonmclibs))
        }
    }
    create("client")
    create("server") {
        programArgument("--nogui")
    }
}

dependencies {
    implementation("net.neoforged:neoforge:${project.properties["neo_version"]}")

    configurations.getByName("api").extendsFrom(nonmclibs)

    // Default classpath
    nonmclibs(kotlin("stdlib"))
    nonmclibs(kotlin("stdlib-common"))
    nonmclibs(kotlin("stdlib-jdk8"))
    nonmclibs(kotlin("stdlib-jdk7"))
    nonmclibs(kotlin("reflect"))
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

    // Only require the lang provider to use explicit visibility modifiers, not the test mod
    withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs = listOf("-Xexplicit-api=warning", "-Xjvm-default=all")
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
            artifactId = "kfflang-neoforge"
        }
    }
}
