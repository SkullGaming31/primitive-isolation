package com.canadiendragon.primitiveisolation;

import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;

public class PerishableFoodItem extends Item {
    private static final String SPOILS_AT_KEY = "PrimitiveIsolationSpoilsAt";
    private final long spoilDurationTicks;

    public PerishableFoodItem(Properties properties, long spoilDurationTicks) {
        super(properties);
        this.spoilDurationTicks = spoilDurationTicks;
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, EquipmentSlot slot) {
        updateSpoilage(itemStack, level, owner);
    }

    public void tickItemEntity(ItemEntity entity, ServerLevel level) {
        updateSpoilage(entity.getItem(), level, entity);
    }

    public boolean hasSpoiled(ItemStack itemStack, Level level) {
        return hasSpoiled(itemStack, level, this.spoilDurationTicks);
    }

    public long getSpoilDurationTicks() {
        return this.spoilDurationTicks;
    }

    public static boolean hasSpoiled(ItemStack itemStack, Level level, long spoilDurationTicks) {
        Optional<Long> spoilsAt = getSpoilsAt(itemStack);
        if (spoilsAt.isEmpty()) {
            CustomData.update(
                    DataComponents.CUSTOM_DATA,
                    itemStack,
                    data -> data.putLong(SPOILS_AT_KEY, level.getGameTime() + spoilDurationTicks));
            return false;
        }

        return level.getGameTime() >= spoilsAt.get();
    }

    public static void pauseSpoilage(ItemStack itemStack, Level level, long spoilDurationTicks) {
        long spoilsAt = getSpoilsAt(itemStack).orElse(level.getGameTime() + spoilDurationTicks);
        CustomData.update(
                DataComponents.CUSTOM_DATA,
                itemStack,
                data -> data.putLong(SPOILS_AT_KEY, spoilsAt + 1));
    }

    public static ItemStack getSpoiledStack(ItemStack itemStack) {
        return new ItemStack(Items.ROTTEN_FLESH, itemStack.getCount());
    }

    @Override
    public void appendHoverText(
            ItemStack itemStack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> builder,
            TooltipFlag tooltipFlag) {
        Optional<Long> spoilsAt = getSpoilsAt(itemStack);
        Level level = context.level();

        if (spoilsAt.isPresent() && level == null) {
            builder.accept(Component.translatable("item.primitiveisolation.spoilage.unknown")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        long remainingTicks = spoilsAt
                .map(deadline -> deadline - level.getGameTime())
                .orElse(this.spoilDurationTicks);
        long remainingSeconds = (long) Math.ceil(remainingTicks / (double) context.tickRate());
        builder.accept(Component.translatable(
                        "item.primitiveisolation.spoilage.remaining",
                        formatDuration(Math.max(0L, remainingSeconds)))
                .withStyle(ChatFormatting.GRAY));
    }

    private void updateSpoilage(ItemStack itemStack, Level level, Entity owner) {
        if (!hasSpoiled(itemStack, level)) {
            return;
        }

        ItemStack spoiled = getSpoiledStack(itemStack);
        if (owner instanceof ItemEntity itemEntity) {
            itemEntity.setItem(spoiled);
        } else if (owner instanceof Player player) {
            Inventory inventory = player.getInventory();
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                if (inventory.getItem(slot) == itemStack) {
                    inventory.setItem(slot, spoiled);
                    return;
                }
            }
        }
    }

    private static Optional<Long> getSpoilsAt(ItemStack itemStack) {
        return itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getLong(SPOILS_AT_KEY);
    }

    private static String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }

        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return minutes + ":" + (remainingSeconds < 10 ? "0" : "") + remainingSeconds;
    }
}
