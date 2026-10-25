package com.canadiendragon.primitiveisolation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class HandDrillItem extends Item {
    private static final int LIGHTING_TIME_TICKS = 100;
    private static final double USE_RANGE_SQUARED = 25.0;

    private final Map<UUID, DrillingSession> activeSessions = new HashMap<>();

    public HandDrillItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack itemStack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> builder,
            TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("item.primitiveisolation.hand_drill.tooltip"));
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.BRUSH;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity user) {
        return LIGHTING_TIME_TICKS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (player == null || !canLightTarget(level, pos)) {
            return InteractionResult.PASS;
        }

        InteractionHand hand = context.getHand();
        this.activeSessions.put(player.getUUID(), new DrillingSession(level, pos.immutable(), hand));
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack itemStack, int ticksRemaining) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }

        DrillingSession session = this.activeSessions.get(player.getUUID());
        if (session == null
                || !player.isAlive()
                || !player.isUsingItem()
                || player.getUsedItemHand() != session.hand()
                || player.getItemInHand(session.hand()).getItem() != this
                || player.level() != session.level()
                || player.distanceToSqr(session.pos().getCenter()) > USE_RANGE_SQUARED
                || !isLookingAt(player, session.pos())
                || !canLightTarget(level, session.pos())) {
            this.cancelAndStop(player);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return itemStack;
        }

        DrillingSession session = this.activeSessions.remove(player.getUUID());
        if (session == null
                || level.isClientSide()
                || !(level instanceof ServerLevel serverLevel)
                || session.level() != level
                || !player.isAlive()
                || player.getUsedItemHand() != session.hand()
                || player.distanceToSqr(session.pos().getCenter()) > USE_RANGE_SQUARED
                || !isLookingAt(player, session.pos())
                || !canLightTarget(level, session.pos())) {
            return itemStack;
        }

        BlockState state = level.getBlockState(session.pos());
        if (CampfireBlock.canLight(state)) {
            serverLevel.setBlock(session.pos(), state.setValue(CampfireBlock.LIT, true), 11);
            this.playLightingEffects(serverLevel, player, session.pos());
            this.damageDrill(itemStack, player, session.hand());
        } else if (state.is(primitiveisolation.CHARCOAL_PIT.get())
                && serverLevel.getBlockEntity(session.pos()) instanceof CharcoalPitBlockEntity pit) {
            if (pit.isActive()) {
                player.sendSystemMessage(Component.translatable("message.primitiveisolation.charcoal_pit.active"));
                return itemStack;
            }

            int remainingTicks = pit.startPit();
            if (remainingTicks == 0) {
                player.sendSystemMessage(pit.getInvalidReason());
                return itemStack;
            }

            player.sendSystemMessage(Component.translatable(
                    "message.primitiveisolation.charcoal_pit.started",
                    pit.getLogCount(),
                    (remainingTicks + 19) / 20));
            this.playLightingEffects(serverLevel, player, session.pos());
            this.damageDrill(itemStack, player, session.hand());
        }

        return itemStack;
    }

    @Override
    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity entity, int remainingUseDuration) {
        if (entity instanceof Player player) {
            this.activeSessions.remove(player.getUUID());
        }
        return false;
    }

    private static boolean canLightTarget(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (CampfireBlock.canLight(state)) {
            return !level.isRainingAt(pos.above());
        }
        return state.is(primitiveisolation.CHARCOAL_PIT.get())
                && !state.getValue(CharcoalPitBlock.ACTIVE);
    }

    private static boolean isLookingAt(Player player, BlockPos pos) {
        HitResult hit = player.pick(5.0, 0.0F, false);
        return hit instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(pos);
    }

    private void damageDrill(ItemStack itemStack, Player player, InteractionHand hand) {
        if (!player.hasInfiniteMaterials()) {
            itemStack.hurtAndBreak(1, player, hand);
        }
    }

    private void playLightingEffects(ServerLevel level, Player player, BlockPos pos) {
        level.playSound(
                null,
                pos,
                SoundEvents.FLINTANDSTEEL_USE,
                SoundSource.BLOCKS,
                1.0F,
                level.getRandom().nextFloat() * 0.4F + 0.8F);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
    }

    private void cancelAndStop(Player player) {
        this.activeSessions.remove(player.getUUID());
        player.stopUsingItem();
    }

    private record DrillingSession(Level level, BlockPos pos, InteractionHand hand) {
    }
}
