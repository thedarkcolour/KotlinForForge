package thedarkcolour.kotlinforforge.forge.vectorutil

import org.joml.Vector3d
import org.joml.Vector3f
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec3

public operator fun Vector3d.plusAssign(other: Vector3d) {
    x += other.x
    y += other.y
    z += other.z
}

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

public operator fun Vector3d.timesAssign(times: Double) {
    x *= times
    y *= times
    z *= times
}

public operator fun Vector3d.times(times: Double): Vector3d {
    return Vector3d(x * times, y * times, z * times)
}

public infix fun Vector3d.dot(other: Vector3d): Double {
    return x * other.x + y * other.y + z * other.z
}

public infix fun Vector3d.cross(other: Vector3d): Vector3d {
    return Vector3d((y * other.z - z * other.y), (z * other.x - x * other.z), (x * other.y - y * other.x))
}

public fun Vector3d.deepCopy(): Vector3d {
    return Vector3d(x, y, z)
}

public fun Vector3d.toVec3i(): Vec3i = Vec3i(x, y, z)

public fun Vector3d.toVec3(): Vec3 = Vec3(x, y, z)

public fun Vector3d.toVector3f(): Vector3f = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())

public operator fun Vector3d.component1(): Double = x

public operator fun Vector3d.component2(): Double = y

public operator fun Vector3d.component3(): Double = z
