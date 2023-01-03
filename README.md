# KotlinForForge
**\*\*These instructions are for 1.18-1.19. To see instructions for 1.14-1.16, [click here](https://github.com/thedarkcolour/KotlinForForge/blob/1.x/README.md).**

Makes Kotlin Forge-friendly by doing the following:
- Provides Kotlin stdlib, reflection, JSON serialization, and coroutines libraries.
- Provides `KotlinLanguageProvider` to allow usage of object declarations as @Mod targets.
- Provides `AutoKotlinEventBusSubscriber` to allow usage of object declarations as @Mod.EventBusSubscriber targets.
- Provides useful utility functions and constants

An example mod is provided at the [KotlinModdingSkeleton repository](https://github.com/thedarkcolour/KotlinModdingSkeleton/tree/1.19.x).

If you aren't sure where to start, make a fork of the KotlinModdingSkeleton repository.
```git
git clone --branch 1.19.x https://github.com/thedarkcolour/KotlinModdingSkeleton.git
```

To implement in an existing project, merge the following into your build.gradle:
```groovy
plugins {
    // Adds the Kotlin Gradle plugin
    id 'org.jetbrains.kotlin.jvm' version '1.7.20'
    // OPTIONAL Kotlin Serialization plugin
    id 'org.jetbrains.kotlin.plugin.serialization' version '1.7.20'
}

repositories {
    // Add KFF Maven repository
    maven {
        name = 'Kotlin for Forge'
        url = 'https://thedarkcolour.github.io/KotlinForForge/'
    }
}

dependencies {
    // Adds KFF as dependency and Kotlin libs
    implementation 'thedarkcolour:kotlinforforge:3.8.0'
}
```
Then, change the following to your mods.toml file:
```toml
modLoader="kotlinforforge"
# Change this if you require a certain version of KotlinForForge
loaderVersion="[3,)"
```

Use
```thedarkcolour.kotlinforforge.forge.MOD_CONTEXT```              
instead of ```net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext```
