import net.neoforged.moddevgradle.tasks.JarJar
import org.gradle.api.publish.maven.internal.dependencies.MavenDependency
import org.gradle.api.publish.maven.internal.dependencies.MavenPomDependencies
import org.gradle.api.publish.maven.internal.publication.MavenPomInternal
import org.gradle.kotlin.dsl.support.normaliseLineSeparators
import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `maven-publish`
    idea

    alias(libs.plugins.ideaext)
    alias(libs.plugins.moddev).apply(false)
    alias(libs.plugins.kotlin)

    alias(libs.plugins.mod.publish.plugin)
}

//
// BOILERPLATE
//

java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))

tasks.wrapper {
    distributionType = Wrapper.DistributionType.BIN
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    compilerOptions.freeCompilerArgs.addAll(listOf("-Xexplicit-api=warning", "-Xjvm-default=all"))
}

// Make IntelliJ download sources and Javadoc
idea.module {
    isDownloadSources = true
    isDownloadJavadoc = true
}

//
// PROJECT PROPERTIES
//

base.archivesName.set("kotlinforforge")
version = project.property("kff_version")!!
group = "thedarkcolour"

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val replacements = mapOf(
        "min_mc_version" to project.property("min_mc_version"),
        "unsupported_mc_version" to project.property("unsupported_mc_version"),
        "kff_version" to project.property("kff_version"),
        "kff_max_version" to project.property("kff_max_version"),
    )
    inputs.properties(replacements)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(replacements)
    }
    from("src/kffmod/templates")
    into("build/generated/sources/modMetadata")
}

// Regenerate mods.toml files after every IntelliJ refresh using idea-ext plugin
idea.project.settings.taskTriggers.afterSync(generateModMetadata)

//
// SOURCE SETS
//

sourceSets {
    // Allows compiling against Minecraft class (ex. Vec3) without needing entire Minecraft artifact
    create("fakecraft").java.srcDir("src/fakecraft/java")

    // KFF Lang
    create("lang") {
        kotlin.srcDir("src/kfflang/kotlin")
        resources.srcDir("src/kfflang/resources")
    }

    // KFF Lib
    create("lib").kotlin.srcDir("src/kfflib/kotlin")

    // KFF Mod
    create("mod") {
        kotlin.srcDir("src/kffmod/kotlin")
        resources
            .srcDir("src/kffmod/resources")
            .srcDir(generateModMetadata)
    }
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
}

dependencies {
    // fakecraft (fake class/field/method signatures used in place of the full Minecraft/Forge dependencies)
    "fakecraftCompileOnly"(libs.joml)
    "fakecraftCompileOnly"(libs.night.config)

    // kfflang
    "langCompileOnly"(libs.asm)
    "langCompileOnly"(libs.log4j.core)

    // kfflib
    "libCompileOnly"(libs.joml)
    "libCompileOnly"(sourceSets["fakecraft"].output)
    "libCompileOnly"(sourceSets["lang"].output)

    // kffmod
    "modCompileOnly"(libs.log4j.core)

    for (name in listOf("lang", "lib", "mod")) {
        val sourceSet = sourceSets[name]
        val compileOnly = sourceSet.compileOnlyConfigurationName

        dependencies.add(compileOnly, libs.bundles.kotlin)
        dependencies.add(compileOnly, libs.bundles.neoforge, closureOf<ModuleDependency> {
            isTransitive = false
        })
        // NeoForge 26.3+ is only published for Java 25, but KFF still compiles to JVM 21 bytecode for backwards compat
        configurations.named(sourceSet.compileClasspathConfigurationName) {
            attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
        }
    }
}

//
// ARTIFACTS
//

inline fun <reified J : Jar> registerArtifact(taskName: String, baseName: String, vararg sourceSetNames: String, crossinline configure: J.() -> Unit) {
    tasks.register<J>(taskName) {
        archiveBaseName.set(baseName)
        group = "kff"

        for (sourceSetName in sourceSetNames) {
            from(sourceSets[sourceSetName].output)
        }

        configure()
    }
    tasks.register<J>(taskName + "Sources") {
        archiveBaseName.set(baseName)
        archiveClassifier.set("sources")
        group = "kff"

        for (sourceSetName in sourceSetNames) {
            from(sourceSets[sourceSetName].allSource)
        }

        configure()
    }
}

// kfflang-neoforge
registerArtifact<Jar>("langJar", "kfflang-neoforge", "lang") {
    manifest {
        attributes(
            mapOf(
                "FMLModType" to "LIBRARY",
                "Implementation-Version" to version
            )
        )
    }
}

// kfflib-neoforge
registerArtifact<Jar>("libJar", "kfflib-neoforge", "lib") {
    manifest {
        attributes(mapOf("FMLModType" to "GAMELIBRARY"))
    }
}

// kffmod-neoforge
registerArtifact<Jar>("modJar", "kffmod-neoforge", "mod") {}

//
// JAR IN JAR
//

