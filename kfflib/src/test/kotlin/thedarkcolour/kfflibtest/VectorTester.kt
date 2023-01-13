package thedarkcolour.kfflibtest

import com.mojang.math.Vector3d
import com.mojang.math.Vector3f
import com.mojang.math.Vector4f
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import thedarkcolour.kotlinforforge.forge.vectorutil.*
import java.lang.IllegalStateException
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

    // I can't believe Vec2#equals does not override Object#equals
    // It's entirely separate method with same name
    requireEquality(v1 + v2, Vec2(x1 + x2, y1 + y2), Vec2::equals) { "Vec2 addition has failed!" }
    requireEquality(v1 - v2, Vec2(x1 - x2, y1 - y2), Vec2::equals) { "Vec2 subtraction has failed!" }
    requireEquality(-v1, Vec2(-x1, -y1), Vec2::equals) { "Vec2 unaryMinus has failed!" }

    val scalar = nextFloat()
    requireEquality(v1 * scalar, Vec2(x1 * scalar, y1 * scalar), Vec2::equals) { "Vec2 multiplication has failed! " }
    requireEquality(v1.clone(), v1, Vec2::equals) { "Vec2 cloning has failed! " }

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
    requireEquality(v1 + v2, Vec3i(x1 + x2, y1 + y2, z1 + z2)) { "Vec3i addition has failed!" }
    requireEquality(v1 - v2, Vec3i(x1 - x2, y1 - y2, z1 - z2)) { "Vec3i subtraction has failed!" }
    requireEquality(-v1, Vec3i(-x1, -y1, -z1)) { "Vec3i unaryMinus has failed!" }

    val scalar = nextInt()
    requireEquality(v1 * scalar, Vec3i(x1 * scalar, y1 * scalar, z1 * scalar)) { "Vec3i multiplication has failed! " }
    requireEquality(v1.clone(), Vec3i(x1, y1, z1)) { "Vec3i cloning has failed! " }

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
    requireEquality(v1 + v2, Vec3(x1 + x2, y1 + y2, z1 + z2)) { "Vec3 addition has failed!" }
    requireEquality(v1 - v2, Vec3(x1 - x2, y1 - y2, z1 - z2)) { "Vec3 subtraction has failed!" }
    requireEquality(-v1, Vec3(-x1, -y1, -z1)) { "Vec3 unaryMinus has failed!" }

    val scalar = nextDouble()
    requireEquality(v1 * scalar, Vec3(x1 * scalar, y1 * scalar, z1 * scalar)) { "Vec3 multiplication has failed! " }
    requireEquality(v1.clone(), v1) { "Vec3 cloning has failed! " }

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
    val tester = { a: Vector3d, b: Vector3d ->
        a.x == b.x && a.y == b.y && a.z == b.z
    }

    val (x1, y1, z1) = listOf(nextDouble(), nextDouble(), nextDouble())
    val (x2, y2, z2) = listOf(nextDouble(), nextDouble(), nextDouble())

    val v1 = Vector3d(x1, y1, z1)
    val v2 = Vector3d(x2, y2, z2)

    val (a, b, c) = v1

    requireEquality(a, x1) { "Vector3d deconstruction for x has failed!" }
    requireEquality(b, y1) { "Vector3d deconstruction for y has failed!" }
    requireEquality(c, z1) { "Vector3d deconstruction for z has failed!" }

    // Oh my god this one doesn't even have equals defined
    requireEquality(v1 + v2,  Vector3d(x1 + x2, y1 + y2, z1 + z2), tester) { "Vector3d addition has failed!" }
    requireEquality(v1 - v2, Vector3d(x1 - x2, y1 - y2, z1 - z2), tester) { "Vector3d subtraction has failed!" }
    requireEquality(-v1, Vector3d(-x1, -y1, -z1), tester) { "Vector3d unaryMinus has failed!" }

    val scalar = nextDouble()
    requireEquality(v1 * scalar, Vector3d(x1 * scalar, y1 * scalar, z1 * scalar), tester) { "Vector3d multiplication has failed! " }
    requireEquality(v1.clone(), v1, tester) { "Vector3d cloning has failed! " }

    val plusAssign = v1.clone()
    plusAssign += v2
    requireEquality(v1 + v2, plusAssign, tester) { "Vector3d addition&assign has failed!" }

    val minusAssign = v1.clone()
    minusAssign -= v2
    requireEquality(v1 - v2, minusAssign, tester) { "Vector3d subtraction&assign has failed!" }

    val timesAssign = v1.clone()
    timesAssign *= scalar
    requireEquality(v1 * scalar, timesAssign, tester) { "Vector3d multiplication&assign has failed!" }

    requireEquality(v1, Vector3d(x1, y1, z1), tester) { "v1 has been mutated!" }
    requireEquality(v2, Vector3d(x2, y2, z2), tester) { "v2 has been mutated!" }

    val vec3i = Vec3i(x1, y1, z1)
    val vector3f = Vector3f(x1.toFloat(), y1.toFloat(), z1.toFloat())
    val vec3 = Vec3(x1, y1, z1)

    requireEquality(v1, vec3i.toVector3d(), tester) { "Vec3i -> Vector3d conversion has failed!" }
    requireEquality(v1, vector3f.toVector3d(), tester) { "Vector3f -> Vector3d conversion has failed!" }
    requireEquality(v1, vec3.toVector3d(), tester) { "Vec3 -> Vector3d conversion has failed!" }

    KFFLibTest.LOGGER.info("Vector3d test succeed")
}

