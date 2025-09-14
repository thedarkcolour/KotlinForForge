plugins {
    id("kff.common-conventions")
    alias(libs.plugins.neogradle) apply false
}

// maven.repo.local is set within the Julia script in the website branch
tasks.register("publishAllMavens") {
    dependsOn(":forge:publishAllMavens")
    dependsOn(":neoforge:publishAllMavens")
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
