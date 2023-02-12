package thedarkcolour.kfflibtest

import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.joml.*
import thedarkcolour.kotlinforforge.forge.vectorutil.v2d.*
import thedarkcolour.kotlinforforge.forge.vectorutil.v3d.*
import thedarkcolour.kotlinforforge.forge.vectorutil.v4d.*
import kotlin.IllegalStateException
import kotlin.random.Random
import kotlin.random.nextInt

internal fun testVec2() {
    val x1 = nextFloat()
    val y1 = nextFloat()

    val x2 = nextFloat()
    val y2 = nextFloat()

    val v1 = Vec2(x1, y1)
    val v2 = Vec2(x2, y2)

    val (a, b) = v1
    requireEquality(a, x1) { "Vec2 deconstruction for x has failed!" }
    requireEquality(b, y1) { "Vec2 deconstruction for y has failed!" }

    requireEquality(x1, v1[0]) { "Vec2 index access for x has failed! "}
    requireEquality(y1, v1[1]) { "Vec2 index access for y has failed! "}

    for ((index, element) in v1.iterator().withIndex()) {
        if (index == 0) {
            requireEquality(x1, element) { "Vec2 iterator x has failed! "}
        }
        if (index == 1) {
            requireEquality(y1, element) { "Vec2 iterator y has failed! "}
        }
        if (index >= 2) {
            throw IllegalStateException("Vec2 has no 3rd element!")
        }
    }

    // I can't believe Vec2#equals does not override Object#equals
    // It's entirely separate method with same name
    requireEquality(v1 + v2, Vec2(x1 + x2, y1 + y2), Vec2::equals) { "Vec2 addition has failed!" }
    requireEquality(v1 - v2, Vec2(x1 - x2, y1 - y2), Vec2::equals) { "Vec2 subtraction has failed!" }
    requireEquality(-v1, Vec2(-x1, -y1), Vec2::equals) { "Vec2 unaryMinus has failed!" }

    val scalar = nextFloat()
    requireEquality(v1 * scalar, Vec2(x1 * scalar, y1 * scalar), Vec2::equals) { "Vec2 scalar multiplication has failed! " }
    requireEquality(v1 * v2, Vec2(x1 * x2, y1 * y2), Vec2::equals) { "Vec2 * Vec2 multiplication has failed! " }

    requireEquality(v1 / scalar, Vec2(x1 / scalar, y1 / scalar), Vec2::equals) { "Vec2 scalar division has failed! " }
    requireEquality(v1 / v2, Vec2(x1 / x2, y1 / y2), Vec2::equals) { "Vec2 / Vec2 division has failed! " }

    requireEquality(v1.deepCopy(), v1, Vec2::equals) { "Vec2 cloning has failed! " }

    requireEquality(v1, Vec2(x1, y1), Vec2::equals) { "v1 has been mutated!" }
    requireEquality(v2, Vec2(x2, y2), Vec2::equals) { "v2 has been mutated!" }

    KFFLibTest.LOGGER.info("Vec2 test succeed")
}

