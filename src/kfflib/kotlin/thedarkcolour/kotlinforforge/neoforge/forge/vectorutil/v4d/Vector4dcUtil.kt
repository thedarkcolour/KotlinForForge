package thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v4d

import org.joml.*

public operator fun Vector4d.plusAssign(other: Vector4dc){
    add(other)
}

public operator fun Vector4dc.plus(other: Vector4dc): Vector4d = add(other, Vector4d())

public operator fun Vector4dc.unaryMinus(): Vector4d = negate(Vector4d())

public operator fun Vector4d.minusAssign(other: Vector4dc) {
    x -= other.x()
    y -= other.y()
    z -= other.z()
    w -= other.w()
}

public operator fun Vector4dc.minus(other: Vector4dc): Vector4d = sub(other, Vector4d())

public operator fun Vector4d.minusAssign(other: Vector4fc) {
    x -= other.x()
    y -= other.y()
    z -= other.z()
    w -= other.w()
}

public operator fun Vector4dc.minus(other: Vector4fc): Vector4d = sub(other, Vector4d())

public operator fun Vector4d.timesAssign(scalar: Double) {
    mul(scalar)
}

public operator fun Vector4dc.times(scalar: Double): Vector4d = mul(scalar, Vector4d())

public operator fun Vector4d.timesAssign(other: Vector4dc) {
    mul(other)
}

public operator fun Vector4dc.times(other: Vector4dc): Vector4d = mul(other, Vector4d())

public operator fun Vector4d.timesAssign(matrix: Matrix4dc) {
    mul(matrix)
}

public operator fun Vector4dc.times(matrix: Matrix4dc): Vector4d = mul(matrix, Vector4d())

public operator fun Vector4d.timesAssign(quaternion: Quaterniondc) {
    rotate(quaternion)
}

public operator fun Vector4dc.times(quaternion: Quaterniondc): Vector4d = rotate(quaternion, Vector4d())

public operator fun Vector4d.timesAssign(matrix: Matrix4x3fc) {
    mul(matrix)
}

public operator fun Vector4dc.times(matrix: Matrix4x3fc): Vector4d = mul(matrix, Vector4d())

public operator fun Vector4d.divAssign(scalar: Double) {
    div(scalar)
}

public operator fun Vector4dc.div(scalar: Double): Vector4d = div(scalar, Vector4d())

public operator fun Vector4d.divAssign(other: Vector4dc) {
    div(other)
}

public operator fun Vector4dc.div(other: Vector4dc): Vector4d = div(other, Vector4d())

public fun Vector4dc.deepCopy(): Vector4d = Vector4d(x(), y(), z(), w())

public operator fun Vector4dc.component1(): Double = x()

public operator fun Vector4dc.component2(): Double = y()

public operator fun Vector4dc.component3(): Double = z()

public operator fun Vector4dc.component4(): Double = w()

public operator fun Vector4dc.iterator(): DoubleIterator {
    return object: DoubleIterator() {
        var index = 0

        override fun hasNext(): Boolean {
            return index <= 3
        }

        override fun nextDouble(): Double {
            if (index > 3) throw IndexOutOfBoundsException()
            return this@iterator[index++]
        }
    }
}

public operator fun Vector4d.set(index: Int, scalar: Double) {
    setComponent(index, scalar)
}

public fun Vector4dc.toVector4f(): Vector4f = get(Vector4f())
