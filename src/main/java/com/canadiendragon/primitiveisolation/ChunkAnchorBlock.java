package com.canadiendragon.primitiveisolation;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class ChunkAnchorBlock extends BaseEntityBlock {
    private static final MapCodec<ChunkAnchorBlock> CODEC = simpleCodec(ChunkAnchorBlock::new);

    public ChunkAnchorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChunkAnchorBlockEntity(pos, state);
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
                        primitiveisolation.CHUNK_ANCHOR_BLOCK_ENTITY.get(),
                        ChunkAnchorBlockEntity::serverTick);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(this) && level instanceof ServerLevel serverLevel
                && !Config.CHUNK_ANCHOR_REQUIRES_FUEL.getAsBoolean()) {
            refreshChunkTicket(serverLevel, pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        refreshChunkTicket(level, pos);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult) {
        return showStatus(level, pos, player);
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
        if (!itemStack.is(Items.ENDER_PEARL)) {
            return showStatus(level, pos, player);
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!Config.CHUNK_ANCHOR_REQUIRES_FUEL.getAsBoolean()) {
            player.sendSystemMessage(Component.translatable(
                    "message.primitiveisolation.chunk_anchor.fuel_disabled"));
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof ChunkAnchorBlockEntity anchor)) {
            return InteractionResult.FAIL;
        }

        anchor.addFuel((ServerLevel) level, ChunkAnchorBlockEntity.TICKS_PER_PEARL);
        if (!player.hasInfiniteMaterials()) {
            itemStack.shrink(1);
        }
        player.sendSystemMessage(anchor.getStatus());
        return InteractionResult.SUCCESS;
    }

    public static void refreshChunkTicket(ServerLevel level, BlockPos pos) {
        int startX = pos.getX() & ~15;
        int startZ = pos.getZ() & ~15;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean shouldForce = false;
        boolean requiresFuel = Config.CHUNK_ANCHOR_REQUIRES_FUEL.getAsBoolean();

        search:
        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                for (int y = level.getMinY(); y <= level.getMaxY(); y++) {
                    cursor.set(x, y, z);
                    if (!level.getBlockState(cursor).is(primitiveisolation.CHUNK_ANCHOR.get())) {
                        continue;
                    }
                    if (!requiresFuel
                            || level.getBlockEntity(cursor) instanceof ChunkAnchorBlockEntity anchor && anchor.hasFuel()) {
                        shouldForce = true;
                        break search;
                    }
                }
            }
        }

        level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, shouldForce);
    }

    private InteractionResult showStatus(Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof ChunkAnchorBlockEntity anchor)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            player.sendSystemMessage(anchor.getStatus());
        }
        return InteractionResult.SUCCESS;
    }
}
