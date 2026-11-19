@file:Suppress("AvoidApplyPluginMethod")

import org.gradle.kotlin.dsl.support.normaliseLineSeparators
import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDateTime

plugins {
    base
    idea

    alias(libs.plugins.ideaext)
    alias(libs.plugins.kotlin).apply(false)
    alias(libs.plugins.mod.publish.plugin)
}

// make version catalog accessible to subprojects
val rootLibs = libs

val kffVersion = property("kff_version") as String
val kffMaxVersion = property("kff_max_version") as String

version = kffVersion
group = "thedarkcolour"

tasks.wrapper {
    distributionType = Wrapper.DistributionType.BIN
}

allprojects {
    version = kffVersion
    group = "thedarkcolour"

    repositories {
        mavenCentral()
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://libraries.minecraft.net/")
    }
}

//
// SUBPROJECTS
//

// fake minecraft code to compile against so i don't have to do a full decomp
project(":fakecraft") {
    apply(plugin = "java-library")
    layout.buildDirectory.set(rootProject.layout.buildDirectory.dir("fakecraft"))

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        sourceSets["main"].java.setSrcDirs(listOf("java"))
        sourceSets["main"].resources.setSrcDirs(emptyList<Any>())
    }
    dependencies {
        "compileOnly"(rootLibs.joml)
    }
}

// kfflang, kfflib, kffmod
val forgeProjects = listOf("kfflang", "kfflib", "kffmod")
// kfflang-neoforge, kfflib-neoforge, kffmod-neoforge
val neoForgeProjects = forgeProjects.map { "$it-neoforge" }

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val replacements = mapOf(
        "min_mc_version" to project.property("min_mc_version"),
        "unsupported_mc_version" to project.property("unsupported_mc_version"),
        "kff_version" to project.property("kff_version"),
        "kff_max_version" to project.property("kff_max_version"),
    )
    inputs.properties(replacements)
    filesMatching(listOf("META-INF/mods.toml")) {
        expand(replacements)
    }
    from("src/kffmod/templates")
    into("build/generated/sources/modMetadata")
}

idea.project.settings.taskTriggers.afterSync(generateModMetadata)

fun jarManfiest(module: String, isNeoForge: Boolean): Map<String, Any>? {
    return when (module) {
        "kfflang" -> linkedMapOf(
            "Specification-Title" to "Kotlin for Forge",
            "Specification-Vendor" to "Forge",
            "Specification-Version" to "1",
            "Implementation-Title" to "kfflang",
            "Implementation-Version" to kffVersion,
            "Implementation-Vendor" to "thedarkcolour",
            "Implementation-Timestamp" to LocalDateTime.now(),
            "Automatic-Module-Name" to "thedarkcolour.kotlinforforge.lang",
            "FMLModType" to "LANGPROVIDER",
        )
        "kfflib" -> linkedMapOf(
            "Specification-Title" to if (isNeoForge) "Kotlin for Forge" else "kfflib",
            "Specification-Vendor" to "Forge",
            "Specification-Version" to "1",
            "Implementation-Title" to "kfflib",
            "Implementation-Version" to kffVersion,
            "Implementation-Vendor" to "thedarkcolour",
            "Implementation-Timestamp" to LocalDateTime.now(),
            "Automatic-Module-Name" to "thedarkcolour.kotlinforforge.lib",
            "FMLModType" to "GAMELIBRARY",
        )
        else -> null
    }
}

