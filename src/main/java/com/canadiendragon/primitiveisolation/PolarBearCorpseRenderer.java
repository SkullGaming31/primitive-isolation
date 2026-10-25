package com.canadiendragon.primitiveisolation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PolarBearRenderer;
import net.minecraft.client.renderer.entity.state.PolarBearRenderState;

public class PolarBearCorpseRenderer extends PolarBearRenderer {
    public PolarBearCorpseRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void setupRotations(PolarBearRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        CorpseRenderRotations.rotate(poseStack, 0.45F);
    }
}
