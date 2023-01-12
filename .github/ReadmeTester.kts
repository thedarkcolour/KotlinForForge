import java.io.File
import java.lang.IllegalStateException
import java.lang.ProcessBuilder.Redirect

val readmeContents = File("README.md").readLines()
val propertiesContents = File("gradle.properties").readLines()
val mcVersion = propertiesContents.find { it.trim().startsWith("mc_version") }!!.substringAfter("=").trim()
val forgeVersion = propertiesContents.find { it.trim().startsWith("forge_version") }!!.substringAfter("=").trim()
val kffVersion = propertiesContents.find { it.trim().startsWith("kff_version") }!!.substringAfter("=").trim()

fun main() {
    println("minecraft version: $mcVersion")
    println("forge version: $forgeVersion")

    "cp -n -r gradle .github/readmetester/".runCommand()
    "cp -n gradlew .github/readmetester/".runCommand()

    newRun()

    useGradle()
    applyReadme(isGroovy = true, readmeContents)
    testBuild()

    useKts()
    resetBuildEnv()
    applyReadme(isGroovy = false, readmeContents)
    testBuild()

    testTOML()
}

main()

fun newRun() {
    "rm -rf .github/readmetester/.gradle".runCommand()
    "rm -rf .github/readmetester/build*".runCommand()
}

fun applyReadme(isGroovy: Boolean, readme: List<String>) {
    val (readmePluginsSection, readmeReposSection, readmeDepsSection) = extractFromReadme(isGroovy, readme)

    val readmePluginsLines = readme.subList(readmePluginsSection.first + 1, readmePluginsSection.last)
    val readmeRepoLines = readme.subList(readmeReposSection.first + 1, readmeReposSection.last)
    val readmeDepsLines = readme.subList(readmeDepsSection.first + 1, readmeDepsSection.last)

    val buildGradle = File(if (isGroovy) ".github/readmetester/build.gradle" else ".github/readmetester/build.gradle.kts").readLines()

    val gradlePluginsSection = getRangeOfSection("plugins {", "}", buildGradle)
    val gradleReposSection = getRangeOfSection("repositories {", "}", buildGradle)
    val gradleDepsSection = getRangeOfSection("dependencies {", "}", buildGradle)

    val pluginsInsertTarget = gradlePluginsSection.last
    val reposInsertTarget = gradleReposSection.last + (readmePluginsSection.length() - 2)
    val depsInsertTarget = gradleDepsSection.last + (readmePluginsSection.length() - 2) + (readmeReposSection.length() - 2)

    // When printing, add 1 to printed line numbers as text editors usually starts line number at 1 and not 0
    println("README info")
    println("merging plugins (${readmePluginsSection.shift(1)}) into build script at $pluginsInsertTarget {")
    readmePluginsLines.forEach {
        println(it)
    }
    println("}")
    println("targeted build script line number: ${pluginsInsertTarget + 1}")
    println("merging repository (${readmeReposSection.shift(1)}) into build script at $reposInsertTarget {")
    readmeRepoLines.forEach {
        println(it)
    }
    println("}")
    println("merging dependencies (${readmeDepsSection.shift(1)}) into build script at $depsInsertTarget {")
    readmeDepsLines.forEach {
        println(it)
    }
    println("}")

    val newBuildGradle = ArrayList<String>(buildGradle.size + (readmePluginsSection.length() - 2) + (readmeReposSection.length() - 2) + (readmeDepsSection.length() - 2))
    newBuildGradle.addAll(buildGradle)

    // Lines are added backwards so reversing is necessary
    readmePluginsLines.asReversed().forEach {
        newBuildGradle.add(pluginsInsertTarget, it)
    }
    readmeRepoLines.asReversed().forEach {
        newBuildGradle.add(reposInsertTarget, it)
    }
    readmeDepsLines.asReversed().forEach {
        newBuildGradle.add(depsInsertTarget, it)
    }

    File(if (isGroovy) ".github/readmetester/build.gradle" else ".github/readmetester/build.gradle.kts").bufferedWriter().use { writer ->
        newBuildGradle.forEach { newLine ->
            writer.appendLine(newLine)
        }
    }
}

fun testBuild() {
    val exitValue = "./gradlew build".runCommand(File("./.github/readmetester"))
    require(exitValue == 0) { "Build test failed!" }
}