for (projectName in forgeProjects + neoForgeProjects) {
    project(":$projectName") {
        val isNeoForge = projectName.endsWith("-neoforge")
        val module = projectName.removeSuffix("-neoforge")

        apply(plugin = "java-library")
        apply(plugin = "maven-publish")
        apply(plugin = "org.jetbrains.kotlin.jvm")

        layout.buildDirectory.set(rootProject.layout.buildDirectory.dir(projectName))

        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(17))
            withSourcesJar()

            sourceSets["main"].java.setSrcDirs(emptyList<Any>())
            sourceSets["main"].resources.setSrcDirs(listOf(if (module == "kffmod") "../common/resources" else "resources"))
        }
        extensions.configure<KotlinJvmProjectExtension> {
            sourceSets["main"].kotlin.setSrcDirs(listOf("kotlin"))
        }

        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
                freeCompilerArgs.set(listOf("-Xexplicit-api=warning", "-Xjvm-default=all"))
                // Kotlin module name used to be the subproject name, which was the same for Forge and NeoForge
                moduleName.set(module)
            }
        }

        if (module == "kffmod") {
            // Jar gets the generated mods.toml, sources jar gets the template
            tasks.named<ProcessResources>("processResources") {
                from(generateModMetadata)
            }
            tasks.named<Jar>("sourcesJar") {
                from(rootProject.file("src/kffmod/templates"))
            }
        }

        jarManfiest(module, isNeoForge)?.let { attributes ->
            tasks.withType<Jar>().configureEach {
                manifest.attributes(attributes)
            }
        }

        // Forge modules never published their runtime dependencies
        if (!isNeoForge) {
            configurations.named("runtimeElements") {
                setExtendsFrom(emptySet())
            }
        }

        val platformProject = { mod: String ->
            val projectPath = if (isNeoForge) ":$mod-neoforge" else ":$mod"
            dependencies.project(mapOf("path" to projectPath)) as ProjectDependency
        }

        dependencies {
            // forge/neoforge
            if (isNeoForge) {
                "compileOnly"(variantOf(rootLibs.neoforge.universal) { classifier("universal") })
            } else {
                "compileOnly"(variantOf(rootLibs.forge.universal) { classifier("universal") }) { isTransitive = false }
                "compileOnly"(rootLibs.bundles.forge)
            }
            // lib and mod need fakecraft
            if (module != "kfflang") {
                "compileOnly"(project(":fakecraft"))
                "compileOnly"(rootLibs.joml)
            }
            // kotlin
            "api"(rootLibs.kotlin.stdlib.jdk8)
            "api"(rootLibs.kotlin.reflect)
            "api"(rootLibs.kotlinx.coroutines.core)
            "api"(rootLibs.kotlinx.coroutines.core.jvm)
            "api"(rootLibs.kotlinx.coroutines.jdk8)
            "api"(rootLibs.kotlinx.serialization.json)

            // dependencies on other subprojects
            if (module == "kfflib") {
                "implementation"(platformProject("kfflang"))
            } else if (module == "kffmod") {
                "implementation"(platformProject("kfflang")) { isTransitive = false }
                "implementation"(platformProject("kfflib")) { isTransitive = false }
            }
        }

        extensions.configure<PublishingExtension> {
            publications.register<MavenPublication>("maven") {
                from(components["java"])
            }
            repositories {
                maven(rootProject.file("site"))
            }
        }
    }
}

//
// "-all" JARS
//

// Kotlin libraries that get shaded directly into every "-all" jar
val shadedKotlin: Configuration by configurations.creating {
    isCanBeConsumed = false
    exclude("org.jetbrains", "annotations")
}
dependencies {
    shadedKotlin(libs.bundles.kotlin)
}

// builds a fatjar including the Kotlin libraries and provided innerJars (kfflang, kfflib, kffmod or their combined versions)
fun Jar.configureAllJar(metadataDir: Any, innerJars: Map<String, Provider<RegularFile>>) {
    group = "kff"
    archiveClassifier.set("all")
    // First copy wins, like the jarJar tasks
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(provider {
        shadedKotlin.map(::zipTree)
    })
    into("META-INF/jarjar") {
        innerJars.forEach { (fileName, jar) ->
            from(jar) {
                rename { fileName }
            }
        }
        from(metadataDir) {
            include("metadata.json")
            val replacements = mapOf("kff_version" to kffVersion, "kff_max_version" to kffMaxVersion)
            inputs.properties(replacements)
            expand(replacements)
        }
    }
    manifest.attributes(
        "Automatic-Module-Name" to "thedarkcolour.kotlinforforge",
        "FMLModType" to "LIBRARY",
    )
}

