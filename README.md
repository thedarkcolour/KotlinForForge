# KotlinForForge
Makes Kotlin forge-friendly by doing the following:
- Provides the Kotlin libraries.
- Provides `KotlinLanguageProvider` to allow usage of object declarations as @Mod targets.
- Provides `AutoKotlinEventBusSubscriber` to allow usage of object declarations as @Mod.EventBusSubscriber targets.
- Provides useful top-level utility functions and constants

To implement in your project, add the following to your build.gradle: 
```groovy
buildscript {
    // ...
    dependencies {
        // Make sure to use the correct version
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61"
    }
}

apply plugin: 'kotlin'

repositories {
    maven {
        name = 'kotlinforforge'
        url = 'https://thedarkcolour.github.io/KotlinForForge/'
    }
}

dependencies {
    // Uses the latest version of KotlinForForge
    implementation 'thedarkcolour:kotlinforforge:1+'
}

compileKotlin {
    kotlinOptions {
        jvmTarget = '1.8'
    }
}
```
Then, add the following to your mods.toml file:
```toml
modLoader="kotlinforforge"
loaderVersion="[1,)"
```

Use
```thedarkcolour.kotlinforforge.KotlinModLoadingContext```              
instead of   
```net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext```
