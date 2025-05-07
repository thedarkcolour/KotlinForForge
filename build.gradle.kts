import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.matthewprenger.cursegradle.CurseArtifact
import net.neoforged.moddevgradle.tasks.JarJar
import org.gradle.api.publish.maven.internal.dependencies.MavenDependency
import org.gradle.api.publish.maven.internal.dependencies.MavenPomDependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.publish.maven.internal.publication.MavenPomInternal
import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers
import com.matthewprenger.cursegradle.CurseProject
import org.gradle.kotlin.dsl.support.normaliseLineSeparators

plugins {
    `java-library`
    `maven-publish`
    idea

    alias(libs.plugins.ideaext)
    alias(libs.plugins.shadow).apply(false)
    alias(libs.plugins.moddev).apply(false)
    alias(libs.plugins.kotlin)

    alias(libs.plugins.minotaur)
    alias(libs.plugins.cursegradle)
}

//
// BOILERPLATE
//

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

tasks.wrapper {
    distributionType = Wrapper.DistributionType.BIN
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
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
    filesMatching(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
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
    create("langNeoForge") {
        kotlin.srcDir("src/kfflang/neoforge/kotlin")
        resources.srcDir("src/kfflang/neoforge/resources")
    }
    create("langForge") {
        kotlin.srcDir("src/kfflang/forge/kotlin")
        resources.srcDir("src/kfflang/forge/resources")
    }

    // KFF Lib
    create("libCommon").kotlin.srcDir("src/kfflib/common/kotlin")
    create("libNeoForge").kotlin.srcDir("src/kfflib/neoforge/kotlin")
    create("libForge").kotlin.srcDir("src/kfflib/forge/kotlin")

    // KFF Mod
    create("modCommon").resources
        .srcDir("src/kffmod/common/resources")
        .srcDir(generateModMetadata)
    create("modNeoForge").kotlin.srcDir("src/kffmod/neoforge/kotlin")
    create("modForge").kotlin.srcDir("src/kffmod/forge/kotlin")
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.minecraftforge.net/")
}

dependencies {
    // fakecraft (fake class/field/method signatures used in place of the full Minecraft/Forge dependencies)
    "fakecraftCompileOnly"(libs.joml)
    "fakecraftCompileOnly"(libs.forge.core)
    "fakecraftCompileOnly"(libs.forge.bus)
    "fakecraftCompileOnly"(libs.night.config)

    // kfflib/common
    "libCommonCompileOnly"(libs.joml)

    // kfflib/neoforge
    "libNeoForgeCompileOnly"(sourceSets["langNeoForge"].output)

    // kfflib/forge
    "libForgeCompileOnly"(sourceSets["langForge"].output)

    // kffmod/neoforge
    "modNeoForgeCompileOnly"(libs.log4j.core)

    // kffmod/forge
    "modForgeCompileOnly"(libs.log4j.core)

    sourceSets.forEach { sourceSet ->
        val name = sourceSet.name
        val compileOnly = sourceSet.compileOnlyConfigurationName

        // Kotlin dep for all modules
        dependencies.add(compileOnly, libs.bundles.kotlin)

        // NeoForge/Forge dep
        if (name.contains("Forge")) {
            dependencies.add(compileOnly, if (name.contains("Neo")) libs.bundles.neoforge else libs.bundles.forge, closureOf<ModuleDependency> {
                isTransitive = false
            })
        }
        // FakeCraft dep for kfflib
        if (name.contains("lib")) {
            dependencies.add(compileOnly, sourceSets["fakecraft"].output)
        }
        // ASM and Log4j for kfflang
        if (name.contains("lang")) {
            dependencies.add(compileOnly, libs.asm)
            dependencies.add(compileOnly, libs.log4j.core)
        }
    }
}

//
// ARTIFACTS
//

// kfflang-neoforge
tasks.register<Jar>("langNeoForgeJar") {
    archiveBaseName.set("kfflang-neoforge")
    group = "kff"

    from(sourceSets["langNeoForge"].output)

    manifest {
        attributes(
            mapOf(
                "FMLModType" to "LIBRARY",
                "Implementation-Version" to version
            )
        )
    }
}

// kfflang-forge
tasks.register<Jar>("langForgeJar") {
    archiveBaseName.set("kfflang-forge")
    group = "kff"

    from(sourceSets["langForge"].output)

    manifest {
        attributes(
            mapOf(
                "Specification-Title" to "Kotlin for Forge",
                "Specification-Vendor" to "Forge",
                "Specification-Version" to "1",
                "Implementation-Title" to "kfflang",
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "thedarkcolour",
                "Automatic-Module-Name" to "thedarkcolour.kotlinforforge.lang",
                "FMLModType" to "LANGPROVIDER"
            )
        )
    }
}

// kfflib-neoforge
tasks.register<ShadowJar>("libNeoForgeJar") {
    archiveBaseName.set("kfflib-neoforge")
    group = "kff"

    from(sourceSets["libCommon"].output)
    from(sourceSets["libNeoForge"].output)

    // Move common lib into correct package
    relocate("thedarkcolour.kotlinforforge.forge", "thedarkcolour.kotlinforforge.neoforge.forge")
    relocate("thedarkcolour.kotlinforforge.kotlin", "thedarkcolour.kotlinforforge.neoforge.kotlin")

    manifest {
        attributes(mapOf("FMLModType" to "GAMELIBRARY"))
    }
}

// kfflib-forge
tasks.register<Jar>("libForgeJar") {
    archiveBaseName.set("kfflib-forge")
    group = "kff"

    from(sourceSets["libForge"].output)
    from(sourceSets["libCommon"].output)

    manifest {
        attributes(mapOf("FMLModType" to "GAMELIBRARY"))
    }
}

// kffmod-neoforge
tasks.register<Jar>("modNeoForgeJar") {
    archiveBaseName.set("kffmod-neoforge")
    group = "kff"

    from(sourceSets["modCommon"].output)
    from(sourceSets["modNeoForge"].output)

    exclude("META-INF/mods.toml")
}

// kffmod-forge
tasks.register<Jar>("modForgeJar") {
    archiveBaseName.set("kffmod-forge")
    group = "kff"

    from(sourceSets["modCommon"].output)
    from(sourceSets["modForge"].output)

    exclude("META-INF/neoforge.mods.toml")
}

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
            "kfflang" to "langForgeJar",
            "kfflib" to "libForgeJar",
            "kffmod" to "modForgeJar",
            "kfflang-neoforge" to "langNeoForgeJar",
            "kfflib-neoforge" to "libNeoForgeJar",
            "kffmod-neoforge" to "modNeoForgeJar",
        ).forEach { (name, jar) ->
            register<MavenPublication>(name.replace("-", "_")) {
                artifactId = name
                artifact(tasks.named<Jar>(jar))
            }
        }
        listOf("", "-neoforge").forEach { suffix ->
            register<MavenPublication>("kotlinforforge" + suffix.replace("-", "_")) {
                // FatJar the Kotlin libs
                artifactId = "kotlinforforge$suffix"
                artifact(tasks.named<Jar>("mavenJar"))

                // Generate Maven dependencies manually
                val dependencies = arrayListOf("kfflang", "kfflib", "kffmod")
                    .map { FakeMavenDependency("thedarkcolour", it + suffix, project.property("kff_version") as String) }
                    .plus(libs.bundles.kotlin.get()
                        .map { FakeMavenDependency(it.group!!, it.name, it.version!!) }
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
val supportedMcVersions = listOf("1.20.6", "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5")

val curseForgeToken: String? = System.getenv("CURSEFORGE_TOKEN")
if (!curseForgeToken.isNullOrBlank()) {
    curseforge {
        apiKey = curseForgeToken

        project(closureOf<CurseProject> {
            id = "351264"
            changelogType = "markdown"
            changelog = getChangelogText()
            releaseType = "release"

            gameVersionStrings.add("Forge")
            gameVersionStrings.add("NeoForge")
            gameVersionStrings.add("Java 21")
            gameVersionStrings.addAll(supportedMcVersions)

            mainArtifact(
                tasks.jar.get().archiveFile,
                closureOf<CurseArtifact> {
                    displayName = "Kotlin for Forge ${project.version}"
                }
            )
        })
    }
}

modrinth {
    projectId.set("ordsPcFz")
    versionName.set("Kotlin for Forge ${project.version}")
    versionNumber.set("${project.version}")
    versionType.set("release")
    gameVersions.addAll(supportedMcVersions)
    loaders.add("forge")
    loaders.add("neoforge")
    changelog = getChangelogText()
    uploadFile.set(tasks.jar)
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