internal fun testVec3i() {
    val (x1, y1, z1) = listOf(nextInt(), nextInt(), nextInt())
    val (x2, y2, z2) = listOf(nextInt(), nextInt(), nextInt())

    val v1 = Vec3i(x1, y1, z1)
    val v2 = Vec3i(x2, y2, z2)

    val (a, b, c) = v1

    requireEquality(a, x1) { "Vec3i deconstruction for x has failed!" }
    requireEquality(b, y1) { "Vec3i deconstruction for y has failed!" }
    requireEquality(c, z1) { "Vec3i deconstruction for z has failed!" }

    requireEquality(v1[0], x1) { "Vec3i index access for x has failed!" }
    requireEquality(v1[1], y1) { "Vec3i index access for y has failed!" }
    requireEquality(v1[2], z1) { "Vec3i index access for z has failed!" }

    for ((index, element) in v1.iterator().withIndex()) {
        if (index == 0) {
            requireEquality(x1, element) { "Vec3i iterator x has failed! "}
        }
        if (index == 1) {
            requireEquality(y1, element) { "Vec3i iterator y has failed! "}
        }
        if (index == 2) {
            requireEquality(z1, element) { "Vec3i iterator z has failed! "}
        }
        if (index >= 3) {
            throw IllegalStateException("Vec3i has no 4th element!")
        }
    }

    requireEquality(v1 + v2, Vec3i(x1 + x2, y1 + y2, z1 + z2)) { "Vec3i addition has failed!" }
    requireEquality(v1 - v2, Vec3i(x1 - x2, y1 - y2, z1 - z2)) { "Vec3i subtraction has failed!" }
    requireEquality(-v1, Vec3i(-x1, -y1, -z1)) { "Vec3i unaryMinus has failed!" }

    val scalar = nextInt()
    requireEquality(v1 * scalar, Vec3i(x1 * scalar, y1 * scalar, z1 * scalar)) { "Vec3i scalar multiplication has failed! " }
    requireEquality(v1 * v2, Vec3i(x1 * x2, y1 * y2, z1 * z2)) { "Vec3i * Vec3i multiplication has failed! " }

    requireEquality(v1 / scalar, Vec3i(x1 / scalar, y1 / scalar, z1 / scalar)) { "Vec3i scalar division has failed! " }
    requireEquality(v1 / v2, Vec3i(x1 / x2, y1 / y2, z1 / z2)) { "Vec3i / Vec3i division has failed! " }

    requireEquality(v1.deepCopy(), Vec3i(x1, y1, z1)) { "Vec3i cloning has failed! " }

    requireEquality(v1, Vec3i(x1, y1, z1)) { "v1 has been mutated!" }
    requireEquality(v2, Vec3i(x2, y2, z2)) { "v2 has been mutated!" }

    val vec3 = Vec3(x1.toDouble(), y1.toDouble(), z1.toDouble())
    val vector3f = Vector3f(x1.toFloat(), y1.toFloat(), z1.toFloat())
    val vector3d = Vector3d(x1.toDouble(), y1.toDouble(), z1.toDouble())

    requireEquality(v1, vec3.toVec3i()) { "Vec3 -> Vec3i conversion has failed!" }
    requireEquality(v1, vector3f.toVec3i()) { "Vector3f -> Vec3i conversion has failed!" }
    requireEquality(v1, vector3d.toVec3i()) { "Vector3d -> Vec3i conversion has failed!" }

    KFFLibTest.LOGGER.info("Vec3i test succeed")
}

