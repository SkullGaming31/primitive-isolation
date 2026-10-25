package com.canadiendragon.primitiveisolation;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

import org.jspecify.annotations.Nullable;

public class DryingRackBlock extends BaseEntityBlock {
    private static final MapCodec<DryingRackBlock> CODEC = simpleCodec(DryingRackBlock::new);
    public static final EnumProperty<DryingStage> STAGE = EnumProperty.create("stage", DryingStage.class);

    public DryingRackBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, DryingStage.IDLE));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DryingRackBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide()
                ? null
                : createTickerHelper(
                        type,
                        primitiveisolation.DRYING_RACK_BLOCK_ENTITY.get(),
                        DryingRackBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult) {
        return this.openMenu(level, pos, player);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack itemStack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        return this.openMenu(level, pos, player);
    }

    private InteractionResult openMenu(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(level.getBlockEntity(pos) instanceof DryingRackBlockEntity rack)) {
            return InteractionResult.FAIL;
        }
        serverPlayer.openMenu(rack, buffer -> buffer.writeBlockPos(pos));
        return InteractionResult.SUCCESS;
    }

    public enum DryingStage implements StringRepresentable {
        IDLE("idle"),
        WET("wet"),
        PARTIALLY_DRY("partially_dry"),
        NEARLY_DRY("nearly_dry"),
        READY("ready");

        private final String name;

        DryingStage(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
