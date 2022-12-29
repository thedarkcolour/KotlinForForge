package thedarkcolour.kfflibtest

import com.mojang.math.Vector3d
import com.mojang.math.Vector3f
import com.mojang.math.Vector4f
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import thedarkcolour.kotlinforforge.forge.vectorutil.*

internal fun testVec2() {
    val v1 = Vec2(1.0F, 1.0F)
    val v2 = Vec2(2.0F, 2.0F)

    assert(v1 + v2 == Vec2(3.0F, 3.0F)) { "Vec2 addition has failed!" }
    assert(v1 - v2 == Vec2(-1.0F, -1.0F)) { "Vec2 subtraction has failed!" }
    assert(-v1 == Vec2(-1.0F, -1.0F)) { "Vec2 unaryMinus has failed!" }
    assert(v1 * 2.2F == Vec2(2.2F, 2.2F)) { "Vec2 multiplication has failed! " }
    assert(v1.clone() == v1) { "Vec2 cloning has failed! " }

    assert(v1 == Vec2(1.0F, 1.0F)) { "v1 has been mutated!" }
    assert(v2 == Vec2(2.0F, 2.0F)) { "v1 has been mutated!" }

    KFFLibTest.LOGGER.info("Vec2 test succeed")
}

internal fun testVec3i() {
    val v1 = Vec3i(1, 1, 1)
    val v2 = Vec3i(2, 2, 2)

    assert(v1 + v2 == Vec3i(3, 3, 3)) { "Vec3i addition has failed!" }
    assert(v1 - v2 == Vec3i(-1, -1, -1)) { "Vec3i subtraction has failed!" }
    assert(-v1 == Vec3i(-1, -1, -1)) { "Vec3i unaryMinus has failed!" }
    assert(v1 * 23 == Vec3i(23, 23, 23)) { "Vec3i multiplication has failed! " }
    assert(v1 dot v2 == 6) { "Vec3i dot product has failed! " }
    assert(v1.clone() == v1) { "Vec3i cloning has failed! " }

    assert(v1 == Vec3i(1, 1, 1)) { "v1 has been mutated!" }
    assert(v2 == Vec3i(2, 2, 2)) { "v1 has been mutated!" }

    KFFLibTest.LOGGER.info("Vec3i test succeed")
}

internal fun testVec3() {
    val v1 = Vec3(1.0, 1.0, 1.0)
    val v2 = Vec3(2.0, 2.0, 2.0)

    assert(v1 + v2 == Vec3(3.0, 3.0, 3.0)) { "Vec3 addition has failed!" }
    assert(v1 - v2 == Vec3(-1.0, -1.0, -1.0)) { "Vec3 subtraction has failed!" }
    assert(-v1 == Vec3(-1.0, -1.0, -1.0)) { "Vec3 unaryMinus has failed!" }
    assert(v1 * 23.0 == Vec3(23.0, 23.0, 23.0)) { "Vec3 multiplication has failed! " }
    assert(v1.clone() == v1) { "Vec3 cloning has failed! " }

    assert(v1 == Vec3(1.0, 1.0, 1.0)) { "v1 has been mutated!" }
    assert(v2 == Vec3(2.0, 2.0, 2.0)) { "v1 has been mutated!" }

    KFFLibTest.LOGGER.info("Vec3 test succeed")
}

