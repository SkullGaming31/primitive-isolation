package com.canadiendragon.primitiveisolation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.state.PigRenderState;

public class PigCorpseRenderer extends PigRenderer {
    public PigCorpseRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void setupRotations(PigRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        poseStack.translate(0.0F, 0.45F, 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.translate(0.0F, -0.45F, 0.0F);
    }
}
