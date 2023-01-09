package thedarkcolour.kfflibtest

import com.mojang.math.Vector3d
import com.mojang.math.Vector3f
import com.mojang.math.Vector4f
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import thedarkcolour.kotlinforforge.forge.vectorutil.*
import kotlin.random.Random

internal fun testVec2() {
    val x1 = Random.nextFloat()
    val y1 = Random.nextFloat()

    val x2 = Random.nextFloat()
    val y2 = Random.nextFloat()

    val v1 = Vec2(x1, y1)
    val v2 = Vec2(x2, y2)

    val (a, b) = v1
    assert(a == x1) { "Vec2 deconstruction for x has failed!" }
    assert(b == y1) { "Vec2 deconstruction for y has failed!" }
    assert(v1 + v2 == Vec2(x1 + x2, y1 + y2)) { "Vec2 addition has failed!" }
    assert(v1 - v2 == Vec2(x1 - x2, y1 - y2)) { "Vec2 subtraction has failed!" }
    assert(-v1 == Vec2(-x1, -y1)) { "Vec2 unaryMinus has failed!" }
    assert(v1 * 2.2F == Vec2(x1 * 2.2F, y1 * 2.2F)) { "Vec2 multiplication has failed! " }
    assert(v1.clone() == v1) { "Vec2 cloning has failed! " }

    assert(v1 == Vec2(x1, y1)) { "v1 has been mutated!" }
    assert(v2 == Vec2(x2, y2)) { "v2 has been mutated!" }

    KFFLibTest.LOGGER.info("Vec2 test succeed")
}

internal fun testVec3i() {
    val (x1, y1, z1) = listOf(Random.nextInt(), Random.nextInt(), Random.nextInt())
    val (x2, y2, z2) = listOf(Random.nextInt(), Random.nextInt(), Random.nextInt())

    val v1 = Vec3i(x1, y1, z1)
    val v2 = Vec3i(x2, y2, z2)

    val (a, b, c) = v1

    assert(a == x1) { "Vec3i deconstruction for x has failed!" }
    assert(b == y1) { "Vec3i deconstruction for y has failed!" }
    assert(c == z1) { "Vec3i deconstruction for z has failed!" }
    assert(v1 + v2 == Vec3i(x1 + x2, y1 + y2, z1 + z2)) { "Vec3i addition has failed!" }
    assert(v1 - v2 == Vec3i(x1 - x2, y1 - y2, z1 - z2)) { "Vec3i subtraction has failed!" }
    assert(-v1 == Vec3i(-x1, -y1, -z1)) { "Vec3i unaryMinus has failed!" }
    assert(v1 * 23 == Vec3i(x1 * 23, y2 * 23, z2 * 23)) { "Vec3i multiplication has failed! " }
    assert(v1.clone() == Vec3i(x1, y1, z1)) { "Vec3i cloning has failed! " }

    assert(v1 == Vec3i(x1, y1, z1)) { "v1 has been mutated!" }
    assert(v2 == Vec3i(x2, x2, x2)) { "v2 has been mutated!" }

    val vec3 = Vec3(x1.toDouble(), y1.toDouble(), z1.toDouble())
    val vector3f = Vector3f(x1.toFloat(), y1.toFloat(), z1.toFloat())
    val vector3d = Vector3d(x1.toDouble(), y1.toDouble(), z1.toDouble())

    assert(v1 == vec3.toVec3i()) { "Vec3 -> Vec3i conversion has failed!" }
    assert(v1 == vector3f.toVec3i()) { "Vector3f -> Vec3i conversion has failed!" }
    assert(v1 == vector3d.toVec3i()) { "Vector3d -> Vec3i conversion has failed!" }

    KFFLibTest.LOGGER.info("Vec3i test succeed")
}

