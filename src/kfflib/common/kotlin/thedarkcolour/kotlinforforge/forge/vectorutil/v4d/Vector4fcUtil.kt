package thedarkcolour.kotlinforforge.forge.vectorutil.v4d

import org.joml.*

public operator fun Vector4f.plusAssign(other: Vector4fc){
    add(other)
}

public operator fun Vector4fc.plus(other: Vector4fc): Vector4f = add(other, Vector4f())

public operator fun Vector4fc.unaryMinus(): Vector4f = negate(Vector4f())

public operator fun Vector4f.minusAssign(other: Vector4fc) {
    x -= other.x()
    y -= other.y()
    z -= other.z()
    w -= other.w()
}

public operator fun Vector4fc.minus(other: Vector4fc): Vector4f = sub(other, Vector4f())

public operator fun Vector4f.timesAssign(scalar: Float) {
    mul(scalar)
}

public operator fun Vector4fc.times(scalar: Float): Vector4f = mul(scalar, Vector4f())

public operator fun Vector4f.timesAssign(other: Vector4fc) {
    mul(other)
}

public operator fun Vector4fc.times(other: Vector4fc): Vector4f = mul(other, Vector4f())

public operator fun Vector4f.timesAssign(matrix: Matrix4fc) {
    mul(matrix)
}

public operator fun Vector4fc.times(matrix: Matrix4fc): Vector4f = mul(matrix, Vector4f())

public operator fun Vector4f.timesAssign(quaternion: Quaternionfc) {
    rotate(quaternion)
}

public operator fun Vector4fc.times(quaternion: Quaternionfc): Vector4f = rotate(quaternion, Vector4f())

public operator fun Vector4f.timesAssign(matrix: Matrix4x3fc) {
    mul(matrix)
}

public operator fun Vector4fc.times(matrix: Matrix4x3fc): Vector4f = mul(matrix, Vector4f())

public operator fun Vector4f.divAssign(scalar: Float) {
    div(scalar)
}

public operator fun Vector4fc.div(scalar: Float): Vector4f = div(scalar, Vector4f())

public operator fun Vector4f.divAssign(other: Vector4fc) {
    div(other)
}

public operator fun Vector4fc.div(other: Vector4fc): Vector4f = div(other, Vector4f())

public fun Vector4fc.deepCopy(): Vector4f = Vector4f(x(), y(), z(), w())

public operator fun Vector4fc.component1(): Float = x()

public operator fun Vector4fc.component2(): Float = y()

public operator fun Vector4fc.component3(): Float = z()

public operator fun Vector4fc.component4(): Float = w()

public operator fun Vector4fc.iterator(): FloatIterator {
    return object: FloatIterator() {
        var index = 0

        override fun hasNext(): Boolean {
            return index <= 3
        }

        override fun nextFloat(): Float {
            if (index > 3) throw IndexOutOfBoundsException()
            return this@iterator[index++]
        }
    }
}

public operator fun Vector4f.set(index: Int, scalar: Float) {
    setComponent(index, scalar)
}

public fun Vector4fc.toVector4d(): Vector4d = get(Vector4d())
