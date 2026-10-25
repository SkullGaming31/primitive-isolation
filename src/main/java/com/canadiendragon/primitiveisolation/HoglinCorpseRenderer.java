package com.canadiendragon.primitiveisolation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HoglinRenderer;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;

public class HoglinCorpseRenderer extends HoglinRenderer {
    public HoglinCorpseRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void setupRotations(HoglinRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        CorpseRenderRotations.rotate(poseStack, 0.45F);
    }
}
