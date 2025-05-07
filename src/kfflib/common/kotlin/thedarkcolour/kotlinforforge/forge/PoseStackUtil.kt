package thedarkcolour.kotlinforforge.forge

import com.mojang.blaze3d.vertex.PoseStack
import org.joml.Quaternionf

public fun PoseStack.use(toRun: () -> Unit) {
    pushPose()
    toRun()
    popPose()
}

public operator fun PoseStack.timesAssign(matrix: Quaternionf): Unit = mulPose(matrix)
