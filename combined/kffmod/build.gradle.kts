import java.time.LocalDateTime

plugins {
    java
}

val kff_version: String by project

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from(provider {
            listOf(
                zipTree((project(":forge:kffmod").tasks.getByName("jar") as Jar).archiveFile),
                zipTree((project(":neoforge:kffmod").tasks.getByName("jar") as Jar).archiveFile),
            )
        })

        manifest {
            attributes(
                "Specification-Title" to "Kotlin for Forge",
                "Specification-Vendor" to "Forge",
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "thedarkcolour",
                "Implementation-Timestamp" to LocalDateTime.now(),
                "Automatic-Module-Name" to "thedarkcolour.kotlinforforge.mod",
            )
        }
    }
}
