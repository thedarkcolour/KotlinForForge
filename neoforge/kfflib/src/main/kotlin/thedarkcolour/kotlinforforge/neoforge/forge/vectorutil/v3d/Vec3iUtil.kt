package thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d

import org.joml.Vector3d
import org.joml.Vector3f
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec3

public operator fun Vec3i.plus(other: Vec3i): Vec3i = offset(other)

public operator fun Vec3i.unaryMinus(): Vec3i = this * -1

public operator fun Vec3i.minus(other: Vec3i): Vec3i = subtract(other)

public operator fun Vec3i.times(scalar: Int): Vec3i = multiply(scalar)

public operator fun Vec3i.times(other: Vec3i): Vec3i = Vec3i(x * other.x, y * other.y, z * other.z)

public operator fun Vec3i.div(scalar: Int): Vec3i = Vec3i(x / scalar, y / scalar, z / scalar)

public operator fun Vec3i.div(other: Vec3i): Vec3i = Vec3i(x / other.x, y / other.y, z / other.z)

public infix fun Vec3i.dot(other: Vec3i): Int {
    val (x, y, z) = this

    return x * other.x + y * other.y + z * other.z
}

public fun Vec3i.deepCopy(): Vec3i {
    return Vec3i(x, y, z)
}

public operator fun Vec3i.component1(): Int = x

public operator fun Vec3i.component2(): Int = y

public operator fun Vec3i.component3(): Int = z

public operator fun Vec3i.iterator(): IntIterator {
    return object: IntIterator() {
        var index = 0

        override fun hasNext(): Boolean {
            return index <= 2
        }

        override fun nextInt(): Int {
            return this@iterator[index++]
        }
    }
}

public operator fun Vec3i.get(index: Int): Int {
    if (index == 0) return x
    if (index == 1) return y
    if (index == 2) return z
    throw IndexOutOfBoundsException()
}

public fun Vec3i.toVec3(): Vec3 = Vec3.atLowerCornerOf(this)

public fun Vec3i.toVector3f(): Vector3f = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())

public fun Vec3i.toVector3d(): Vector3d = Vector3d(x.toDouble(), y.toDouble(), z.toDouble())
