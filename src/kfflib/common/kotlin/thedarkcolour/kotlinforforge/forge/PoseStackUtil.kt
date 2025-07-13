package thedarkcolour.kotlinforforge.forge

import com.mojang.blaze3d.vertex.PoseStack
import org.joml.Quaternionf

public fun PoseStack.use(toRun: PoseStack.() -> Unit) {
    pushPose()
    this.toRun()
    popPose()
}

public operator fun PoseStack.timesAssign(matrix: Quaternionf): Unit = mulPose(matrix)
