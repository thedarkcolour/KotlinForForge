import net.neoforged.gradle.dsl.common.extensions.RunnableSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDateTime

plugins {
    id("kff.neoforge-conventions")
}

// Tells NeoGradle to treat this source set as a separate mod
sourceSets["test"].extensions.getByType<RunnableSourceSet>().configure { run -> run.modIdentifier("kfflangtest") }

val nonmclibs: Configuration by configurations.creating {}

runs {
    configureEach {
        modSource(sourceSets["main"])
        //modSource(sourceSets["test"])
        dependencies {
            runtime((nonmclibs))
        }
    }
    create("client")
    create("server") {
        programArgument("--nogui")
    }
}

dependencies {
    implementation(libs.neoforge)

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
        compilerOptions.freeCompilerArgs.set(listOf("-Xexplicit-api=warning", "-Xjvm-default=all"))
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
