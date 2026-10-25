package com.canadiendragon.primitiveisolation.jei;

import net.minecraft.world.item.ItemStack;

import com.canadiendragon.primitiveisolation.primitiveisolation;

public record HideScrapingJeiRecipe(ItemStack input, ItemStack tool, ItemStack output) {
    public HideScrapingJeiRecipe {
        input = input.copy();
        tool = tool.copy();
        output = output.copy();
    }

    public static HideScrapingJeiRecipe rawHide() {
        return new HideScrapingJeiRecipe(
                new ItemStack(primitiveisolation.RAW_HIDE.get()),
                new ItemStack(primitiveisolation.FLINT_KNIFE.get()),
                new ItemStack(primitiveisolation.CLEANED_HIDE.get()));
    }
}
