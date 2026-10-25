package com.canadiendragon.primitiveisolation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.client.renderer.entity.state.SheepRenderState;

public class SheepCorpseRenderer extends SheepRenderer {
    public SheepCorpseRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void setupRotations(SheepRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        CorpseRenderRotations.rotate(poseStack, 0.45F);
    }
}
