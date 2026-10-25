package com.canadiendragon.primitiveisolation;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

public class DeerCorpseRenderer extends MobRenderer<DeerCorpse, LivingEntityRenderState, WildlifeModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            primitiveisolation.MODID, "textures/entity/deer.png");

    public DeerCorpseRenderer(EntityRendererProvider.Context context) {
        super(context, new WildlifeModel(context.bakeLayer(WildlifeModel.DEER_LAYER)), 0.2F);
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

    @Override
    public void submit(
            LivingEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}
