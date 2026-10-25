package com.canadiendragon.primitiveisolation;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class WaterPredatorModel extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation PIRANHA_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(primitiveisolation.MODID, "piranha"), "main");
    public static final ModelLayerLocation SHARK_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(primitiveisolation.MODID, "shark"), "main");

    private final ModelPart body;
    private final ModelPart tail;

    public WaterPredatorModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.tail = this.body.getChild("tail");
    }

    public static LayerDefinition createPiranhaLayer() {
        return createLayer(false);
    }

    public static LayerDefinition createSharkLayer() {
        return createLayer(true);
    }

    private static LayerDefinition createLayer(boolean shark) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body;
        if (shark) {
            body = root.addOrReplaceChild(
                    "body",
                    CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -4.0F, -9.0F, 10.0F, 8.0F, 18.0F),
                    PartPose.offset(0.0F, 14.0F, 0.0F));
            body.addOrReplaceChild(
                    "head",
                    CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -3.0F, -14.0F, 8.0F, 6.0F, 6.0F),
                    PartPose.ZERO);
            body.addOrReplaceChild(
                    "tail",
                    CubeListBuilder.create().texOffs(40, 0).addBox(-1.0F, -6.0F, 0.0F, 2.0F, 12.0F, 5.0F),
                    PartPose.offset(0.0F, 0.0F, 9.0F));
            body.addOrReplaceChild(
                    "dorsal_fin",
                    CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, -5.0F, -2.0F, 2.0F, 5.0F, 4.0F),
                    PartPose.offset(0.0F, -2.0F, 0.0F));
            addEyes(body, 3.8F, -1.0F, -11.5F);
            addSideFins(body, 4.4F, 1.0F, 1.0F);
        } else {
            body = root.addOrReplaceChild(
                    "body",
                    CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -5.0F, 6.0F, 6.0F, 10.0F),
                    PartPose.offset(0.0F, 15.0F, 0.0F));
            body.addOrReplaceChild(
                    "head",
                    CubeListBuilder.create().texOffs(24, 0).addBox(-2.5F, -2.0F, -8.0F, 5.0F, 4.0F, 4.0F),
                    PartPose.ZERO);
            body.addOrReplaceChild(
                    "tail",
                    CubeListBuilder.create().texOffs(40, 0).addBox(-0.5F, -3.0F, 0.0F, 1.0F, 6.0F, 3.0F),
                    PartPose.offset(0.0F, 0.0F, 5.0F));
            body.addOrReplaceChild(
                    "dorsal_fin",
                    CubeListBuilder.create().texOffs(48, 0).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 3.0F, 2.0F),
                    PartPose.offset(0.0F, -2.0F, 0.0F));
            addEyes(body, 2.3F, -1.0F, -6.5F);
            addSideFins(body, 2.7F, 0.5F, 1.0F);
        }

        return LayerDefinition.create(mesh, 64, 32);
    }

    private static void addEyes(PartDefinition body, float side, float y, float z) {
        CubeListBuilder eye = CubeListBuilder.create().texOffs(56, 24).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F);
        body.addOrReplaceChild("left_eye", eye, PartPose.offset(-side, y, z));
        body.addOrReplaceChild("right_eye", eye, PartPose.offset(side, y, z));
    }

    private static void addSideFins(PartDefinition body, float side, float y, float z) {
        CubeListBuilder fin = CubeListBuilder.create().texOffs(48, 8).addBox(-3.0F, -0.5F, -2.0F, 3.0F, 1.0F, 4.0F);
        body.addOrReplaceChild("left_fin", fin, PartPose.offset(-side, y, z));
        body.addOrReplaceChild(
                "right_fin",
                CubeListBuilder.create().texOffs(48, 8).addBox(0.0F, -0.5F, -2.0F, 3.0F, 1.0F, 4.0F),
                PartPose.offset(side, y, z));
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        this.body.xRot = state.xRot * (float) (Math.PI / 180.0);
        this.tail.yRot = (float) Math.cos(state.walkAnimationPos * 0.8F) * 0.4F * state.walkAnimationSpeed;
    }
}
