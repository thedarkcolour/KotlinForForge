# KotlinForForge
Makes Kotlin forge-friendly.

To implement in your project, add the following to your build.gradle: 
```
repositories {
    maven {
        name = "kotlinforforge"
        url = "https://cdn.jsdelivr.net/gh/thedarkcolour/KotlinForForge@master/"
    }
}

dependencies {
    implementation 'thedarkcolour:kotlinforforge:0.1.14'
}
```
Then, in your mods.toml file, change modLoader to "kotlinforforge".

Currently, this mod allows you to have @Mod object declarations and @Mod.EventBusSubscriber declarations.
