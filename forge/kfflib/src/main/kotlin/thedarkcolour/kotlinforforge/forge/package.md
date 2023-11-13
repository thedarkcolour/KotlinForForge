# thedarkcolour.kotlinforforge.forge
This package contains useful constants and utility functions that improve Forge's compatibility with Kotlin.
## Constants
There are constants for the mod event bus, the Forge event bus, Kotlin mod loading context, and
the current distribution of Minecraft.
## Functions
There are inline functions that serve as alternatives to the static methods in DistExecutor which inline the lambdas
passed in as the parameters. There is also a function to create a configuration file for your mod.
## Property Delegates
There are functions to create lazy sided delegates (read only) and to create non-lazy sided delegates (read only) whose values
are evaluated each time they are accessed. 
