package com.canadiendragon.primitiveisolation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MushroomCowRenderer;
import net.minecraft.client.renderer.entity.state.MushroomCowRenderState;

public class MooshroomCorpseRenderer extends MushroomCowRenderer {
    public MooshroomCorpseRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void setupRotations(MushroomCowRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        CorpseRenderRotations.rotate(poseStack, 0.45F);
    }
}
