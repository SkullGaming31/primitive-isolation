package com.canadiendragon.primitiveisolation.jei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.canadiendragon.primitiveisolation.CharcoalPitBlockEntity;

public record CharcoalPitJeiRecipe(int pitSize, int logCount, int durationTicks) {
    public static List<CharcoalPitJeiRecipe> allPitSizes() {
        List<CharcoalPitJeiRecipe> recipes = new ArrayList<>();
        for (int pitSize = 3; pitSize <= 9; pitSize += 2) {
            int logCount = pitSize * pitSize - 1;
            recipes.add(new CharcoalPitJeiRecipe(
                    pitSize,
                    logCount,
                    logCount * CharcoalPitBlockEntity.TICKS_PER_LOG));
        }
        return List.copyOf(recipes);
    }

    public List<ItemStack> outputs() {
        List<ItemStack> outputs = new ArrayList<>();
        int remaining = this.logCount;
        while (remaining > 0) {
            int count = Math.min(remaining, Items.CHARCOAL.getDefaultMaxStackSize());
            outputs.add(new ItemStack(Items.CHARCOAL, count));
            remaining -= count;
        }
        return List.copyOf(outputs);
    }

    public int durationSeconds() {
        return this.durationTicks / 20;
    }
}
