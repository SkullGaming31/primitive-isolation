package com.canadiendragon.primitiveisolation;

import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.AdultAndBabyModelPair;
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.cow.CowVariant;

public class CowCorpseRenderer extends MobRenderer<CowCorpse, CowRenderState, CowModel> {
    private final Map<CowVariant.ModelType, AdultAndBabyModelPair<CowModel>> models;

    public CowCorpseRenderer(EntityRendererProvider.Context context) {
        super(context, new CowModel(context.bakeLayer(ModelLayers.COW)), 0.7F);
        this.models = Map.of(
                CowVariant.ModelType.NORMAL,
                new AdultAndBabyModelPair<>(new CowModel(context.bakeLayer(ModelLayers.COW)), new CowModel(context.bakeLayer(ModelLayers.COW_BABY))),
                CowVariant.ModelType.WARM,
                new AdultAndBabyModelPair<>(new CowModel(context.bakeLayer(ModelLayers.WARM_COW)), new CowModel(context.bakeLayer(ModelLayers.WARM_COW_BABY))),
                CowVariant.ModelType.COLD,
                new AdultAndBabyModelPair<>(new CowModel(context.bakeLayer(ModelLayers.COLD_COW)), new CowModel(context.bakeLayer(ModelLayers.COLD_COW_BABY)))
        );
    }

    @Override
    public Identifier getTextureLocation(CowRenderState state) {
        return state.variant == null
                ? MissingTextureAtlasSprite.getLocation()
                : (state.isBaby ? state.variant.babyTexture().texturePath() : state.variant.modelAndTexture().asset().texturePath());
    }

    @Override
    public CowRenderState createRenderState() {
        return new CowRenderState();
    }

    @Override
    public void extractRenderState(CowCorpse entity, CowRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.variant = entity.getVariant().value();
    }

    @Override
    protected void setupRotations(CowRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        poseStack.translate(0.0F, 0.45F, 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.translate(0.0F, -0.45F, 0.0F);
    }

    @Override
    public void submit(CowRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.variant != null) {
            this.model = this.models.get(state.variant.modelAndTexture().model()).getModel(state.isBaby);
            super.submit(state, poseStack, submitNodeCollector, camera);
        }
    }
}
