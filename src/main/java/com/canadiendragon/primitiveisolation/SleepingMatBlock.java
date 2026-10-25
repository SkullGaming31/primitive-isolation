package com.canadiendragon.primitiveisolation;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class SleepingMatBlock extends BedBlock {
    private static final MapCodec<BedBlock> CODEC = BlockBehaviour.simpleCodec(SleepingMatBlock::new);

    public SleepingMatBlock(BlockBehaviour.Properties properties) {
        super(DyeColor.BROWN, properties);
    }

    @Override
    public MapCodec<BedBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS_SERVER;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.CONSUME;
        }

        Direction facing = state.getValue(FACING);
        BlockPos headPos = state.getValue(PART) == BedPart.HEAD ? pos : pos.relative(facing);
        BlockState headState = level.getBlockState(headPos);
        if (!headState.is(this)) {
            return InteractionResult.CONSUME;
        }
        facing = headState.getValue(FACING);
        BlockPos footPos = headPos.relative(facing.getOpposite());

        if (player.isSleeping() || !player.isAlive()) {
            player.sendOverlayMessage(Component.translatable("sleep.not_possible"));
            return InteractionResult.SUCCESS_SERVER;
        }

        BedRule bedRule = level.environmentAttributes().getValue(EnvironmentAttributes.BED_RULE, headPos);
        boolean canSleep = bedRule.canSleep(level)
                || bedRule.canSleep() == BedRule.Rule.WHEN_DARK;
        if (!canSleep) {
            bedRule.errorMessage().ifPresent(player::sendOverlayMessage);
            return InteractionResult.SUCCESS_SERVER;
        }
        if (!isWithinBedReach(player, headPos) && !isWithinBedReach(player, footPos)) {
            player.sendOverlayMessage(Player.BedSleepingProblem.TOO_FAR_AWAY.message());
            return InteractionResult.SUCCESS_SERVER;
        }
        if (!level.getBlockState(headPos.above()).getCollisionShape(level, headPos.above()).isEmpty()
                || !level.getBlockState(footPos.above()).getCollisionShape(level, footPos.above()).isEmpty()) {
            player.sendOverlayMessage(Player.BedSleepingProblem.OBSTRUCTED.message());
            return InteractionResult.SUCCESS_SERVER;
        }

        if (!player.isCreative()) {
            AABB restArea = new AABB(
                    headPos.getX() - 8.0,
                    headPos.getY() - 5.0,
                    headPos.getZ() - 8.0,
                    headPos.getX() + 8.0,
                    headPos.getY() + 5.0,
                    headPos.getZ() + 8.0);
            if (!level.getEntitiesOfClass(
                    Monster.class,
                    restArea,
                    monster -> monster.isPreventingPlayerRest(serverLevel, player)).isEmpty()) {
                player.sendOverlayMessage(Player.BedSleepingProblem.NOT_SAFE.message());
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        player.startSleeping(headPos);
        serverLevel.updateSleepingPlayerList();
        if (!serverLevel.canSleepThroughNights()) {
            player.sendOverlayMessage(Component.translatable("sleep.not_possible"));
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    private static boolean isWithinBedReach(Player player, BlockPos pos) {
        Vec3 center = Vec3.atBottomCenterOf(pos);
        return Math.abs(player.getX() - center.x()) <= 3.0
                && Math.abs(player.getY() - center.y()) <= 2.0
                && Math.abs(player.getZ() - center.z()) <= 3.0;
    }
}
