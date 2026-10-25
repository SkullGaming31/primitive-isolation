package com.canadiendragon.primitiveisolation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

final class CorpseRenderRotations {
    private CorpseRenderRotations() {
    }

    static void rotate(PoseStack poseStack, float pivot) {
        poseStack.translate(0.0F, pivot, 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.translate(0.0F, -pivot, 0.0F);
    }
}