// JarJar of combined kfflang, kfflib, kffmod, and Kotlin libs (CurseForge release)
JarJar.registerWithConfiguration(project, "jarJar").configure {
    group = "kff"

    dependencies.add("jarJar", projects.combined.kfflang)
    dependencies.add("jarJar", projects.combined.kfflib)
    dependencies.add("jarJar", projects.combined.kffmod)
    dependencies.add("jarJar", libs.bundles.kotlin)
}
tasks.jar {
    from(tasks.named("jarJar"))
    manifest.attributes("FMLModType" to "LIBRARY")
    archiveClassifier.set("all")
}

// JarJar of Kotlin libs (Maven fatjar)
JarJar.registerWithConfiguration(project, "mavenJarJar").configure {
    group = "kff"

    dependencies.add("mavenJarJar", libs.bundles.kotlin)
}
tasks.register<Jar>("mavenJar") {
    from(tasks.named("mavenJarJar"))
    manifest.attributes("FMLModType" to "LIBRARY")
}

//
// MAVEN
//

publishing {
    publications {
        mapOf(
            "kfflang-neoforge" to "langJar",
            "kfflib-neoforge" to "libJar",
            "kffmod-neoforge" to "modJar",
        ).forEach { (name, jar) ->
            register<MavenPublication>(name.replace("-", "_")) {
                artifactId = name
                artifact(tasks.named<Jar>(jar))
                artifact(tasks.named<Jar>(jar + "Sources"))
            }
        }
        listOf("-neoforge").forEach { suffix ->
            register<MavenPublication>("kotlinforforge" + suffix.replace("-", "_")) {
                // FatJar the Kotlin libs
                artifactId = "kotlinforforge$suffix"
                artifact(tasks.named<Jar>("mavenJar"))

                // Generate Maven dependencies manually
                // Multiplatform kotlinx libraries publish their JVM jars as separate "-jvm" artifacts. Maven can't
                // read Gradle module metadata, so the POM must reference those directly
                val jvmArtifactIds = mapOf(
                    "kotlinx-coroutines-core" to "kotlinx-coroutines-core-jvm",
                    "kotlinx-serialization-core" to "kotlinx-serialization-core-jvm",
                    "kotlinx-serialization-json" to "kotlinx-serialization-json-jvm",
                )
                val dependencies = arrayListOf("kfflang", "kfflib", "kffmod")
                    .map { FakeMavenDependency("thedarkcolour", it + suffix, project.property("kff_version") as String) }
                    .plus(libs.bundles.kotlin.get()
                        .map { FakeMavenDependency(it.group!!, jvmArtifactIds[it.name] ?: it.name, it.version!!) }
                        .distinct()
                    )

                pom {
                    // Hacky way to add dependencies to the Maven POM (not sure if there's a better way to do this)
                    (this as MavenPomInternal).dependencies.set(object : MavenPomDependencies {
                        override fun getDependencies(): List<MavenDependency> = dependencies
                        override fun getDependencyManagement(): List<MavenDependency> = emptyList()
                    })
                }
            }
        }
    }

    repositories {
        maven("file://${project.projectDir}/site")
        mavenLocal()
    }
}

data class FakeMavenDependency(private val groupId: String, private val artifactId: String, private val version: String) : MavenDependency {
    override fun getGroupId() = groupId
    override fun getArtifactId() = artifactId
    override fun getVersion() = version
    override fun getType() = null
    override fun getClassifier() = null
    override fun getScope() = "compile"
    override fun getExcludeRules() = emptySet<ExcludeRule>()
    override fun isOptional() = false
}

//
// MOD PLATFORMS
//
val supportedMcVersions = listOf("1.21.9", "1.21.10", "1.21.11", "26.1", "26.1.1", "26.1.2", "26.2", "26.3")

publishMods {
    file = tasks.jar.get().archiveFile

    changelog = getChangelogText()
    type = STABLE
    modLoaders.addAll("neoforge")
    displayName = "Kotlin for Forge ${project.version}"

    curseforge {
        projectId = "351264"
        projectSlug = "kotlin-for-forge"
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        minecraftVersions.addAll(supportedMcVersions)
        client = true
        server = true
    }
    modrinth {
        projectId = "ordsPcFz"
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        minecraftVersions.addAll(supportedMcVersions)
    }
}

fun getChangelogText(): String {
    val version = project.property("kff_version").toString()

    val file = File("changelog.md")
    if (!file.exists()) {
        return "Changelog not found"
    }

    // Relies on the changelog block being "##blahblahblah_VERSION" where _ is a space
    val content = file.readText().normaliseLineSeparators().split("##.* ")

    for (chunk in content) {
        if (chunk.isEmpty()) continue

        val lineTerminatorIndex = chunk.indexOfFirst { c -> c == '\n' || c == '\r' }
        val versionString = chunk.substring(0, lineTerminatorIndex)

        if (versionString == version) {
            return "## Kotlin for Forge $version\n${chunk.substring(lineTerminatorIndex + 1)}"
        }
    }

    // Fallback in case changelog was not provided
    return "Kotlin for Forge $version"
}