internal fun testVector3f() {
    val (x1, y1, z1) = listOf(nextFloat(), nextFloat(), nextFloat())
    val (x2, y2, z2) = listOf(nextFloat(), nextFloat(), nextFloat())

    val v1 = Vector3f(x1, y1, z1)
    val v2 = Vector3f(x2, y2, z2)

    val (a, b, c) = v1

    requireEquality(a, x1) { "Vector3f deconstruction for x has failed!" }
    requireEquality(b, y1) { "Vector3f deconstruction for y has failed!" }
    requireEquality(c, z1) { "Vector3f deconstruction for z has failed!" }
    requireEquality(v1 + v2, Vector3f(x1 + x2, y1 + y2, z1 + z2)) { "Vector3f addition has failed!" }
    requireEquality(v1 - v2, Vector3f(x1 - x2, y1 - y2, z1 - z2)) { "Vector3f subtraction has failed!" }
    requireEquality(-v1, Vector3f(-x1, -y1, -z1)) { "Vector3f unaryMinus has failed!" }

    val scalar = nextFloat()
    requireEquality(v1 * scalar, Vector3f(x1 * scalar, y1 * scalar, z1 * scalar)) { "Vector3f multiplication has failed! " }
    requireEquality(v1.clone(), v1) { "Vector3f cloning has failed! " }

    val plusAssign = v1.clone()
    plusAssign += v2
    requireEquality(v1 + v2, plusAssign) { "Vector3f addition&assign has failed!" }

    val minusAssign = v1.clone()
    minusAssign -= v2
    requireEquality(v1 - v2, minusAssign) { "Vector3f subtraction&assign has failed!" }

    val timesAssign = v1.clone()
    timesAssign *= scalar
    requireEquality(v1 * scalar, timesAssign) { "Vector3f multiplication&assign has failed!" }

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

    val v1 = Vector4f(x1, y1, z1, w1)
    val v2 = Vector4f(x2, y2, z2, w2)

    val (a, b, c, d) = v1

    requireEquality(a, x1) { "Vector4f deconstruction for x has failed!" }
    requireEquality(b, y1) { "Vector4f deconstruction for y has failed!" }
    requireEquality(c, z1) { "Vector4f deconstruction for z has failed!" }
    requireEquality(d, w1) { "Vector4f deconstruction for w has failed!" }
    requireEquality(v1 + v2, Vector4f(x1 + x2, y1 + y2, z1 + z2, w1 + w2)) { "Vector4f addition has failed!" }
    requireEquality(v1 - v2, Vector4f(x1 - x2, y1 - y2, z1 - z2, w1 - w2)) { "Vector4f subtraction has failed!" }
    requireEquality(-v1, Vector4f(-x1, -y1, -z1, -w1)) { "Vector4f unaryMinus has failed!" }

    val scalar = nextFloat()
    requireEquality(v1 * scalar, Vector4f(x1 * scalar, y1 * scalar, z1 * scalar, w1 * scalar)) { "Vector4f multiplication has failed! " }
    requireEquality(v1.clone(), v1) { "Vector4f cloning has failed! " }

    requireEquality(v1, Vector4f(x1, y1, z1, w1)) { "v1 has been mutated!" }
    requireEquality(v2, Vector4f(x2, y2, z2, w2)) { "v2 has been mutated!" }

    val plusAssign = v1.clone()
    plusAssign += v2
    requireEquality(v1 + v2, plusAssign) { "Vector4f addition&assign has failed!" }

    val minusAssign = v1.clone()
    minusAssign -= v2
    requireEquality(v1 - v2, minusAssign) { "Vector4f subtraction&assign has failed!" }

    val timesAssign = v1.clone()
    timesAssign *= scalar
    requireEquality(v1 * scalar, timesAssign) { "Vector4f multiplication&assign has failed!" }

    KFFLibTest.LOGGER.info("Vector4f test succeed")
}

private fun nextInt() = Random.nextInt(-5000..5000)

private fun nextFloat() = nextInt().toFloat()

private fun nextDouble() = nextInt().toDouble()

private fun <T: Any> requireEquality(a: T, b: T, tester: (T, T) -> Boolean = { f, s -> f == s}, message: () -> String) {
    if (!tester(a, b)) {
        throw IllegalStateException("${message()}\nLeft: $a\nRight: $b")
    }
}
