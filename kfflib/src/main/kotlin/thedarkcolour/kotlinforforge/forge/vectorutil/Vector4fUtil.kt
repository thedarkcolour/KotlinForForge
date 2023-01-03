package thedarkcolour.kotlinforforge.forge.vectorutil

import com.mojang.math.Vector3f
import com.mojang.math.Vector4f

public operator fun Vector4f.plusAssign(other: Vector4f): Unit = add(other.x(), other.y(), other.z(), other.w())

public operator fun Vector4f.plus(other: Vector4f): Vector4f {
    val newOne = Vector4f(x(), y(), z(), w())
    newOne += other
    return newOne
}

public operator fun Vector4f.unaryMinus(): Vector4f = this * -1F

public operator fun Vector4f.minusAssign(other: Vector4f): Unit = add(-other.x(), -other.y(), -other.z(), -other.w())

public operator fun Vector4f.minus(other: Vector4f): Vector4f {
    val newOne = Vector4f(x(), y(), z(), w())
    newOne -= other
    return newOne
}

public operator fun Vector4f.timesAssign(times: Float): Unit = mul(times)

public operator fun Vector4f.timesAssign(other: Vector3f): Unit = mul(other)

public operator fun Vector4f.times(times: Float): Vector4f {
    val newOne = Vector4f(x(), y(), z(), w())
    newOne *= times
    return newOne
}

public fun Vector4f.clone(): Vector4f {
    return Vector4f(x(), y(), z(), w())
}
