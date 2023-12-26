## Kotlin for Forge 4.10.0
- Update to Kotlin 1.9.22
- Address changes in NeoForge FancyModLoader (added type field to the mods.toml dependencies)

## Kotlin for Forge 4.9.0
- Removed CapabilityUtil.kt from NeoForged version of KFF due to NeoForge's capability changes
- Fixed a crash with new Forge changes. KFF 4.x is still compatible with Forge 1.19.3-1.20.4 and NeoForge 1.20.1-1.20.4.

## Kotlin for Forge 4.8.0
- Update to Kotlin 1.9.21, coroutines 1.6.2
- Fixed a crash with new NeoForge changes

## Kotlin for Forge 4.7.0
- Fixed incompatibility with NeoForge's new registry system
- You MUST USE this import for property delegates to work with DeferredRegister: `import thedarkcolour.kotlinforforge.neoforge.forge.getValue`

## Kotlin for Forge 4.6.2
- Fixed mod event bus not working properly on NeoForge

## Kotlin for Forge 4.6.1
- Fixed language provider crashing for non-object Mod classes

## Kotlin for Forge 4.6.0
- Updated to Kotlin 1.9.20
- Added support for NeoForge 1.20.2 (must use the "kotlinforforge-neoforge" artifact on Maven instead of just "kotlinforforge")

## Kotlin for Forge 4.5.0
- Updated to Kotlin 1.9.10, serialization 1.6.0, coroutines 1.7.3

## Kotlin for Forge 4.4.0
- Updated to Kotlin 1.9.0, coroutines 1.7.2

