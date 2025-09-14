import thedarkcolour.kotlinforforge.plugin.getPropertyString
import java.time.LocalDateTime

project.plugins.apply(JavaPlugin::class)

project.version = getPropertyString("kff_version")
project.group = "thedarkcolour"

project.tasks.withType<Jar> {
    // get rid of duplicate files in the ZIP
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    val subproject = project.name.substringAfterLast(':')

    from(provider {
        listOf(
            zipTree(project(":forge:$subproject").tasks.getByName("jar", Jar::class).archiveFile),
            zipTree(project(":neoforge:$subproject").tasks.getByName("jar", Jar::class).archiveFile),
        )
    })
}
