import java.io.File
import java.lang.ProcessBuilder.Redirect
import java.net.URL

val readmeContents = File("README.md").readLines()
val propertiesContents = File("gradle.properties").readLines()
val mcVersion = propertiesContents.find { it.trim().startsWith("mc_version") }!!.substring(11)
val forgeVersion = propertiesContents.find { it.trim().startsWith("forge_version") }!!.substring(14)

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
