package thedarkcolour.kotlinforforge.forge.vectorutil

import com.mojang.math.Vector3d
import com.mojang.math.Vector3f
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec3

public operator fun Vector3d.plusAssign(other: Vector3d): Unit = add(other)

public operator fun Vector3d.plus(other: Vector3d): Vector3d {
    return Vector3d(x + other.x, y + other.y, z + other.z)
}

public operator fun Vector3d.unaryMinus(): Vector3d = this * -1.0

public operator fun Vector3d.minusAssign(other: Vector3d) {
    x -= other.x
    y -= other.y
    z -= other.z
}

public operator fun Vector3d.minus(other: Vector3d): Vector3d {
    return Vector3d(x - other.x, y - other.y, z - other.z)
}

public operator fun Vector3d.timesAssign(times: Double): Unit = scale(times)

public operator fun Vector3d.times(times: Double): Vector3d {
    return Vector3d(x * times, y * times, z * times)
}

public infix fun Vector3d.dot(other: Vector3d): Double {
    return x * other.x + y * other.y + z * other.z
}

public infix fun Vector3d.cross(other: Vector3d): Vector3d {
    return Vector3d((y * other.z - z * other.y), (z * other.x - x * other.z), (x * other.y - y * other.x))
}

public fun Vector3d.clone(): Vector3d {
    return Vector3d(x, y, z)
}

public fun Vector3d(other: Vec3): Vector3d {
    return Vector3d(other.x, other.y, other.z)
}

public fun Vector3d(other: Vector3f): Vector3d {
    return Vector3d(other.x().toDouble(), other.y().toDouble(), other.z().toDouble())
}

public fun Vector3d(other: Vec3i): Vector3d {
    return Vector3d(other.x.toDouble(), other.y.toDouble(), other.z.toDouble())
}

public operator fun Vector3d.component1(): Double = x

public operator fun Vector3d.component2(): Double = y

public operator fun Vector3d.component3(): Double = z
