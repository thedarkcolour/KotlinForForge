# KotlinForForge
Makes Kotlin forge-friendly.

To implement in your project, add the following to your build.gradle: 
```groovy
repositories {
    maven {
        name = 'kotlinforforge'
        url = 'https://thedarkcolour.github.io/KotlinForForge/'
    }
}

dependencies {
    implementation 'thedarkcolour:kotlinforforge:1+'
}
```
Then, add the following to your mods.toml file:
```toml
modLoader="kotlinforforge"
loaderVersion="[1,)"

[[dependencies.YOUR_MODID]]
    modId="kotlinforforge"
    mandatory=true
    versionRange="[1,)"
    ordering="NONE"
    side="BOTH"
```

Currently, this mod supports object declarations with @Mod and @EventBusSubscriber annotations.

It is recommended that you use   
```net.minecraftforge.registries.DeferredRegister```  
instead of           
```net.minecraftforge.registries.ObjectHolder```

You must use   
```thedarkcolour.kotlinforforge.KotlinModLoadingContext```              
instead of   
```net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext```
