plugins {
    id("net.neoforged.gradle.userdev") version "[7.0,8.0)"
    id("com.modrinth.minotaur") version "2.+"
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
val min_forge_version: String by project
val min_neo_version: String by project

val replacements: MutableMap<String, Any> = mutableMapOf(
    "min_mc_version" to min_mc_version,
    "unsupported_mc_version" to unsupported_mc_version,
    "min_forge_version" to min_forge_version,
    "min_neo_version" to min_neo_version,
    "kff_version" to kff_version
)
val targets = mutableListOf("META-INF/mods.toml")

subprojects {
    apply(plugin = "java")
    tasks {
        withType<ProcessResources> {
            inputs.properties(replacements)

            filesMatching(targets) {
                expand(replacements)
            }
        }
    }
}

val supportedMcVersions = listOf("1.19.3", "1.19.4", "1.20", "1.20.1", "1.20.2")

curseforge {
    // Use the command line on Linux because IntelliJ doesn't pick up from .bashrc
    apiKey = System.getenv("CURSEFORGE_API_KEY") ?: "no-publishing-allowed"

    project(closureOf<com.matthewprenger.cursegradle.CurseProject> {
        id = "351264"
        releaseType = "release"
        gameVersionStrings.add("Forge")
        gameVersionStrings.add("NeoForge")
        gameVersionStrings.add("Java 17")
        gameVersionStrings.addAll(supportedMcVersions)

        // from Modrinth's Util.resolveFile
        @Suppress("DEPRECATION")
        mainArtifact(project(":combined").tasks.jarJar.get().archivePath, closureOf<com.matthewprenger.cursegradle.CurseArtifact> {
            displayName = "Kotlin for Forge ${project.version}"
        })
    })
}

modrinth {
    projectId.set("ordsPcFz")
    versionName.set("Kotlin for Forge ${project.version}")
    versionNumber.set("${project.version}")
    versionType.set("release")
    gameVersions.addAll(supportedMcVersions)
    loaders.add("forge")
    loaders.add("neoforge")
    uploadFile.provider(project(":combined").tasks.jarJar)
}

// maven.repo.local is set within the Julia script in the website branch
tasks.create("publishAllMavens") {
    for (proj in arrayOf(":forge", ":neoforge")) {
        finalizedBy(project(proj).tasks.getByName("publishAllMavens"))
    }
}
tasks.create("publishModPlatforms") {
    finalizedBy(tasks.create("printPublishingMessage") {
        this.doFirst {
            println("Publishing Kotlin for Forge $kff_version to Modrinth and CurseForge")
        }
    })
    finalizedBy(tasks.modrinth)
    finalizedBy(tasks.curseforge)
}

task<Exec>("testREADME") {
    group = "verification"
    description = "Applies steps in README to ensure it works on mdk"
    workingDir("./")
    commandLine("kotlinc", "-script", ".github/ReadmeTester.kts")
    doLast {
        executionResult.get().assertNormalExitValue()
    }
}

// Kotlin function ambiguity fix
fun <T> Property<T>.provider(value: T) {
    set(value)
}
