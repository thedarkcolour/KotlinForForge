# KotlinForForge
Makes Kotlin forge-friendly by doing the following:
- Provides the Kotlin libraries.
- Provides `KotlinLanguageProvider` to allow usage of object declarations as @Mod targets.
- Provides `AutoKotlinEventBusSubscriber` to allow usage of object declarations as @Mod.EventBusSubscriber targets.
- Provides useful utility functions and constants

To implement in your project, paste the following into your build.gradle:
```groovy
buildscript {
    repositories {
        // For early access Kotlin versions
        maven { url = "https://dl.bintray.com/kotlin/kotlin-eap" }
    }
    dependencies {
        // Make sure to use the correct version
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.4-M1"
    }
}

apply plugin: 'kotlin'

repositories {
    maven {
        name = "Kotlin Early Access"
        url = "https://dl.bintray.com/kotlin/kotlin-eap"
    }
    maven {
        name = 'kotlinforforge'
        url = 'https://thedarkcolour.github.io/KotlinForForge/'
    }
}

dependencies {
    // Uses the latest version of KotlinForForge
    implementation 'thedarkcolour:kotlinforforge:1.2.1'
}

compileKotlin {
    // Needed if you use Forge.kt
    kotlinOptions {
        jvmTarget = '1.8'
        languageVersion = '1.4'
        apiVersion = '1.4'
    }
    
    // Required to run in dev environment
    copy {
        from "$buildDir/classes/kotlin/main" into "$buildDir/classes/java/main"
    }
}
```
Then, change the following to your mods.toml file:
```toml
modLoader="kotlinforforge"
# Change this if you require a certain version of KotlinForForge
loaderVersion="[1,)"
```

Use
```thedarkcolour.kotlinforforge.forge.MOD_CONTEXT```              
instead of ```net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext```