internal fun testVec3() {
    val (x1, y1, z1) = listOf(nextDouble(), nextDouble(), nextDouble())
    val (x2, y2, z2) = listOf(nextDouble(), nextDouble(), nextDouble())

    val v1 = Vec3(x1, y1, z1)
    val v2 = Vec3(x2, y2, z2)

    val (a, b, c) = v1

    requireEquality(a, x1) { "Vec3 deconstruction for x has failed!" }
    requireEquality(b, y1) { "Vec3 deconstruction for y has failed!" }
    requireEquality(c, z1) { "Vec3 deconstruction for z has failed!" }

    requireEquality(v1[0], x1) { "Vec3 index access for x has failed!" }
    requireEquality(v1[1], y1) { "Vec3 index access for y has failed!" }
    requireEquality(v1[2], z1) { "Vec3 index access for z has failed!" }

    for ((index, element) in v1.iterator().withIndex()) {
        if (index == 0) {
            requireEquality(x1, element) { "Vec3 iterator x has failed! "}
        }
        if (index == 1) {
            requireEquality(y1, element) { "Vec3 iterator y has failed! "}
        }
        if (index == 2) {
            requireEquality(z1, element) { "Vec3 iterator z has failed! "}
        }
        if (index >= 3) {
            throw IllegalStateException("Vec3 has no 4th element!")
        }
    }

    requireEquality(v1 + v2, Vec3(x1 + x2, y1 + y2, z1 + z2)) { "Vec3 addition has failed!" }
    requireEquality(v1 - v2, Vec3(x1 - x2, y1 - y2, z1 - z2)) { "Vec3 subtraction has failed!" }
    requireEquality(-v1, Vec3(-x1, -y1, -z1)) { "Vec3 unaryMinus has failed!" }

    val scalar = nextDouble()
    requireEquality(v1 * scalar, Vec3(x1 * scalar, y1 * scalar, z1 * scalar)) { "Vec3 multiplication has failed! " }
    requireEquality(v1 * v2, Vec3(x1 * x2, y1 * y2, z1 * z2)) { "Vec3 multiplication has failed! " }

    requireEquality(v1 / scalar, Vec3(x1 / scalar, y1 / scalar, z1 / scalar)) { "Vec3 scalar division has failed! " }
    requireEquality(v1 / v2, Vec3(x1 / x2, y1 / y2, z1 / z2)) { "Vec3 / Vec3 division has failed! " }

    requireEquality(v1.deepCopy(), v1) { "Vec3 cloning has failed! " }

    requireEquality(v1, Vec3(x1, y1, z1)) { "v1 has been mutated!" }
    requireEquality(v2, Vec3(x2, y2, z2)) { "v2 has been mutated!" }

    val vec3i = Vec3i(x1, y1, z1)
    val vector3f = Vector3f(x1.toFloat(), y1.toFloat(), z1.toFloat())
    val vector3d = Vector3d(x1, y1, z1)

    requireEquality(v1, vec3i.toVec3()) { "Vec3i -> Vec3 conversion has failed!" }
    requireEquality(v1, vector3f.toVec3()) { "Vector3f -> Vec3 conversion has failed!" }
    requireEquality(v1, vector3d.toVec3()) { "Vector3d -> Vec3 conversion has failed!" }

    KFFLibTest.LOGGER.info("Vec3 test succeed")
}

