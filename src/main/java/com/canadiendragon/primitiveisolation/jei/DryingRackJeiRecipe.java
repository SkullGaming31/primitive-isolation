package com.canadiendragon.primitiveisolation.jei;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.canadiendragon.primitiveisolation.DryingRackBlockEntity;
import com.canadiendragon.primitiveisolation.primitiveisolation;

public record DryingRackJeiRecipe(ItemStack input, ItemStack output, int durationTicks) {
    public DryingRackJeiRecipe {
        input = input.copy();
        output = output.copy();
    }

    public static DryingRackJeiRecipe hide() {
        return new DryingRackJeiRecipe(
                new ItemStack(primitiveisolation.TANNED_HIDE.get()),
                new ItemStack(Items.LEATHER),
                DryingRackBlockEntity.DRYING_PROGRESS_REQUIRED / 2);
    }
}
