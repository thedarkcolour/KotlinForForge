package thedarkcolour.kotlinforforge.neoforge.forge

import net.minecraft.core.Direction
import net.neoforged.neoforge.common.capabilities.Capability
import net.neoforged.neoforge.common.capabilities.ICapabilityProvider

public fun <T> ICapabilityProvider.getCapabilityOrThrow(cap: Capability<T>, direction: Direction? = null): T =
    getCapability(cap, direction).resolve().orElseThrow()