fun testTOML() {
    val tomlSection = getRangeOfSection("```toml", "```", readmeContents)
    val tomlLines = readmeContents.subList(tomlSection.first, tomlSection.last + 1)

    val modloaderLine = tomlLines
        .find { it.startsWith("modLoader") }!!
        .split('=')[1]
        .trim()
        .replace("\"", "")

    val loaderVersionLine = tomlLines
        .find { it.startsWith("loaderVersion") }!!
        .split('=')[1]
        .trim()
        .replace("\"", "")

    val rootProjectName = File("settings.gradle.kts")
        .readLines()
        .find { it.startsWith("rootProject.name") }!!
        .split("=")[1]
        .trim()
        .replace("\"", "")

    require(modloaderLine == rootProjectName) { "$modloaderLine != $rootProjectName" }

    val startInclusive = if (loaderVersionLine.startsWith('[')) true
        else if (loaderVersionLine.startsWith('(')) false
        else throw IllegalStateException("Failed to parse version range for toml!")

    val endInclusive = if (loaderVersionLine.endsWith(']')) true
        else if (loaderVersionLine.endsWith(')')) false
        else throw IllegalStateException("Failed to parse version range for toml!")

    val split = loaderVersionLine.split(',')

    val start = split[0].replace("[", "").replace("(", "")
    val end = split[1].replace("]", "").replace(")", "")

    when (compareVersion(start, kffVersion)) {
        -1 -> {
            println("specified kff version in toml is bigger than start")
        }
        0 -> {
            if (startInclusive) {
                println("specified kff version in toml is equal to start, allowed")
            } else {
                throw IllegalStateException("specified kff version in toml is equal to start, disallowed")
            }
        }
        1 -> {
            throw IllegalStateException("specified kff version in toml is less than start")
        }
    }

    when (compareVersion(kffVersion, end)) {
        -1 -> {
            println("specified kff version in toml is less than end")
        }
        0 -> {
            if (endInclusive) {
                println("specified kff version in toml is equal to end, allowed")
            } else {
                throw IllegalStateException("specified kff version in toml is equal to end, disallowed")
            }
        }
        1 -> {
            throw IllegalStateException("specified kff version in toml is bigger than end")
        }
    }
}

fun useGradle() {
    "rm -f .github/readmetester/build.gradle.kts".runCommand()
    "cp .github/readmetester/groovy .github/readmetester/build.gradle".runCommand()
    runSed("String mcVersion = MCVERSION", "String mcVersion = \"$mcVersion\"", ".github/readmetester/build.gradle")
    runSed("String forgeVersion = FORGEVERSION", "String forgeVersion = \"$forgeVersion\"", ".github/readmetester/build.gradle")
}

fun useKts() {
    "rm -f .github/readmetester/build.gradle".runCommand()
    "cp .github/readmetester/kts .github/readmetester/build.gradle.kts".runCommand()
    runSed("val mcVersion = MCVERSION", "val mcVersion = \"$mcVersion\"", ".github/readmetester/build.gradle.kts")
    runSed("val forgeVersion = FORGEVERSION", "val forgeVersion = \"$forgeVersion\"", ".github/readmetester/build.gradle.kts")
}

fun resetBuildEnv() {
    "rm -rf .github/readmetester/.gradle".runCommand()
    "rm -rf .github/readmetester/build".runCommand()
}

fun runSed(old: String, new: String, target: String) {
    val args = arrayOf("sed",  "-i", "s/$old/$new/g", target)
    ProcessBuilder(*args).directory(File("./"))
        .redirectOutput(Redirect.INHERIT)
        .redirectError(Redirect.INHERIT)
        .start()
        .waitFor()
}

fun String.runCommand(workingDir: File = File("./")): Int {
    return ProcessBuilder(*split(' ').toTypedArray())
        .directory(workingDir)
        .redirectOutput(Redirect.INHERIT)
        .redirectError(Redirect.INHERIT)
        .start()
        .waitFor()
}

fun getRangeOfSection(head: String, tail: String, searchIn: List<String>, searchRange: IntRange = 0..searchIn.lastIndex): IntRange {
    var start = -1
    var end = -1
    for ((index, line) in searchIn.withIndex()) {
        if (index < searchRange.first) continue
        val trimmed = line.trimEnd()
        if (trimmed.startsWith(head)) {
            start = index
            continue
        }
        if (start != -1) {
            if (trimmed.startsWith(tail)) {
                end = index
                break
            }
        }

        if (index > searchRange.last) break
    }
    require(start != -1) { "Unable to find start position!" }
    require(end != -1) { "Unable to find end position!" }
    require((start..end).length() > 0) { "Section is empty!" }

    return start..end
}

fun extractFromReadme(isGroovy: Boolean, readme: List<String>): Triple<IntRange, IntRange, IntRange> {
    val range = getRangeOfSection(if (isGroovy) "```groovy" else "```kotlin", "```", readme)

    return Triple(
        getRangeOfSection("plugins {", "}", readme, range),
        getRangeOfSection("repositories {", "}", readme, range),
        getRangeOfSection("dependencies {", "}", readme, range)
    )
}

fun IntRange.length(): Int {
    val toReturn = endInclusive - start + 1
    require(toReturn > 0) { "This is empty range!" }
    return toReturn
}

fun IntRange.shift(n: Int): IntRange {
    return (first+n)..(last+n)
}

fun compareVersion(version1: String, version2: String): Int {
    if (version1 == version2) return 0
    if (version1.isEmpty()) return -1
    if (version2.isEmpty()) return -1

    val split1 = version1.split(".")
    val split2 = version2.split(".")

    val n1 = split1[0].toInt()
    val n2 = split2[0].toInt()

    if (n1 > n2) return 1
    if (n2 > n1) return -1

    if(split1.size == 1 && split2.size == 1) return 0
    if (split1.size == 1) return -1
    if (split2.size == 1) return 1
    return compareVersion(
        split1.subList(1, split1.size).joinToString("."),
        split2.subList(1, split2.size).joinToString("."),
    )
}