internal fun testVec3() {
    val (x1, y1, z1) = listOf(Random.nextDouble(), Random.nextDouble(), Random.nextDouble())
    val (x2, y2, z2) = listOf(Random.nextDouble(), Random.nextDouble(), Random.nextDouble())

    val v1 = Vec3(x1, y1, z1)
    val v2 = Vec3(x2, y2, z2)

    val (a, b, c) = v1

    assert(a == x1) { "Vec3 deconstruction for x has failed!" }
    assert(b == y1) { "Vec3 deconstruction for y has failed!" }
    assert(c == z1) { "Vec3 deconstruction for z has failed!" }
    assert(v1 + v2 == Vec3(x1 + x2, y1 + y2, z1 + z2)) { "Vec3 addition has failed!" }
    assert(v1 - v2 == Vec3(x1 - x2, y1 - y2, z1 - z2)) { "Vec3 subtraction has failed!" }
    assert(-v1 == Vec3(-x1, -y1, -z1)) { "Vec3 unaryMinus has failed!" }
    assert(v1 * 23.0 == Vec3(x1 * 23.0, y1 * 23.0, z1 * 23.0)) { "Vec3 multiplication has failed! " }
    assert(v1.clone() == v1) { "Vec3 cloning has failed! " }

    assert(v1 == Vec3(x1, y1, z1)) { "v1 has been mutated!" }
    assert(v2 == Vec3(x2, y2, z2)) { "v2 has been mutated!" }

    val vec3i = Vec3i(x1, y1, z1)
    val vector3f = Vector3f(x1.toFloat(), y1.toFloat(), z1.toFloat())
    val vector3d = Vector3d(x1, y1, z1)

    assert(v1 == vec3i.toVec3()) { "Vec3i -> Vec3 conversion has failed!" }
    assert(v1 == vector3f.toVec3()) { "Vector3f -> Vec3 conversion has failed!" }
    assert(v1 == vector3d.toVec3()) { "Vector3d -> Vec3 conversion has failed!" }

    KFFLibTest.LOGGER.info("Vec3 test succeed")
}

internal fun testVector3d() {
    val (x1, y1, z1) = listOf(Random.nextDouble(), Random.nextDouble(), Random.nextDouble())
    val (x2, y2, z2) = listOf(Random.nextDouble(), Random.nextDouble(), Random.nextDouble())

    val v1 = Vector3d(x1, y1, z1)
    val v2 = Vector3d(x2, y2, z2)

    val (a, b, c) = v1

    assert(a == x1) { "Vector3d deconstruction for x has failed!" }
    assert(b == y1) { "Vector3d deconstruction for y has failed!" }
    assert(c == z1) { "Vector3d deconstruction for z has failed!" }
    assert(v1 + v2 == Vector3d(x1 + x2, y1 + y2, z1 + z2)) { "Vector3d addition has failed!" }
    assert(v1 - v2 == Vector3d(x1 - x2, y1 - y2, z1 - z2)) { "Vector3d subtraction has failed!" }
    assert(-v1 == Vector3d(-x1, -y1, -z1)) { "Vector3d unaryMinus has failed!" }
    assert(v1 * 23.0 == Vector3d(x1 * 23.0, y1 * 23.0, z1 * 23.0)) { "Vector3d multiplication has failed! " }
    assert(v1.clone() == v1) { "Vector3d cloning has failed! " }

    val plusAssign = v1.clone()
    plusAssign += v2
    assert(v1 + v2 == plusAssign) { "Vector3d addition&assign has failed!" }

    val minusAssign = v1.clone()
    minusAssign -= v2
    assert(v1 - v2 == minusAssign) { "Vector3d subtraction&assign has failed!" }

    val timesAssign = v1.clone()
    timesAssign *= 23.0
    assert(v1 * 23.0 == timesAssign) { "Vector3d multiplication&assign has failed!" }

    assert(v1 == Vector3d(x1, y1, z1)) { "v1 has been mutated!" }
    assert(v2 == Vector3d(x2, y2, z2)) { "v2 has been mutated!" }

    val vec3i = Vec3i(x1, y1, z1)
    val vector3f = Vector3f(x1.toFloat(), y1.toFloat(), z1.toFloat())
    val vec3 = Vec3(x1, y1, z1)

    assert(v1 == vec3i.toVector3d()) { "Vec3i -> Vector3d conversion has failed!" }
    assert(v1 == vector3f.toVector3d()) { "Vector3f -> Vector3d conversion has failed!" }
    assert(v1 == vec3.toVector3d()) { "Vec3 -> Vector3d conversion has failed!" }

    KFFLibTest.LOGGER.info("Vector3d test succeed")
}

