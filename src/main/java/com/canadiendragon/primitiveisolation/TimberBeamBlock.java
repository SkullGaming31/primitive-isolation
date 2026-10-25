package com.canadiendragon.primitiveisolation;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TimberBeamBlock extends RotatedPillarBlock {
    private static final MapCodec<TimberBeamBlock> CODEC = simpleCodec(TimberBeamBlock::new);
    private static final VoxelShape BEAM_SHAPE_X = Block.box(0, 4, 4, 16, 12, 12);
    private static final VoxelShape BEAM_SHAPE_Y = Block.box(4, 0, 4, 12, 16, 12);
    private static final VoxelShape BEAM_SHAPE_Z = Block.box(4, 4, 0, 12, 12, 16);

    public TimberBeamBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends RotatedPillarBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context) {
        return switch (state.getValue(AXIS)) {
            case X -> BEAM_SHAPE_X;
            case Y -> BEAM_SHAPE_Y;
            case Z -> BEAM_SHAPE_Z;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context) {
        return this.getShape(state, level, pos, context);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return Shapes.empty();
    }
}