// intermediary modloader-specific bundle JARs, which are combined into the final -all fatjar
for ((projectName, modules) in listOf(
    "kotlinforforge" to forgeProjects,
    "kotlinforforge-neoforge" to neoForgeProjects
)) {
    project(":$projectName") {
        apply(plugin = "java-library")
        apply(plugin = "maven-publish")
        apply(plugin = "org.jetbrains.kotlin.jvm")

        layout.buildDirectory.set(rootProject.layout.buildDirectory.dir(projectName))

        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(17))
            withSourcesJar()

            sourceSets["main"].java.setSrcDirs(emptyList<Any>())
            sourceSets["main"].resources.setSrcDirs(emptyList<Any>())
        }
        extensions.configure<KotlinJvmProjectExtension> {
            sourceSets["main"].kotlin.setSrcDirs(emptyList<Any>())
        }

        val allJar = tasks.register<Jar>("allJar") {
            configureAllJar(projectDir, modules.associate { module ->
                val name = "${module.removeSuffix("-neoforge")}-$kffVersion.jar"
                val jar = project(":$module").tasks.named<Jar>("jar")
                    .flatMap { it.archiveFile }

                name to jar
            })
        }
        tasks.named("assemble") {
            dependsOn(allJar)
        }

        // Only publish the "-all" jar
        configurations.named("apiElements") {
            artifacts.clear()
        }
        configurations.named("runtimeElements") {
            setExtendsFrom(emptySet())
            artifacts.clear()
            outgoing.artifact(allJar)
        }

        dependencies {
            for (module in modules) {
                "api"(project(":$module"))
            }
        }

        extensions.configure<PublishingExtension> {
            publications.register<MavenPublication>("maven") {
                suppressAllPomMetadataWarnings()
                from(components["java"])
            }
            repositories {
                maven(rootProject.file("site"))
            }
        }
    }
}

// combine the forge/neoforge version of each "kfflang", "kfflib", "kffmod"
fun registerCombinedJar(name: String, modType: String?): TaskProvider<Jar> {
    return tasks.register<Jar>("combined${name.replaceFirstChar(Char::uppercase)}Jar") {
        group = "kff"
        archiveBaseName.set(name)
        destinationDirectory.set(layout.buildDirectory.dir("combined"))
        // Forge copy of duplicate files (META-INF/*.kotlin_module, mods.toml) wins
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        for (projectName in listOf(name, "$name-neoforge")) {
            from(zipTree(project(":$projectName").tasks.named<Jar>("jar").flatMap { it.archiveFile }))
        }

        manifest.attributes(
            "Specification-Title" to "Kotlin for Forge",
            "Specification-Vendor" to "Forge",
            "Specification-Version" to "1",
            "Implementation-Title" to name,
            "Implementation-Version" to kffVersion,
            "Implementation-Vendor" to "thedarkcolour",
            "Implementation-Timestamp" to LocalDateTime.now(),
            "Automatic-Module-Name" to "thedarkcolour.kotlinforforge.$name",
        )
        if (modType != null) {
            manifest.attributes("FMLModType" to modType)
        }
    }
}

// contains both forge and neoforge classes, used to build the player-facing -all fatjar
val combinedJars = mapOf(
    "kfflang" to registerCombinedJar("kfflang", "LANGPROVIDER"),
    "kfflib" to registerCombinedJar("kfflib", "GAMELIBRARY"),
    "kffmod" to registerCombinedJar("kffmod", null),
)

val allJar = tasks.register<Jar>("combinedJar") {
    archiveBaseName.set("kotlinforforge")
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
    configureAllJar(
        "src/kotlinforforge/combined",
        combinedJars
            .mapKeys { (module, _) -> "$module-$kffVersion.jar" }
            .mapValues { (_, jar) -> jar.flatMap { it.archiveFile } }
    )
}

tasks.assemble {
    dependsOn(allJar)
}

//
// MOD PLATFORMS
//

val supportedMcVersions = listOf("1.19.3", "1.19.4", "1.20", "1.20.1", "1.20.2")

publishMods {
    file = allJar.flatMap { it.archiveFile }

    changelog = getChangelogText()
    type = STABLE
    modLoaders.addAll("forge", "neoforge")
    displayName = "Kotlin for Forge $kffVersion"

    curseforge {
        projectId = "351264"
        projectSlug = "kotlin-for-forge"
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        minecraftVersions.addAll(supportedMcVersions)
        javaVersions.add(JavaVersion.VERSION_17)
    }
    modrinth {
        projectId = "ordsPcFz"
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        minecraftVersions.addAll(supportedMcVersions)
    }
}

fun getChangelogText(): String {
    val file = file("changelog.md")
    if (!file.exists()) {
        return "Changelog not found"
    }

    // Relies on the changelog block being "##blahblahblah_VERSION" where _ is a space
    val content = file.readText().normaliseLineSeparators().split("##.* ")

    for (chunk in content) {
        if (chunk.isEmpty()) continue

        val lineTerminatorIndex = chunk.indexOfFirst { c -> c == '\n' || c == '\r' }
        val versionString = chunk.substring(0, lineTerminatorIndex)

        if (versionString == kffVersion) {
            return "## Kotlin for Forge $kffVersion\n${chunk.substring(lineTerminatorIndex + 1)}"
        }
    }

    // Fallback in case changelog was not provided
    return "Kotlin for Forge $kffVersion"
}
