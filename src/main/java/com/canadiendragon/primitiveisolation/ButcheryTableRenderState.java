package com.canadiendragon.primitiveisolation;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jspecify.annotations.Nullable;

public class ButcheryTableRenderState extends BlockEntityRenderState {
    @Nullable
    public EntityRenderState carcass;
}
