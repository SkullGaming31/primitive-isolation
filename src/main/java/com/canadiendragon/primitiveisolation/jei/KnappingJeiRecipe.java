package com.canadiendragon.primitiveisolation.jei;

import java.util.List;

import net.minecraft.world.item.ItemStack;

import com.canadiendragon.primitiveisolation.KnappingRecipes;

public record KnappingJeiRecipe(ItemStack output, List<ItemStack> inputs) {
    public KnappingJeiRecipe {
        output = output.copy();
        inputs = inputs.stream().map(ItemStack::copy).toList();
    }

    public static List<KnappingJeiRecipe> headKnappingRecipes() {
        return from(KnappingRecipes.HEADS);
    }

    public static List<KnappingJeiRecipe> toolAssemblyRecipes() {
        return from(KnappingRecipes.ASSEMBLIES);
    }

    private static List<KnappingJeiRecipe> from(List<KnappingRecipes.PatternRecipe> recipes) {
        return recipes.stream()
                .map(recipe -> {
                    List<ItemStack> inputs = recipe.pattern().stream()
                            .flatMap(row -> row.chars().mapToObj(column -> {
                                if (column == ' ') {
                                    return ItemStack.EMPTY;
                                }
                                return new ItemStack(recipe.key().get((char) column));
                            }))
                            .toList();
                    return new KnappingJeiRecipe(new ItemStack(recipe.output()), inputs);
                })
                .toList();
    }
}
