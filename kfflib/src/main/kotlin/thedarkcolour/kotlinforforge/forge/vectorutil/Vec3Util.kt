package thedarkcolour.kotlinforforge.forge.vectorutil

import com.mojang.math.Vector3d
import net.minecraft.world.phys.Vec3


public operator fun Vec3.plus(other: Vec3): Vec3 = add(other)

public operator fun Vec3.unaryMinus(): Vec3 = this * -1.0

public operator fun Vec3.minus(other: Vec3): Vec3 = subtract(other)

public operator fun Vec3.times(times: Double): Vec3 = scale(times)

public fun Vec3.clone(): Vec3 {
    return Vec3(x, y, z)
}

public fun Vec3(other: Vector3d): Vec3 {
    return Vec3(other.x, other.y, other.z)
}

public operator fun Vec3.component1(): Double = x

public operator fun Vec3.component2(): Double = y

public operator fun Vec3.component3(): Double = z