internal fun testVector3d() {
    val (x1, y1, z1) = listOf(nextDouble(), nextDouble(), nextDouble())
    val (x2, y2, z2) = listOf(nextDouble(), nextDouble(), nextDouble())
    val tester: (Vector3dc, Vector3dc) -> Boolean = { a, b ->
        a.distanceSquared(b) < 0.01
    }

    val v1: Vector3dc = Vector3d(x1, y1, z1)
    val v2: Vector3dc = Vector3d(x2, y2, z2)

    val (a, b, c) = v1

    requireEquality(a, x1) { "Vector3d deconstruction for x has failed!" }
    requireEquality(b, y1) { "Vector3d deconstruction for y has failed!" }
    requireEquality(c, z1) { "Vector3d deconstruction for z has failed!" }

    requireEquality(v1[0], x1) { "Vector3d index access for x has failed!" }
    requireEquality(v1[1], y1) { "Vector3d index access for y has failed!" }
    requireEquality(v1[2], z1) { "Vector3d index access for z has failed!" }

    val v3 = Vector3d()
    
    for ((index, element) in v1.iterator().withIndex()) {
        if (index == 0) {
            requireEquality(x1, element) { "Vector3d iterator x has failed! "}
        }
        if (index == 1) {
            requireEquality(y1, element) { "Vector3d iterator y has failed! "}
        }
        if (index == 2) {
            requireEquality(z1, element) { "Vector3d iterator z has failed! "}
        }
        if (index >= 3) {
            throw IllegalStateException("Vector3d has no 4th element!")
        }
        v3[index] = element
    }
    
    requireEquality(v1, v3) { "Vector3d index setter has failed! "}

    requireEquality(v1 + v2,  Vector3d(x1 + x2, y1 + y2, z1 + z2)) { "Vector3d addition has failed!" }
    requireEquality(v1 - v2, Vector3d(x1 - x2, y1 - y2, z1 - z2)) { "Vector3d subtraction has failed!" }
    requireEquality(-v1, Vector3d(-x1, -y1, -z1)) { "Vector3d unaryMinus has failed!" }

    val scalar = nextDouble()
    requireEquality(v1 * scalar, Vector3d(x1 * scalar, y1 * scalar, z1 * scalar), tester) { "Vector3d scalar multiplication has failed! " }
    requireEquality(v1 * v2, Vector3d(x1 * x2, y1 * y2, z1 * z2), tester) { "Vector3d * Vector3d multiplication has failed! " }

    requireEquality(v1 / scalar, Vector3d(x1 / scalar, y1 / scalar, z1 / scalar), tester) { "Vector3d scalar division has failed! " }
    requireEquality(v1 / v2, Vector3d(x1 / x2, y1 / y2, z1 / z2), tester) { "Vector3d / Vector3d division has failed! " }

    requireEquality(v1.deepCopy(), v1) { "Vector3d cloning has failed!" }

    val plusAssign = v1.deepCopy()
    plusAssign += v2
    requireEquality(v1 + v2, plusAssign) { "Vector3d addition&assign has failed!" }

    val minusAssign = v1.deepCopy()
    minusAssign -= v2
    requireEquality(v1 - v2, minusAssign) { "Vector3d subtraction&assign has failed!" }

    val timesAssign = v1.deepCopy()
    timesAssign *= scalar
    requireEquality(v1 * scalar, timesAssign) { "Vector3d multiplication&assign has failed!" }

    val divAssign = v1.deepCopy()
    divAssign /= scalar
    requireEquality(v1 / scalar, divAssign) { "Vector3d division&assign has failed!" }

    requireEquality(v1, Vector3d(x1, y1, z1)) { "v1 has been mutated!" }
    requireEquality(v2, Vector3d(x2, y2, z2)) { "v2 has been mutated!" }

    val vec3i = Vec3i(x1, y1, z1)
    val vector3f = Vector3f(x1.toFloat(), y1.toFloat(), z1.toFloat())
    val vec3 = Vec3(x1, y1, z1)

    requireEquality(v1, vec3i.toVector3d()) { "Vec3i -> Vector3d conversion has failed!" }
    requireEquality(v1, vector3f.toVector3d()) { "Vector3f -> Vector3d conversion has failed!" }
    requireEquality(v1, vec3.toVector3d()) { "Vec3 -> Vector3d conversion has failed!" }

    KFFLibTest.LOGGER.info("Vector3d test succeed")
}

