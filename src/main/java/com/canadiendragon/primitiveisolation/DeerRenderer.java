package com.canadiendragon.primitiveisolation;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class DeerRenderer extends MobRenderer<Deer, LivingEntityRenderState, WildlifeModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            primitiveisolation.MODID, "textures/entity/deer.png");

    public DeerRenderer(EntityRendererProvider.Context context) {
        super(context, new WildlifeModel(context.bakeLayer(WildlifeModel.DEER_LAYER)), 0.45F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
