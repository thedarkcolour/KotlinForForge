package thedarkcolour.kotlinforforge.forge.vectorutil

import net.minecraft.core.Vec3i

public operator fun Vec3i.plus(other: Vec3i): Vec3i = offset(other)

public operator fun Vec3i.unaryMinus(): Vec3i = this * -1

public operator fun Vec3i.minus(other: Vec3i): Vec3i = subtract(other)

public operator fun Vec3i.times(times: Int): Vec3i = multiply(times)

public infix fun Vec3i.dot(other: Vec3i): Int {
    return x * other.x + y * other.y + z * other.z
}

public fun Vec3i.clone(): Vec3i {
    return Vec3i(x, y, z)
}
