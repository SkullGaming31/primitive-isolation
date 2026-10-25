package com.canadiendragon.primitiveisolation;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ClimbableRopeBlock extends Block {
    private static final MapCodec<ClimbableRopeBlock> CODEC = simpleCodec(ClimbableRopeBlock::new);
    private static final VoxelShape SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);

    public ClimbableRopeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction directionToNeighbour,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random) {
        return !this.canSurvive(state, level, pos)
                ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
                : super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos supportPos = pos.relative(direction);
            BlockState support = level.getBlockState(supportPos);
            if (support.is(primitiveisolation.ROPE.get())
                    || support.isFaceSturdy(level, supportPos, direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }
}