## Kotlin for Forge 4.3.0
- Now supports Minecraft 1.20 in addition to 1.19.3 and 1.19.4 (close #72)
- Updated to Kotlin 1.8.22, serialization 1.5.1, coroutines 1.7.1
- Compatibility with Catalogue mod (close #70)

## Kotlin for Forge 4.2.0
- Updated to Kotlin 1.8.21

## Kotlin for Forge 4.1.0
- Supports 1.19.4 as well as 1.19.3
- Updated to serialization 1.5.0

## Kotlin for Forge 4.0.0
- Supports 1.19.3
- Updated to Kotlin 1.8.10

## Kotlin for Forge 3.10.0
- Add utility methods for working with Mojang's Vector classes

## ~~Kotlin for Forge 3.9.2~~
~~- Add annotations back to the compiled JAR, hopefully the last bugfix update after switching to JarJar~~

## Kotlin for Forge 3.9.1
- Fix version range on kffmod, otherwise identical to 3.9.0

## Kotlin for Forge 3.9.0
- Updated to Kotlin 1.8.0

## Kotlin for Forge 3.8.0
- Updated to Kotlin 1.7.20, serialization 1.4.1

## Kotlin for Forge 3.7.1
- Updated to Kotlin 1.7.10, coroutines 1.6.4
- Fixed incompatibility with Forge 1.19.2 43.0.1+

## Kotlin for Forge 3.6.0
- Updated to Kotlin 1.7.0

## Kotlin for Forge 3.5.0
- Switch logging markers to work with 1.19
- Now works with 1.18 - 1.19

## Kotlin for Forge 3.4.0
- Updated to coroutines 1.6.2, serialization 1.3.3
- Changed KotlinModContainer to fix an issue with LuckPerms

## Kotlin for Forge 3.3.2
- Bundle JetBrains annotations 23.0.0 again because Forge does not bundle it in production

## Kotlin for Forge 3.3.1
- Removed multiple generic type bounds on `registerObject` because it was causing errors

## Kotlin for Forge 3.3.0
- Updated to Kotlin 1.6.21
- `registerObject` now supports Deferred Registers with vanilla registry types
  because Forge supports those types of registries as of 40.1.0

## Kotlin for Forge 3.2.0
- Updated to Kotlin 1.6.20, coroutines 1.6.1

## Kotlin for Forge 3.1.0
- Updated to Kotlin 1.6.10, coroutines 1.6.0, serialization 1.3.2
- Fix Java module issue with ObjectHolderDelegates
- Fix sources jar in gradle

## Kotlin for Forge 3.0.0
- Updated to 1.18
- Removed JetBrains annotations from bundled dependencies because Forge now includes it

## Kotlin for Forge 2.2.0
- Fix generic types with `registerObject` to work like they used to
- Go back to using slim jars on maven (todo update the readme)
- You will now need to manually add kotlin in your `build.gradle`

## Kotlin for Forge 2.1.2
- Another attempt to fix @EventBusSubscriber

## Kotlin for Forge 2.1.1
- ~~Actually fixed bug with @EventBusSubscriber~~

## Kotlin for Forge 2.1.0
- Fixed bug with @EventBusSubscriber (from unpublished version 2.0.2)
- Updated to Kotlin 1.5.31, Jetbrains annotations 23.0.0, serialization 1.3.1
- Change `registerObject` to return an interface subclassing property delegate and supplier

## Kotlin for Forge 2.0.2
- Fixed bug with @EventBusSubscriber

## Kotlin for Forge 2.0.1
- Updated to Kotlin 1.5.31, serialization 1.3.0
- Maven now has fat jar
- Removed KDeferredRegister and replaced with an extension function for property delegates
- Inlined a bunch of Forge.kt functions because KFF can't reference game code anymore
- Removed KotlinEventBus

## Kotlin for Forge 2.0.0
- WILL NOT WORK WITH 1.16 AND PRIOR, 1.x.x will be maintained for those versions
- Updated to work with Forge 1.17.x
- KDeferredRegister now (hopefully) supports modded registries
- ObjectHolderDelegate has slightly more helpful error messages when a value is not present
- Cleaned up legacy code from 1.14-1.16
- Removed some deprecated functions

## Kotlin for Forge 1.15.1
- Fixed missing dependencies on Curseforge

## Kotlin for Forge 1.15.0
- Updated to Kotlin 1.5.30, JetBrains annotations 22.0.0, coroutines 1.5.2

## Kotlin for Forge 1.14.0
- Updated to Kotlin 1.5.21, JetBrains annotations 21.0.1, coroutines 1.5.1, serialization 1.2.2
- Fixed maven repo to contain the latest versions

## Kotlin for Forge 1.13.0
- Updated to Kotlin 1.5.20

## Kotlin for Forge 1.12.2
- Added support for using Mod.EventBusSubscriber as a file annotation. See AutoKotlinEventBusSubscriber.kt KDoc for example.
- Made registryName and registry properties of ObjectHolderDelegate public.

## Kotlin for Forge 1.12.1
- Updated to Kotlin 1.5.10, Updated to coroutines 1.5.0, Updated to serialization 1.2.1
- Added a missing serialization library that went missing in 1.12.0
- Finally removed the `MINECRAFT` thing that was deprecated a while ago
- Improved the buildscript and fixed some deprecation warnings, thanks Username404-59 on GitHub

## Kotlin for Forge 1.12.0
- Updated to Kotlin 1.5.0, Updated to coroutines 1.5.0-RC, updated to serialization 1.2.0
- Forge Gradle 4 and Kotlin Gradle plugin now require Gradle 6.8.1. Update by changing the version of the gradle wrapper in `gradle/wrapper/gradle-wrapper.properties`.
- Added a missing coroutines module that caused an issue in KFF 1.11.0

## Kotlin for Forge 1.11.1
- Downgraded to coroutines 1.4.2 due to missing class errors

## Kotlin for Forge 1.11.0
- Updated to Kotlin 1.4.32, Updated to coroutines 1.4.3

## Kotlin for Forge 1.10.0
- Updated to Kotlin 1.4.31, Updated to serialization 1.1.0

## Kotlin for Forge 1.9.0
- Updated to Kotlin 1.4.30

## Kotlin for Forge 1.8.0
- Fixed a few things to match the new EventBus version.
- Fix #11 and #12. Event bus wrapper now posts events properly.
- Now includes the Kotlin JSON Serialization 1.0.1 library.

## Kotlin for Forge 1.7.0
- Added `registerObject` function to KDeferredRegister for getting ObjectHolderDelegate instances
  without needing a cast to ObjectHolderDelegate.
- Deprecated `register` in KDeferredRegister
- Fixed KReflect sometimes not showing up on the Maven.
- Updated to Kotlin 1.4.21, Updated to coroutines 1.4.2, Updated to JetBrains annotations 20.1.0 

## Kotlin for Forge 1.6.2
- Fixed errors in KotlinEventBus with certain Lambda syntax

## Kotlin for Forge 1.6.1
- Removed inline modifier for functions `runForDist`, `runWhenOn`, and `callWhenOn` in Forge.kt.
- Changed the `AutoKotlinEventBusSubscriber` to not crash when loading client only subscribers with client only members.
- Deprecated `MINECRAFT` and set deprecation level to error. This is because the property only works on the version KFF was compiled with.

## Kotlin for Forge 1.6.0
- Updated to support changes in the Forge API in 1.16.2 and 1.16.3 (KFF should no longer cause crashes)
- Updated to Kotlin 1.4.10

## Kotlin for Forge 1.5.0
- Updated to Kotlin 1.4.0

## Kotlin for Forge 1.4.1
- Fixed `KDeferredRegistry` registering things out of order

## Kotlin for Forge 1.4.0
- Kotlin Gradle Plugin now requires Gradle 5.3. Update by changing the version of the gradle wrapper in `gradle/wrapper/gradle-wrapper.properties`.
- Added a `KDeferredRegistry` similar to Forge's `DeferredRegistry` but works with `ObjectHolderDelegate`s instead of `RegistryObject`s.
- Fixed a typo in the KDoc for `MOD_BUS` that falsely stated `AttachCapabilitiesEvent` was fired on the mod-specific event bus. 
- Updated to Kotlin 1.4.0-rc

## Kotlin for Forge 1.3.1
- Bumped version range to work with 1.16 Forge when it comes out.

## Kotlin for Forge 1.3.0
- Added a modding skeleton repository as an alternative to editing the build.gradle the Forge MDK ships with.
- Added two more reified generic functions to the KotlinEventBus for `priority` and `receivedCancelled` parameters.
- Fixed the ObjectHolderDelegate not allowing subtypes of classes that implement IForgeRegistryEntry
- Updated to Kotlin 1.4-M2, Updated to coroutines 1.3.7

## Kotlin for Forge 1.2.2
- Added a sided delegate class which returns a "client value" on the client side and a "server value" on the server side.
- Added a new utility file called "Kotlin.kt" that provides a few utility functions not related to Minecraft Forge.
- Added an example mod. I will make a template GitHub repository for Kotlin for Forge soon.
- Adjusted mod construction to accurately report exceptions in @Mod object constructors
- Restructured Kotlin for Forge code to use Kotlin APIs whenever possible
- Added styling to the maven repo

## Kotlin for Forge 1.2.1
- Added backwards compatibility to mods that used versions of Kotlin for Forge before 1.2.0

## Kotlin for Forge 1.2.0
- Added a Kotlin implementation of the Forge EventBus that has working addListener and addGenericListener functions
- Added an overload of addGenericListener that uses a reified type parameter instead of a class parameter.
- Updated to Kotlin 1.4-M1

## Kotlin for Forge 1.1.0
- Events now properly fire through KotlinModContainer
- Updated to Kotlin 1.3.70, Updated to coroutines 1.3.4, Updated to JetBrains annotations 19.0.0

## Kotlin for Forge 1.0.1
- Fixed an issue with language extensions
- Fixed an internal crash

## Kotlin for Forge 1.0.0
- Initial release for 1.14 and 1.15