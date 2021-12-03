# KotlinForForge
**\*\*These instructions are for 1.18. To see instructions for 1.14-1.16, [click here](https://github.com/thedarkcolour/KotlinForForge/blob/1.x/README.md).**

Makes Kotlin Forge-friendly by doing the following:
- Provides Kotlin stdlib, reflection, JSON serialization, and coroutines libraries.
- Provides `KotlinLanguageProvider` to allow usage of object declarations as @Mod targets.
- Provides `AutoKotlinEventBusSubscriber` to allow usage of object declarations as @Mod.EventBusSubscriber targets.
- Provides useful utility functions and constants
- Provides sided property delegates and object holder property delegates

An example mod is provided at the [KotlinModdingSkeleton repository](https://github.com/thedarkcolour/KotlinModdingSkeleton).

If you aren't sure where to start, make a fork of the KotlinModdingSkeleton repository.
```git
git clone --branch 1.18.x https://github.com/thedarkcolour/KotlinModdingSkeleton.git
```

To implement in an existing project, paste the following into your build.gradle:
```groovy
// Adds the Kotlin Gradle plugin
buildscript {
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.0"
        // OPTIONAL Kotlin Serialization plugin
        classpath 'org.jetbrains.kotlin:kotlin-serialization:1.6.0'
    }
}
apply plugin: 'kotlin'
// OPTIONAL Kotlin Serialization plugin
apply plugin: 'kotlinx-serialization'

// Adds KFF as dependency and Kotlin libs to the runtime classpath
// If you already know how to add the Kotlin plugin to Gradle, this is the only line you need for KFF
apply from: 'https://raw.githubusercontent.com/thedarkcolour/KotlinForForge/site/thedarkcolour/kotlinforforge/gradle/kff-3.0.0.gradle'

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
