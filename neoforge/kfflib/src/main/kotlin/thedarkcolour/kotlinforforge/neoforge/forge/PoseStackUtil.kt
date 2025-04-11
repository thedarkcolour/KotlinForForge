package thedarkcolour.kotlinforforge.neoforge.forge

import com.mojang.blaze3d.vertex.PoseStack
import org.joml.Quaternionf

public inline fun <R> PoseStack.use(toRun: (PoseStack) -> R): R {
    pushPose()
    val result = toRun(this)
    popPose()
    return result
}

public inline fun <R> PoseStack.use(toRun: () -> R): R = use { _ -> toRun() }

public operator fun PoseStack.timesAssign(matrix: Quaternionf): Unit = mulPose(matrix)
