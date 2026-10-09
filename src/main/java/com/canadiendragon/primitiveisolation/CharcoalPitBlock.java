package com.canadiendragon.primitiveisolation;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class CharcoalPitBlock extends BaseEntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private static final MapCodec<CharcoalPitBlock> CODEC = simpleCodec(CharcoalPitBlock::new);

    public CharcoalPitBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CharcoalPitBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide()
                ? null
                : createTickerHelper(type, primitiveisolation.CHARCOAL_PIT_BLOCK_ENTITY.get(), CharcoalPitBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult) {
        return showRemainingTime(level, pos, player);
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
        if (!itemStack.is(Items.FLINT_AND_STEEL)) {
            return showRemainingTime(level, pos, player);
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos) instanceof CharcoalPitBlockEntity pit)) {
            return InteractionResult.FAIL;
        }
        if (pit.isActive()) {
            return showRemainingTime(level, pos, player);
        }

        int remainingTicks = pit.startPit();
        if (remainingTicks == 0) {
            player.sendSystemMessage(pit.getInvalidReason());
            return InteractionResult.FAIL;
        }

        if (!player.hasInfiniteMaterials()) {
            itemStack.hurtAndBreak(1, player, hand);
        }
        player.sendSystemMessage(Component.translatable(
                "message.primitiveisolation.charcoal_pit.started",
                pit.getLogCount(),
                (remainingTicks + 19) / 20));
        return InteractionResult.SUCCESS;
    }

    private InteractionResult showRemainingTime(Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof CharcoalPitBlockEntity pit) || !pit.isActive()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            int seconds = pit.getRemainingTimeSeconds();
            player.sendSystemMessage(Component.translatable(
                    "message.primitiveisolation.charcoal_pit.remaining",
                    seconds / 60,
                    seconds % 60));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(ACTIVE) && random.nextBoolean()) {
            for (int i = 0; i < 2; i++) {
                level.addParticle(
                        ParticleTypes.SMOKE,
                        pos.getX() + 0.35 + random.nextDouble() * 0.3,
                        pos.getY() + 1.0,
                        pos.getZ() + 0.35 + random.nextDouble() * 0.3,
                        0.0,
                        0.06,
                        0.0);
            }
        }
    }
}
