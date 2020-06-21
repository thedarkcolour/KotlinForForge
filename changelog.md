Kotlin for Forge 1.3.0
- Added a modding skeleton repository as an alternative to editing the build.gradle the Forge MDK ships with.
- Added two more reified generic functions to the KotlinEventBus for `priority` and `receivedCancelled` parameters.
- Fixed the ObjectHolderDelegate not allowing subtypes of classes that implement IForgeRegistryEntry
- Updated to Kotlin 1.4-M2, Updated to coroutines 1.3.7
- **KFF 1.3.x is not compatible with versions earlier than 1.2.3 due to Kotlin Reflection changes in 1.4-M2**

Kotlin for Forge 1.2.2
- Added a sided delegate class which returns a "client value" on the client side and a "server value" on the server side.
- Added a new utility file called "Kotlin.kt" that provides a few utility functions not related to Minecraft Forge.
- Added an example mod. I will make a template GitHub repository for Kotlin for Forge soon.
- Adjusted mod construction to accurately report exceptions in @Mod object constructors
- Restructured Kotlin for Forge code to use Kotlin APIs whenever possible
- Added styling to the maven repo

Kotlin for Forge 1.2.1
- Added backwards compatibility to mods that used versions of Kotlin for Forge before 1.2.0

Kotlin for Forge 1.2.0
- Added a Kotlin implementation of the Forge EventBus that has working addListener and addGenericListener functions
- Added an overload of addGenericListener that uses a reified type parameter instead of a class parameter.
- Updated to Kotlin 1.4-M1

Kotlin for Forge 1.1.0
- Events now properly fire through KotlinModContainer
- Updated to Kotlin 1.3.70, Updated to coroutines 1.3.4, Updated to JetBrains annotations 19.0.0

Kotlin for Forge 1.0.1
- Fixed an issue with language extensions
- Fixed an internal crash

Kotlin for Forge 1.0.0
- Initial release for 1.14 and 1.15