internal fun testVector3d() {
    val v1 = Vector3d(0.0, 13.0, 21.0)
    val v2 = Vector3d(-17.0, -42.0, 23.0)

    assert(v1 + v2 == Vector3d(-17.0, -29.0, 44.0)) { "Vector3d addition has failed!" }
    assert(v1 - v2 == Vector3d(17.0, 55.0, -2.0)) { "Vector3d subtraction has failed!" }
    assert(-v1 == Vector3d(0.0, -13.0, -21.0)) { "Vector3d unaryMinus has failed!" }
    assert(v1 * 23.0 == Vector3d(0.0, 299.0, 483.0)) { "Vector3d multiplication has failed! " }
    assert(v1 dot v2 == -63.0) { "Vector3d dot product has failed! " }
    assert(v1 cross v2 == Vector3d(1181.0, -357.0, 221.0)) { "Vector3d multiplication has failed! " }
    assert(v1.clone() == v1) { "Vector3d cloning has failed! " }

    assert(v1 == Vector3d(0.0, 13.0, 21.0)) { "v1 has been mutated!" }
    assert(v2 == Vector3d(-17.0, -42.0, 23.0)) { "v1 has been mutated!" }

    val plusAssign = v1.clone()
    plusAssign += v2
    assert(v1 + v2 == plusAssign) { "Vector3d addition&assign has failed!" }

    val minusAssign = v1.clone()
    minusAssign -= v2
    assert(v1 - v2 == minusAssign) { "Vector3d subtraction&assign has failed!" }

    val timesAssign = v1.clone()
    timesAssign *= 23.0
    assert(v1 * 23.0 == timesAssign) { "Vector3d multiplication&assign has failed!" }

    KFFLibTest.LOGGER.info("Vector3d test succeed")
}

internal fun testVector3f() {
    val v1 = Vector3f(0.0F, 13.0F, 21.0F)
    val v2 = Vector3f(-17.0F, -42.0F, 23.0F)

    assert(v1 + v2 == Vector3f(-17.0F, -29.0F, 44.0F)) { "Vector3f addition has failed!" }
    assert(v1 - v2 == Vector3f(17.0F, 55.0F, -2.0F)) { "Vector3f subtraction has failed!" }
    assert(-v1 == Vector3f(0.0F, -13.0F, -21.0F)) { "Vector3f unaryMinus has failed!" }
    assert(v1 * 23.0F == Vector3f(0.0F, 299.0F, 483.0F)) { "Vector3f multiplication has failed! " }
    assert(v1.clone() == v1) { "Vector3f cloning has failed! " }

    assert(v1 == Vector3f(0.0F, 13.0F, 21.0F)) { "v1 has been mutated!" }
    assert(v2 == Vector3f(-17.0F, -42.0F, 23.0F)) { "v1 has been mutated!" }

    val plusAssign = v1.clone()
    plusAssign += v2
    assert(v1 + v2 == plusAssign) { "Vector3f addition&assign has failed!" }

    val minusAssign = v1.clone()
    minusAssign -= v2
    assert(v1 - v2 == minusAssign) { "Vector3f subtraction&assign has failed!" }

    val timesAssign = v1.clone()
    timesAssign *= 23.0F
    assert(v1 * 23.0F == timesAssign) { "Vector3f multiplication&assign has failed!" }

    KFFLibTest.LOGGER.info("Vector3f test succeed")
}

internal fun testVector4f() {
    val v1 = Vector4f(0.0F, 13.0F, 21.0F, -25.0F)
    val v2 = Vector4f(-17.0F, -42.0F, 23.0F, -13.0F)

    assert(v1 + v2 == Vector4f(-17.0F, -29.0F, 44.0F, -38F)) { "Vector4f addition has failed!" }
    assert(v1 - v2 == Vector4f(17.0F, 55.0F, -2.0F, -12F)) { "Vector4f subtraction has failed!" }
    assert(-v1 == Vector4f(0.0F, -13.0F, -21.0F, 25F)) { "Vector4f unaryMinus has failed!" }
    assert(v1 * 23.0F == Vector4f(0.0F, 299.0F, 483.0F, -575.0F)) { "Vector4f multiplication has failed! " }
    assert(v1.clone() == v1) { "Vector4f cloning has failed! " }

    assert(v1 == Vector4f(0.0F, 13.0F, 21.0F, -25.0F)) { "v1 has been mutated!" }
    assert(v2 == Vector4f(-17.0F, -42.0F, 23.0F, -13.0F)) { "v1 has been mutated!" }

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