package thedarkcolour.kotlinforforge.forge.vectorutil

import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec3
import org.joml.Vector3d


public operator fun Vec3.plus(other: Vec3): Vec3 = add(other)

public operator fun Vec3.unaryMinus(): Vec3 = this * -1.0

public operator fun Vec3.minus(other: Vec3): Vec3 = subtract(other)

public operator fun Vec3.times(times: Double): Vec3 = scale(times)

public fun Vec3.deepCopy(): Vec3 {
    return Vec3(x, y, z)
}

public fun Vec3.toVec3i(): Vec3i = Vec3i(x, y, z)

public fun Vec3.toVector3d(): Vector3d = Vector3d(x, y, z)

public operator fun Vec3.component1(): Double = x

public operator fun Vec3.component2(): Double = y

public operator fun Vec3.component3(): Double = z