internal fun testVector3f() {
    val (x1, y1, z1) = listOf(nextFloat(), nextFloat(), nextFloat())
    val (x2, y2, z2) = listOf(nextFloat(), nextFloat(), nextFloat())
    val tester: (Vector3fc, Vector3fc) -> Boolean = { a, b ->
        a.distanceSquared(b) < 0.01
    }

    val v1: Vector3fc = Vector3f(x1, y1, z1)
    val v2: Vector3fc = Vector3f(x2, y2, z2)

    val (a, b, c) = v1

    requireEquality(a, x1) { "Vector3f deconstruction for x has failed!" }
    requireEquality(b, y1) { "Vector3f deconstruction for y has failed!" }
    requireEquality(c, z1) { "Vector3f deconstruction for z has failed!" }

    requireEquality(v1[0], x1) { "Vector3f index access for x has failed!" }
    requireEquality(v1[1], y1) { "Vector3f index access for y has failed!" }
    requireEquality(v1[2], z1) { "Vector3f index access for z has failed!" }

    val v3 = Vector3f()

    for ((index, element) in v1.iterator().withIndex()) {
        if (index == 0) {
            requireEquality(x1, element) { "Vector3f iterator x has failed! "}
        }
        if (index == 1) {
            requireEquality(y1, element) { "Vector3f iterator y has failed! "}
        }
        if (index == 2) {
            requireEquality(z1, element) { "Vector3f iterator z has failed! "}
        }
        if (index >= 3) {
            throw IllegalStateException("Vector3f has no 4th element!")
        }
        v3[index] = element
    }

    requireEquality(v1, v3) { "Vector3f index setter has failed! "}
    
    requireEquality(v1 + v2, Vector3f(x1 + x2, y1 + y2, z1 + z2)) { "Vector3f addition has failed!" }
    requireEquality(v1 - v2, Vector3f(x1 - x2, y1 - y2, z1 - z2)) { "Vector3f subtraction has failed!" }
    requireEquality(-v1, Vector3f(-x1, -y1, -z1)) { "Vector3f unaryMinus has failed!" }

    val scalar = nextFloat()
    requireEquality(v1 * scalar, Vector3f(x1 * scalar, y1 * scalar, z1 * scalar), tester) { "Vector3f multiplication has failed! " }
    requireEquality(v1 * v2, Vector3f(x1 * x2, y1 * y2, z1 * z2), tester) { "Vector3f * Vector3f multiplication has failed! " }
    requireEquality(v1.deepCopy(), v1) { "Vector3f cloning has failed! " }

    requireEquality(v1 / scalar, Vector3f(x1 / scalar, y1 / scalar, z1 / scalar), tester) { "Vector3f scalar division has failed! " }
    requireEquality(v1 / v2, Vector3f(x1 / x2, y1 / y2, z1 / z2), tester) { "Vector3f / Vector3f division has failed! " }
    
    val plusAssign = v1.deepCopy()
    plusAssign += v2
    requireEquality(v1 + v2, plusAssign) { "Vector3f addition&assign has failed!" }

    val minusAssign = v1.deepCopy()
    minusAssign -= v2
    requireEquality(v1 - v2, minusAssign) { "Vector3f subtraction&assign has failed!" }

    val timesAssign = v1.deepCopy()
    timesAssign *= scalar
    requireEquality(v1 * scalar, timesAssign) { "Vector3f multiplication&assign has failed!" }

    val divAssign = v1.deepCopy()
    divAssign /= scalar
    requireEquality(v1 / scalar, divAssign) { "Vector3f division&assign has failed!" }

    requireEquality(v1, Vector3f(x1, y1 ,z1)) { "v1 has been mutated!" }
    requireEquality(v2, Vector3f(x2, y2, z2)) { "v2 has been mutated!" }

    val vec3i = Vec3i(x1.toInt(), y1.toInt(), z1.toInt())
    val vector3d = Vector3d(x1.toDouble(), y1.toDouble(), z1.toDouble())
    val vec3 = Vec3(x1.toDouble(), y1.toDouble(), z1.toDouble())

    requireEquality(v1, vec3i.toVector3f()) { "Vec3i -> Vector3f conversion has failed!" }
    requireEquality(v1, vector3d.toVector3f()) { "Vector3f -> Vector3f conversion has failed!" }
    requireEquality(v1, vec3.toVector3f()) { "Vec3 -> Vector3f conversion has failed!" }

    KFFLibTest.LOGGER.info("Vector3f test succeed")
}

