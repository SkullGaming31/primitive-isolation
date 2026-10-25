package com.canadiendragon.primitiveisolation;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ButcheryTableRenderer implements BlockEntityRenderer<ButcheryTableBlockEntity, ButcheryTableRenderState> {
    private final EntityRenderDispatcher entityRenderDispatcher;

    public ButcheryTableRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
    }

    @Override
    public ButcheryTableRenderState createRenderState() {
        return new ButcheryTableRenderState();
    }

    @Override
    public void extractRenderState(
            ButcheryTableBlockEntity blockEntity,
            ButcheryTableRenderState renderState,
            float partialTick,
            Vec3 cameraPosition,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(
                blockEntity,
                renderState,
                partialTick,
                cameraPosition,
                crumblingOverlay);
        Entity carcass = blockEntity.getDisplayEntity();
        if (carcass == null) {
            renderState.carcass = null;
            return;
        }

        renderState.carcass = this.entityRenderDispatcher.extractEntity(carcass, partialTick);
        if (blockEntity.getLevel() != null) {
            renderState.carcass.lightCoords =
                    LevelRenderer.getLightCoords(blockEntity.getLevel(), blockEntity.getBlockPos().above());
        } else {
            renderState.carcass.lightCoords = renderState.lightCoords;
        }
    }

    @Override
    public void submit(
            ButcheryTableRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        if (renderState.carcass == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 1.0, 0.5);
        poseStack.scale(0.62F, 0.62F, 0.62F);
        this.entityRenderDispatcher.submit(
                renderState.carcass,
                camera,
                0.0,
                0.0,
                0.0,
                poseStack,
                submitNodeCollector);
        poseStack.popPose();
    }
}
