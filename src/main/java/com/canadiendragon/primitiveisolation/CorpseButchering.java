package com.canadiendragon.primitiveisolation;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public final class CorpseButchering {
    private static final int BUTCHERING_TIME_TICKS = 600;
    private static final double BUTCHERING_RANGE_SQUARED = 25.0;
    private static final long DECOMPOSITION_TIME_TICKS = 6000L;

    private final Animal corpse;
    private final Supplier<ItemStack> carcassItem;
    private final Supplier<List<ItemStack>> butcheredDrops;
    private UUID butcherer;
    private InteractionHand butcherHand;
    private int butcheringTicks;
    private long decomposesAt;

    public CorpseButchering(Animal corpse, Supplier<ItemStack> carcassItem, Supplier<List<ItemStack>> butcheredDrops) {
        this.corpse = corpse;
        this.carcassItem = carcassItem;
        this.butcheredDrops = butcheredDrops;
        this.decomposesAt = corpse.level().getGameTime() + DECOMPOSITION_TIME_TICKS;
        corpse.setNoAi(true);
        corpse.setInvulnerable(true);
        corpse.setSilent(true);
        corpse.setPersistenceRequired();
    }

    public void tick() {
        this.corpse.setNoAi(true);
        this.corpse.setDeltaMovement(Vec3.ZERO);
        if (!(this.corpse.level() instanceof ServerLevel level)) {
            return;
        }

        if (level.getGameTime() >= this.decomposesAt) {
            this.corpse.discard();
            return;
        }

        if (this.butcherer != null) {
            this.tickButchering(level);
        }
    }

    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionHand knifeHand = getKnifeHand(player);
        if (knifeHand == null) {
            if (player.getItemInHand(hand).isEmpty()) {
                if (!this.corpse.level().isClientSide()) {
                    ItemStack carcass = this.carcassItem.get();
                    if (!player.getInventory().add(carcass)) {
                        player.drop(carcass, false);
                    }
                    this.corpse.discard();
                }
                return InteractionResult.SUCCESS;
            }

            if (!this.corpse.level().isClientSide()) {
                player.sendSystemMessage(Component.translatable("message.primitiveisolation.carcass.pickup_or_knife"));
            }
            return InteractionResult.FAIL;
        }

        if (this.butcherer != null && !this.butcherer.equals(player.getUUID())) {
            return InteractionResult.FAIL;
        }

        ButcheryTableBlock.cancelButchering(player);
        if (!this.corpse.level().isClientSide() && this.butcherer == null) {
            this.butcherer = player.getUUID();
            this.butcherHand = knifeHand;
            this.butcheringTicks = 0;
        }

        player.startUsingItem(knifeHand);
        return InteractionResult.CONSUME;
    }

    public void addAdditionalSaveData(ValueOutput output) {
        output.putLong("DecomposesAt", this.decomposesAt);
    }

    public void readAdditionalSaveData(ValueInput input) {
        this.decomposesAt = input.getLongOr(
                "DecomposesAt",
                this.corpse.level().getGameTime() + DECOMPOSITION_TIME_TICKS);
    }

    private static InteractionHand getKnifeHand(Player player) {
        if (CarcassItem.hasFlintKnife(player, InteractionHand.MAIN_HAND)) {
            return InteractionHand.MAIN_HAND;
        }
        if (CarcassItem.hasFlintKnife(player, InteractionHand.OFF_HAND)) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    private void tickButchering(ServerLevel level) {
        Player player = level.getPlayerByUUID(this.butcherer);
        if (player == null
                || !player.isAlive()
                || !player.isUsingItem()
                || player.getUsedItemHand() != this.butcherHand
                || !CarcassItem.hasFlintKnife(player, this.butcherHand)
                || player.distanceToSqr(this.corpse) > BUTCHERING_RANGE_SQUARED) {
            this.butcherer = null;
            this.butcherHand = null;
            this.butcheringTicks = 0;
            return;
        }

        if (++this.butcheringTicks >= BUTCHERING_TIME_TICKS) {
            CarcassItem.process(player, this.butcherHand, this.butcheredDrops.get());
            player.stopUsingItem();
            this.corpse.discard();
        }
    }
}