internal fun testVector4f() {
    val (x1, y1, z1, w1) = listOf(nextFloat(), nextFloat(), nextFloat(), nextFloat())
    val (x2, y2, z2, w2) = listOf(nextFloat(), nextFloat(), nextFloat(), nextFloat())
    val tester: (Vector4fc, Vector4fc) -> Boolean = { a: Vector4fc, b: Vector4fc ->
        a.distanceSquared(b) < 0.01
    }

    val v1: Vector4fc = Vector4f(x1, y1, z1, w1)
    val v2: Vector4fc = Vector4f(x2, y2, z2, w2)

    val (a, b, c, d) = v1

    requireEquality(a, x1) { "Vector4f deconstruction for x has failed!" }
    requireEquality(b, y1) { "Vector4f deconstruction for y has failed!" }
    requireEquality(c, z1) { "Vector4f deconstruction for z has failed!" }
    requireEquality(d, w1) { "Vector4f deconstruction for w has failed!" }

    requireEquality(v1[0], x1) { "Vector4f index access for x has failed!" }
    requireEquality(v1[1], y1) { "Vector4f index access for y has failed!" }
    requireEquality(v1[2], z1) { "Vector4f index access for z has failed!" }
    requireEquality(v1[3], w1) { "Vector4f index access for z has failed!" }

    val v3 = Vector4f()

    for ((index, element) in v1.iterator().withIndex()) {
        if (index == 0) {
            requireEquality(x1, element) { "Vector4f iterator x has failed! "}
        }
        if (index == 1) {
            requireEquality(y1, element) { "Vector4f iterator y has failed! "}
        }
        if (index == 2) {
            requireEquality(z1, element) { "Vector4f iterator z has failed! "}
        }
        if (index == 3) {
            requireEquality(w1, element) { "Vector4f iterator w has failed! "}
        }
        if (index >= 4) {
            throw IllegalStateException("Vector4f has no 5th element!")
        }
        v3[index] = element
    }

    requireEquality(v1, v3) { "Vector4f index setter has failed! "}
    
    requireEquality(v1 + v2, Vector4f(x1 + x2, y1 + y2, z1 + z2, w1 + w2)) { "Vector4f addition has failed!" }
    requireEquality(v1 - v2, Vector4f(x1 - x2, y1 - y2, z1 - z2, w1 - w2)) { "Vector4f subtraction has failed!" }
    requireEquality(-v1, Vector4f(-x1, -y1, -z1, -w1)) { "Vector4f unaryMinus has failed!" }

    val scalar = nextFloat()
    requireEquality(v1 * scalar, Vector4f(x1 * scalar, y1 * scalar, z1 * scalar, w1 * scalar), tester) { "Vector4f scala multiplication has failed! " }
    requireEquality(v1 * v2, Vector4f(x1 * x2, y1 * y2, z1 * z2, w1 * w2), tester) { "Vector4f * Vector4f multiplication has failed! " }

    requireEquality(v1 / scalar, Vector4f(x1 / scalar, y1 / scalar, z1 / scalar, w1 / scalar), tester) { "Vector4f scala division has failed! " }
    requireEquality(v1 / v2, Vector4f(x1 / x2, y1 / y2, z1 / z2, w1 / w2), tester) { "Vector4f / Vector4f division has failed! " }
    
    requireEquality(v1.deepCopy(), v1) { "Vector4f cloning has failed! " }

    requireEquality(v1, Vector4f(x1, y1, z1, w1)) { "v1 has been mutated!" }
    requireEquality(v2, Vector4f(x2, y2, z2, w2)) { "v2 has been mutated!" }

    val plusAssign = v1.deepCopy()
    plusAssign += v2
    requireEquality(v1 + v2, plusAssign) { "Vector4f addition&assign has failed!" }

    val minusAssign = v1.deepCopy()
    minusAssign -= v2
    requireEquality(v1 - v2, minusAssign) { "Vector4f subtraction&assign has failed!" }

    val timesAssign = v1.deepCopy()
    timesAssign *= scalar
    requireEquality(v1 * scalar, timesAssign) { "Vector4f multiplication&assign has failed!" }

    val divAssign = v1.deepCopy()
    divAssign /= scalar
    requireEquality(v1 / scalar, divAssign) { "Vector4f division&assign has failed!" }

    KFFLibTest.LOGGER.info("Vector4f test succeed")
}

