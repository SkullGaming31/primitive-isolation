package com.canadiendragon.primitiveisolation;

import com.mojang.math.Axis;

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

public class WildlifeModel extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation DEER_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(primitiveisolation.MODID, "deer"), "main");
    public static final ModelLayerLocation MOOSE_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(primitiveisolation.MODID, "moose"), "main");

    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public WildlifeModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
    }

    public static LayerDefinition createDeerLayer() {
        return createLayer(false);
    }

    public static LayerDefinition createMooseLayer() {
        return createLayer(true);
    }

    private static LayerDefinition createLayer(boolean moose) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        if (moose) {
            root.addOrReplaceChild(
                    "body",
                    CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -8.0F, -10.0F, 12.0F, 14.0F, 20.0F),
                    PartPose.offset(0.0F, 13.0F, 1.0F));
            root.addOrReplaceChild(
                    "neck",
                    CubeListBuilder.create().texOffs(0, 36).addBox(-4.0F, -12.0F, -4.0F, 8.0F, 14.0F, 8.0F),
                    PartPose.offset(0.0F, 12.0F, -6.0F));
            PartDefinition head = root.addOrReplaceChild(
                    "head",
                    CubeListBuilder.create()
                            .texOffs(32, 32).addBox(-4.0F, -4.0F, -5.0F, 8.0F, 8.0F, 7.0F)
                            .texOffs(32, 48).addBox(-2.0F, 0.0F, -8.0F, 4.0F, 3.0F, 4.0F),
                    PartPose.offset(0.0F, 5.0F, -9.0F));
            head.addOrReplaceChild(
                    "left_antler",
                    CubeListBuilder.create()
                            .texOffs(48, 0).addBox(-12.0F, -8.0F, -1.0F, 8.0F, 2.0F, 3.0F)
                            .texOffs(48, 8).addBox(-12.0F, -13.0F, 0.0F, 2.0F, 6.0F, 2.0F)
                            .texOffs(40, 8).addBox(-9.0F, -12.0F, 0.0F, 2.0F, 5.0F, 2.0F),
                    PartPose.ZERO);
            head.addOrReplaceChild(
                    "right_antler",
                    CubeListBuilder.create()
                            .texOffs(48, 0).addBox(4.0F, -8.0F, -1.0F, 8.0F, 2.0F, 3.0F)
                            .texOffs(48, 8).addBox(10.0F, -13.0F, 0.0F, 2.0F, 6.0F, 2.0F)
                            .texOffs(40, 8).addBox(7.0F, -12.0F, 0.0F, 2.0F, 5.0F, 2.0F),
                    PartPose.ZERO);
            addLegs(root, 10, 4.0F);
        } else {
            root.addOrReplaceChild(
                    "body",
                    CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -6.0F, -8.0F, 10.0F, 12.0F, 16.0F),
                    PartPose.offset(0.0F, 14.0F, 1.0F));
            root.addOrReplaceChild(
                    "neck",
                    CubeListBuilder.create().texOffs(0, 32).addBox(-2.5F, -9.0F, -3.0F, 5.0F, 11.0F, 6.0F),
                    PartPose.offset(0.0F, 12.0F, -6.0F));
            PartDefinition head = root.addOrReplaceChild(
                    "head",
                    CubeListBuilder.create()
                            .texOffs(24, 32).addBox(-3.0F, -3.0F, -4.0F, 6.0F, 6.0F, 6.0F)
                            .texOffs(24, 48).addBox(-2.0F, 0.0F, -7.0F, 4.0F, 3.0F, 3.0F),
                    PartPose.offset(0.0F, 6.0F, -8.0F));
            head.addOrReplaceChild(
                    "left_antler",
                    CubeListBuilder.create()
                            .texOffs(40, 32).addBox(-4.0F, -7.0F, 0.0F, 2.0F, 6.0F, 2.0F)
                            .texOffs(48, 32).addBox(-7.0F, -8.0F, 0.0F, 4.0F, 2.0F, 2.0F)
                            .texOffs(48, 40).addBox(-7.0F, -11.0F, 0.0F, 2.0F, 4.0F, 2.0F)
                            .texOffs(40, 40).addBox(-2.0F, -9.0F, 0.0F, 2.0F, 4.0F, 2.0F),
                    PartPose.ZERO);
            head.addOrReplaceChild(
                    "right_antler",
                    CubeListBuilder.create()
                            .texOffs(40, 32).addBox(2.0F, -7.0F, 0.0F, 2.0F, 6.0F, 2.0F)
                            .texOffs(48, 32).addBox(3.0F, -8.0F, 0.0F, 4.0F, 2.0F, 2.0F)
                            .texOffs(48, 40).addBox(5.0F, -11.0F, 0.0F, 2.0F, 4.0F, 2.0F)
                            .texOffs(40, 40).addBox(0.0F, -9.0F, 0.0F, 2.0F, 4.0F, 2.0F),
                    PartPose.ZERO);
            addLegs(root, 8, 3.0F);
        }

        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addLegs(PartDefinition root, int length, float legOffset) {
        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 52).addBox(-1.5F, 0.0F, -1.5F, 3.0F, length, 3.0F);
        root.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(-legOffset, 14.0F, 6.0F));
        root.addOrReplaceChild("left_hind_leg", leg, PartPose.offset(legOffset, 14.0F, 6.0F));
        root.addOrReplaceChild("right_front_leg", leg, PartPose.offset(-legOffset, 14.0F, -5.0F));
        root.addOrReplaceChild("left_front_leg", leg, PartPose.offset(legOffset, 14.0F, -5.0F));
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        this.head.xRot = state.xRot * (float) (Math.PI / 180.0);
        this.head.yRot = state.yRot * (float) (Math.PI / 180.0);
        float swing = (float) Math.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;
        this.rightHindLeg.xRot = swing;
        this.leftFrontLeg.xRot = swing;
        this.leftHindLeg.xRot = -swing;
        this.rightFrontLeg.xRot = -swing;
    }
}
