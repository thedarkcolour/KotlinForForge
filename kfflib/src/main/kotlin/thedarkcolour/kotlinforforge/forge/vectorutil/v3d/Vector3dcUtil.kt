package thedarkcolour.kotlinforforge.forge.vectorutil.v3d

import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec3
import org.joml.*

public operator fun Vector3d.plusAssign(other: Vector3dc) {
    add(other)
}

public operator fun Vector3dc.plus(other: Vector3dc): Vector3d = add(other, Vector3d())

public operator fun Vector3dc.unaryMinus(): Vector3d = negate(Vector3d())

public operator fun Vector3d.minusAssign(other: Vector3dc) {
    x -= other.x()
    y -= other.y()
    z -= other.z()
}

public operator fun Vector3dc.minus(other: Vector3dc): Vector3d = sub(other, Vector3d())

public operator fun Vector3d.timesAssign(scalar: Double) {
    mul(scalar)
}

public operator fun Vector3dc.times(scalar: Double): Vector3d = mul(scalar, Vector3d())

public operator fun Vector3d.timesAssign(other: Vector3dc) {
    mul(other)
}

public operator fun Vector3dc.times(other: Vector3dc): Vector3d = mul(other, Vector3d())

public operator fun Vector3d.timesAssign(matrix: Matrix3dc) {
    mul(matrix)
}

public operator fun Vector3dc.times(matrix: Matrix3dc): Vector3d = mul(matrix, Vector3d())

public operator fun Vector3d.timesAssign(quaternion: Quaterniondc) {
    rotate(quaternion)
}

public operator fun Vector3dc.times(quaternion: Quaterniondc): Vector3d = rotate(quaternion, Vector3d())

public operator fun Vector3d.timesAssign(matrix: Matrix3x2dc) {
    mul(matrix)
}

public operator fun Vector3dc.times(matrix: Matrix3x2dc): Vector3d = mul(matrix, Vector3d())

public operator fun Vector3d.divAssign(scalar: Double) {
    div(scalar)
}

public operator fun Vector3dc.div(scalar: Double): Vector3d = div(scalar, Vector3d())

public operator fun Vector3d.divAssign(other: Vector3dc) {
    x /= other.x()
    y /= other.y()
    z /= other.z()
}

public operator fun Vector3dc.div(other: Vector3dc): Vector3d = div(other, Vector3d())

public fun Vector3dc.deepCopy(): Vector3d = Vector3d(x(), y(), z())

public operator fun Vector3dc.component1(): Double = x()

public operator fun Vector3dc.component2(): Double = y()

public operator fun Vector3dc.component3(): Double = z()

public operator fun Vector3dc.iterator(): DoubleIterator {
    return object: DoubleIterator() {
        var index = 0

        override fun hasNext(): Boolean {
            return index <= 2
        }

        override fun nextDouble(): Double {
            if (index > 2) throw IndexOutOfBoundsException()
            return this@iterator[index++]
        }
    }
}

public operator fun Vector3d.set(index: Int, scalar: Double) {
    setComponent(index, scalar)
}

public fun Vector3dc.toVector3f(): Vector3f = get(Vector3f())

public fun Vector3dc.toVec3(): Vec3 = Vec3(x(), y(), z())

public fun Vector3dc.toVec3i(): Vec3i = Vec3i(x(), y(), z())
