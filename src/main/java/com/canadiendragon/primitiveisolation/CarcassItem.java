package com.canadiendragon.primitiveisolation;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class CarcassItem extends Item {
    private final Supplier<List<ItemStack>> butcheredDrops;

    public CarcassItem(Properties properties, Supplier<List<ItemStack>> butcheredDrops) {
        super(properties);
        this.butcheredDrops = butcheredDrops;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("item.primitiveisolation.carcass.tooltip"));
    }

    public List<ItemStack> getButcheredDrops() {
        return this.butcheredDrops.get();
    }

    public static boolean hasFlintKnife(Player player, InteractionHand hand) {
        return player.getItemInHand(hand).is(primitiveisolation.FLINT_KNIFE.get());
    }

    public static void process(Player player, InteractionHand knifeHand, List<ItemStack> butcheredDrops) {
        if (!player.hasInfiniteMaterials()) {
            player.getItemInHand(knifeHand).hurtAndBreak(1, player, knifeHand);
        }

        for (ItemStack drop : butcheredDrops) {
            player.getInventory().add(drop);
        }
    }
}