internal fun testVector4d() {
    val (x1, y1, z1, w1) = listOf(nextDouble(), nextDouble(), nextDouble(), nextDouble())
    val (x2, y2, z2, w2) = listOf(nextDouble(), nextDouble(), nextDouble(), nextDouble())
    val tester: (Vector4dc, Vector4dc) -> Boolean = { a, b ->
        a.distanceSquared(b) < 0.01
    }

    val v1: Vector4dc = Vector4d(x1, y1, z1, w1)
    val v2: Vector4dc = Vector4d(x2, y2, z2, w2)

    val (a, b, c, d) = v1

    requireEquality(a, x1) { "Vector4d deconstruction for x has failed!" }
    requireEquality(b, y1) { "Vector4d deconstruction for y has failed!" }
    requireEquality(c, z1) { "Vector4d deconstruction for z has failed!" }
    requireEquality(d, w1) { "Vector4d deconstruction for w has failed!" }

    requireEquality(v1[0], x1) { "Vector4d index access for x has failed!" }
    requireEquality(v1[1], y1) { "Vector4d index access for y has failed!" }
    requireEquality(v1[2], z1) { "Vector4d index access for z has failed!" }
    requireEquality(v1[3], w1) { "Vector4d index access for z has failed!" }

    val v3 = Vector4d()

    for ((index, element) in v1.iterator().withIndex()) {
        if (index == 0) {
            requireEquality(x1, element) { "Vector4d iterator x has failed! "}
        }
        if (index == 1) {
            requireEquality(y1, element) { "Vector4d iterator y has failed! "}
        }
        if (index == 2) {
            requireEquality(z1, element) { "Vector4d iterator z has failed! "}
        }
        if (index == 3) {
            requireEquality(w1, element) { "Vector4d iterator w has failed! "}
        }
        if (index >= 4) {
            throw IllegalStateException("Vector4d has no 5th element!")
        }
        v3[index] = element
    }

    requireEquality(v1, v3) { "Vector4d index setter has failed! "}

    requireEquality(v1 + v2, Vector4d(x1 + x2, y1 + y2, z1 + z2, w1 + w2)) { "Vector4d addition has failed!" }
    requireEquality(v1 - v2, Vector4d(x1 - x2, y1 - y2, z1 - z2, w1 - w2)) { "Vector4d subtraction has failed!" }
    requireEquality(-v1, Vector4d(-x1, -y1, -z1, -w1)) { "Vector4d unaryMinus has failed!" }

    val scalar = nextDouble()
    requireEquality(v1 * scalar, Vector4d(x1 * scalar, y1 * scalar, z1 * scalar, w1 * scalar), tester) { "Vector4d scala multiplication has failed! " }
    requireEquality(v1 * v2, Vector4d(x1 * x2, y1 * y2, z1 * z2, w1 * w2), tester) { "Vector4d * Vector4d multiplication has failed! " }

    requireEquality(v1 / scalar, Vector4d(x1 / scalar, y1 / scalar, z1 / scalar, w1 / scalar), tester) { "Vector4d scala division has failed! " }
    requireEquality(v1 / v2, Vector4d(x1 / x2, y1 / y2, z1 / z2, w1 / w2), tester) { "Vector4d / Vector4d division has failed! " }

    requireEquality(v1.deepCopy(), v1) { "Vector4d cloning has failed! " }

    requireEquality(v1, Vector4d(x1, y1, z1, w1)) { "v1 has been mutated!" }
    requireEquality(v2, Vector4d(x2, y2, z2, w2)) { "v2 has been mutated!" }

    val plusAssign = v1.deepCopy()
    plusAssign += v2
    requireEquality(v1 + v2, plusAssign) { "Vector4d addition&assign has failed!" }

    val minusAssign = v1.deepCopy()
    minusAssign -= v2
    requireEquality(v1 - v2, minusAssign) { "Vector4d subtraction&assign has failed!" }

    val timesAssign = v1.deepCopy()
    timesAssign *= scalar
    requireEquality(v1 * scalar, timesAssign) { "Vector4d multiplication&assign has failed!" }

    val divAssign = v1.deepCopy()
    divAssign /= scalar
    requireEquality(v1 / scalar, divAssign) { "Vector4d division&assign has failed!" }

    KFFLibTest.LOGGER.info("Vector4d test succeed")
}

private fun nextInt() = Random.nextInt(-5000..5000)

private fun nextFloat() = nextInt().toFloat()

private fun nextDouble() = nextInt().toDouble()

private fun <T: Any> requireEquality(a: T, b: T, tester: (T, T) -> Boolean = { f, s -> f == s}, message: () -> String) {
    if (!tester(a, b)) {
        throw IllegalStateException("${message()}\nLeft: $a\nRight: $b")
    }
}
