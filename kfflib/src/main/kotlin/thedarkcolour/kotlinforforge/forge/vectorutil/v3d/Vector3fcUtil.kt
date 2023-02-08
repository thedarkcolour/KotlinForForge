package thedarkcolour.kotlinforforge.forge.vectorutil.v3d

import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec3
import org.joml.*

public operator fun Vector3f.plusAssign(other: Vector3fc){
    add(other)
}

public operator fun Vector3fc.plus(other: Vector3fc): Vector3f = add(other, Vector3f())

public operator fun Vector3fc.unaryMinus(): Vector3f = negate(Vector3f())

public operator fun Vector3f.minusAssign(other: Vector3fc) {
    x -= other.x()
    y -= other.y()
    z -= other.z()
}

public operator fun Vector3fc.minus(other: Vector3fc): Vector3f = sub(other, Vector3f())

public operator fun Vector3f.timesAssign(scalar: Float) {
    mul(scalar)
}

public operator fun Vector3fc.times(scalar: Float): Vector3f = mul(scalar, Vector3f())

public operator fun Vector3f.timesAssign(other: Vector3fc) {
    mul(other)
}

public operator fun Vector3fc.times(other: Vector3fc): Vector3f = mul(other, Vector3f())

public operator fun Vector3f.timesAssign(matrix: Matrix3fc) {
    mul(matrix)
}

public operator fun Vector3fc.times(matrix: Matrix3fc): Vector3f = mul(matrix, Vector3f())

public operator fun Vector3f.timesAssign(quaternion: Quaternionfc) {
    rotate(quaternion)
}

public operator fun Vector3fc.times(quaternion: Quaternionfc): Vector3f = rotate(quaternion, Vector3f())

public operator fun Vector3f.timesAssign(matrix: Matrix3x2fc) {
    mul(matrix)
}

public operator fun Vector3fc.times(matrix: Matrix3x2fc): Vector3f = mul(matrix, Vector3f())

public operator fun Vector3f.divAssign(scalar: Float) {
    div(scalar)
}

public operator fun Vector3fc.div(scalar: Float): Vector3f = div(scalar, Vector3f())

public operator fun Vector3f.divAssign(other: Vector3fc) {
    div(other)
}

public operator fun Vector3fc.div(other: Vector3fc): Vector3f = div(other, Vector3f())

public fun Vector3fc.deepCopy(): Vector3f = Vector3f(x(), y(), z())

public operator fun Vector3fc.component1(): Float = x()

public operator fun Vector3fc.component2(): Float = y()

public operator fun Vector3fc.component3(): Float = z()

public operator fun Vector3fc.iterator(): FloatIterator {
    return object: FloatIterator() {
        var index = 0

        override fun hasNext(): Boolean {
            return index <= 2
        }

        override fun nextFloat(): Float {
            if (index > 2) throw IndexOutOfBoundsException()
            return this@iterator[index++]
        }
    }
}

public operator fun Vector3f.set(index: Int, scalar: Float) {
    setComponent(index, scalar)
}

public fun Vector3fc.toVector3d(): Vector3d = get(Vector3d())

public fun Vector3fc.toVec3(): Vec3 = Vec3(x().toDouble(), y().toDouble(), z().toDouble())

public fun Vector3fc.toVec3i(): Vec3i = Vec3i(x().toInt(), y().toInt(), z().toInt())
