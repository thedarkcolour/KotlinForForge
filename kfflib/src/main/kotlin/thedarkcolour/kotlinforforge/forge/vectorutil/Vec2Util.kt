package thedarkcolour.kotlinforforge.forge.vectorutil

import net.minecraft.world.phys.Vec2

public operator fun Vec2.plus(other: Vec2): Vec2 = add(other)

public operator fun Vec2.minus(other: Vec2): Vec2 = Vec2(x - other.x, y - other.y)

public operator fun Vec2.unaryMinus(): Vec2 = negated()

public operator fun Vec2.times(times: Float): Vec2 = scale(times)

public fun Vec2.deepCopy(): Vec2 {
    return Vec2(x, y)
}

public operator fun Vec2.component1(): Float = x

public operator fun Vec2.component2(): Float = y
