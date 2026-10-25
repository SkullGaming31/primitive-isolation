package com.canadiendragon.primitiveisolation;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RawHideItem extends Item {
    public RawHideItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND
                || !CarcassItem.hasFlintKnife(player, InteractionHand.OFF_HAND)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            ItemStack rawHide = player.getItemInHand(hand);
            rawHide.shrink(1);
            ItemStack cleanedHide = new ItemStack(primitiveisolation.CLEANED_HIDE.get());
            if (!player.getInventory().add(cleanedHide)) {
                player.drop(cleanedHide, false);
            }
            if (!player.hasInfiniteMaterials()) {
                player.getOffhandItem().hurtAndBreak(1, player, InteractionHand.OFF_HAND);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
