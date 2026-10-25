package com.canadiendragon.primitiveisolation.jei;

import java.util.List;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.canadiendragon.primitiveisolation.primitiveisolation;

public record SaltingJeiRecipe(List<ItemStack> rawInputs, ItemStack output) {
    public SaltingJeiRecipe {
        rawInputs = rawInputs.stream().map(ItemStack::copy).toList();
        output = output.copy();
    }

    public static List<SaltingJeiRecipe> all() {
        return List.of(
                recipe(Items.BEEF, primitiveisolation.RAW_BEEF.get(), primitiveisolation.SALTED_BEEF.get()),
                recipe(Items.PORKCHOP, primitiveisolation.RAW_PORK.get(), primitiveisolation.SALTED_PORK.get()),
                recipe(Items.CHICKEN, primitiveisolation.RAW_CHICKEN.get(), primitiveisolation.SALTED_CHICKEN.get()),
                recipe(Items.MUTTON, primitiveisolation.RAW_MUTTON.get(), primitiveisolation.SALTED_MUTTON.get()),
                recipe(Items.RABBIT, primitiveisolation.RAW_RABBIT.get(), primitiveisolation.SALTED_RABBIT.get()),
                recipe(Items.COD, primitiveisolation.RAW_COD.get(), primitiveisolation.SALTED_COD.get()),
                recipe(Items.SALMON, primitiveisolation.RAW_SALMON.get(), primitiveisolation.SALTED_SALMON.get()),
                recipe(
                        Items.TROPICAL_FISH,
                        primitiveisolation.RAW_TROPICAL_FISH.get(),
                        primitiveisolation.SALTED_TROPICAL_FISH.get()),
                recipe(
                        Items.PUFFERFISH,
                        primitiveisolation.RAW_PUFFERFISH.get(),
                        primitiveisolation.SALTED_PUFFERFISH.get()));
    }

    private static SaltingJeiRecipe recipe(Item vanillaRaw, Item modRaw, Item salted) {
        return new SaltingJeiRecipe(
                List.of(new ItemStack(vanillaRaw), new ItemStack(modRaw)),
                new ItemStack(salted));
    }
}
