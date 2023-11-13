package thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d

import net.minecraft.core.Vec3i
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import org.joml.Vector3d


public operator fun Vec3.plus(other: Vec3): Vec3 = add(other)

public operator fun Vec3.unaryMinus(): Vec3 = this * -1.0

public operator fun Vec3.minus(other: Vec3): Vec3 = subtract(other)

public operator fun Vec3.times(scalar: Double): Vec3 = scale(scalar)

public operator fun Vec3.times(other: Vec3): Vec3 = multiply(other)

public operator fun Vec3.div(scalar: Double): Vec3 = Vec3(x / scalar, y / scalar, z / scalar)

public operator fun Vec3.div(other: Vec3): Vec3 = Vec3(x / other.x, y / other.y, z / other.z)

public fun Vec3.deepCopy(): Vec3 = Vec3(x, y, z)

public operator fun Vec3.component1(): Double = x

public operator fun Vec3.component2(): Double = y

public operator fun Vec3.component3(): Double = z

public operator fun Vec3.iterator(): DoubleIterator {
    return object: DoubleIterator() {
        var index = 0

        override fun hasNext(): Boolean {
            return index <= 2
        }

        override fun nextDouble(): Double {
            return this@iterator[index++]
        }
    }
}

public operator fun Vec3.get(index: Int): Double {
    if (index == 0) return x
    if (index == 1) return y
    if (index == 2) return z
    throw IndexOutOfBoundsException()
}

public fun Vec3.toVec3i(): Vec3i = Vec3i(Mth.floor(x), Mth.floor(y), Mth.floor(z))

public fun Vec3.toVector3d(): Vector3d = Vector3d(x, y, z)
