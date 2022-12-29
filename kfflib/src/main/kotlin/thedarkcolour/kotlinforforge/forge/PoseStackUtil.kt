package thedarkcolour.kotlinforforge.forge

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Quaternion

public fun PoseStack.use(toRun: () -> Unit) {
    pushPose()
    toRun()
    popPose()
}

public operator fun PoseStack.timesAssign(matrix: Quaternion): Unit = mulPose(matrix)
