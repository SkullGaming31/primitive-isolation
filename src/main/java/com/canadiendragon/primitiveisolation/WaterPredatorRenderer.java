package com.canadiendragon.primitiveisolation;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class WaterPredatorRenderer<T extends AquaticPredator>
        extends MobRenderer<T, LivingEntityRenderState, WaterPredatorModel> {
    private final Identifier texture;
    private final float renderScale;

    public WaterPredatorRenderer(
            EntityRendererProvider.Context context,
            ModelLayerLocation layer,
            Identifier texture,
            float shadowRadius,
            float renderScale) {
        super(context, new WaterPredatorModel(context.bakeLayer(layer)), shadowRadius);
        this.texture = texture;
        this.renderScale = renderScale;
    }

    @Override
    protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        poseStack.scale(this.renderScale, this.renderScale, this.renderScale);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return this.texture;
    }
}
