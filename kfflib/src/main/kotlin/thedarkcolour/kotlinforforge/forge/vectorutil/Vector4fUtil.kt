package thedarkcolour.kotlinforforge.forge.vectorutil

import org.joml.Vector3f
import org.joml.Vector4f

public operator fun Vector4f.plusAssign(other: Vector4f){
    x += other.x
    y += other.y
    z += other.z
    w += other.w
}

public operator fun Vector4f.plus(other: Vector4f): Vector4f {
    val newOne = Vector4f(x(), y(), z(), w())
    newOne += other
    return newOne
}

public operator fun Vector4f.unaryMinus(): Vector4f = this * -1F

public operator fun Vector4f.minusAssign(other: Vector4f) {
    x -= other.x
    y -= other.y
    z -= other.z
    w -= other.w
}

public operator fun Vector4f.minus(other: Vector4f): Vector4f {
    val newOne = Vector4f(x(), y(), z(), w())
    newOne -= other
    return newOne
}

public operator fun Vector4f.timesAssign(times: Float) {
    x *= times
    y *= times
    z *= times
    w *= times
}

public operator fun Vector4f.timesAssign(other: Vector3f) {
    x *= other.x
    y *= other.y
    z *= other.z
}

public operator fun Vector4f.times(times: Float): Vector4f {
    val newOne = Vector4f(x(), y(), z(), w())
    newOne *= times
    return newOne
}

public fun Vector4f.deepCopy(): Vector4f {
    return Vector4f(x(), y(), z(), w())
}

public operator fun Vector4f.component1(): Float = x()

public operator fun Vector4f.component2(): Float = y()

public operator fun Vector4f.component3(): Float = z()

public operator fun Vector4f.component4(): Float = w()