internal fun testVector3f() {
    val (x1, y1, z1) = listOf(Random.nextFloat(), Random.nextFloat(), Random.nextFloat())
    val (x2, y2, z2) = listOf(Random.nextFloat(), Random.nextFloat(), Random.nextFloat())

    val v1 = Vector3f(x1, y1, z1)
    val v2 = Vector3f(x2, y2, z2)

    val (a, b, c) = v1

    assert(a == x1) { "Vector3f deconstruction for x has failed!" }
    assert(b == y1) { "Vector3f deconstruction for y has failed!" }
    assert(c == z1) { "Vector3f deconstruction for z has failed!" }
    assert(v1 + v2 == Vector3f(x1 + x2, y1 + y2, z1 + z2)) { "Vector3f addition has failed!" }
    assert(v1 - v2 == Vector3f(x1 - x2, y1 - y2, z1 - z2)) { "Vector3f subtraction has failed!" }
    assert(-v1 == Vector3f(-x1, -y1, -z1)) { "Vector3f unaryMinus has failed!" }
    assert(v1 * 23.0F == Vector3f(x1 * 23F, y1 * 23F, z1 * 23F)) { "Vector3f multiplication has failed! " }
    assert(v1.clone() == v1) { "Vector3f cloning has failed! " }

    val plusAssign = v1.clone()
    plusAssign += v2
    assert(v1 + v2 == plusAssign) { "Vector3f addition&assign has failed!" }

    val minusAssign = v1.clone()
    minusAssign -= v2
    assert(v1 - v2 == minusAssign) { "Vector3f subtraction&assign has failed!" }

    val timesAssign = v1.clone()
    timesAssign *= 23.0F
    assert(v1 * 23.0F == timesAssign) { "Vector3f multiplication&assign has failed!" }

    assert(v1 == Vector3f(x1, y1 ,z1)) { "v1 has been mutated!" }
    assert(v2 == Vector3f(x2, y2, z2)) { "v2 has been mutated!" }

    val vec3i = Vec3i(x1.toInt(), y1.toInt(), z1.toInt())
    val vector3d = Vector3d(x1.toDouble(), y1.toDouble(), z1.toDouble())
    val vec3 = Vec3(x1.toDouble(), y1.toDouble(), z1.toDouble())

    assert(v1 == vec3i.toVector3f()) { "Vec3i -> Vector3f conversion has failed!" }
    assert(v1 == vector3d.toVector3f()) { "Vector3f -> Vector3f conversion has failed!" }
    assert(v1 == vec3.toVector3f()) { "Vec3 -> Vector3f conversion has failed!" }

    KFFLibTest.LOGGER.info("Vector3f test succeed")
}

internal fun testVector4f() {
    val (x1, y1, z1, w1) = listOf(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), Random.nextFloat())
    val (x2, y2, z2, w2) = listOf(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), Random.nextFloat())

    val v1 = Vector4f(x1, y1, z1, w1)
    val v2 = Vector4f(x2, y2, z2, w2)

    val (a, b, c, d) = v1

    assert(a == x1) { "Vector4f deconstruction for x has failed!" }
    assert(b == y1) { "Vector4f deconstruction for y has failed!" }
    assert(c == z1) { "Vector4f deconstruction for z has failed!" }
    assert(d == w1) { "Vector4f deconstruction for w has failed!" }
    assert(v1 + v2 == Vector4f(x1 + x2, y1 + y2, z1 + z2, w1 + w2)) { "Vector4f addition has failed!" }
    assert(v1 - v2 == Vector4f(x1 - x2, y1 - y2, z1 - z2, w1 - w2)) { "Vector4f subtraction has failed!" }
    assert(-v1 == Vector4f(-x1, -y1, -z1, -w1)) { "Vector4f unaryMinus has failed!" }
    assert(v1 * 23.0F == Vector4f(x1 * 23, y1 * 23, z1 * 23, w1 * 23)) { "Vector4f multiplication has failed! " }
    assert(v1.clone() == v1) { "Vector4f cloning has failed! " }

    assert(v1 == Vector4f(x1, y1, z1, w1)) { "v1 has been mutated!" }
    assert(v2 == Vector4f(x2, y2, z2, w2)) { "v2 has been mutated!" }

    val plusAssign = v1.clone()
    plusAssign += v2
    assert(v1 + v2 == plusAssign) { "Vector4f addition&assign has failed!" }

    val minusAssign = v1.clone()
    minusAssign -= v2
    assert(v1 - v2 == minusAssign) { "Vector4f subtraction&assign has failed!" }

    val timesAssign = v1.clone()
    timesAssign *= 23.0F
    assert(v1 * 23.0F == timesAssign) { "Vector4f multiplication&assign has failed!" }

    KFFLibTest.LOGGER.info("Vector4f test succeed")
}
