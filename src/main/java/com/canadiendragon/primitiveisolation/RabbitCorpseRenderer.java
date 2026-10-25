package com.canadiendragon.primitiveisolation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RabbitRenderer;
import net.minecraft.client.renderer.entity.state.RabbitRenderState;

public class RabbitCorpseRenderer extends RabbitRenderer {
    public RabbitCorpseRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void setupRotations(RabbitRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        CorpseRenderRotations.rotate(poseStack, 0.25F);
    }
}
