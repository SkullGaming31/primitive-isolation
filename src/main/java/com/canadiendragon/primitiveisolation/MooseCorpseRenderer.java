package com.canadiendragon.primitiveisolation;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class MooseCorpseRenderer extends MobRenderer<MooseCorpse, LivingEntityRenderState, WildlifeModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            primitiveisolation.MODID, "textures/entity/moose.png");

    public MooseCorpseRenderer(EntityRendererProvider.Context context) {
        super(context, new WildlifeModel(context.bakeLayer(WildlifeModel.MOOSE_LAYER)), 0.5F);
    }

    @Override
    protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        poseStack.scale(2.0833333F, 2.0833333F, 2.0833333F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void setupRotations(LivingEntityRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        CorpseRenderRotations.rotate(poseStack, 0.45F);
    }
}
