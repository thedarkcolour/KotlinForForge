package thedarkcolour.kotlinforforge.forge.vectorutil

import com.mojang.math.Vector3d
import com.mojang.math.Vector3f
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec3

public operator fun Vec3i.plus(other: Vec3i): Vec3i = offset(other)

public operator fun Vec3i.unaryMinus(): Vec3i = this * -1

public operator fun Vec3i.minus(other: Vec3i): Vec3i = subtract(other)

public operator fun Vec3i.times(times: Int): Vec3i = multiply(times)

public infix fun Vec3i.dot(other: Vec3i): Int {

    val (x, y, z) = this

    return x * other.x + y * other.y + z * other.z
}

public fun Vec3i.clone(): Vec3i {
    return Vec3i(x, y, z)
}

public fun Vec3i(other: Vec3): Vec3i {
    return Vec3i(other.x.toInt(), other.y.toInt(), other.z.toInt())
}

public fun Vec3i(other: Vector3f): Vec3i {
    return Vec3i(other.x().toInt(), other.y().toInt(), other.z().toInt())
}

public fun Vec3i(other: Vector3d): Vec3i {
    return Vec3i(other.x.toInt(), other.y.toInt(), other.z.toInt())
}

public operator fun Vec3i.component1(): Int = x

public operator fun Vec3i.component2(): Int = y

public operator fun Vec3i.component3(): Int = z
