package com.canadiendragon.primitiveisolation.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;

import net.minecraft.network.chat.Component;

import com.canadiendragon.primitiveisolation.primitiveisolation;

public final class KnappingRecipeCategory extends AbstractRecipeCategory<KnappingJeiRecipe> {
    public KnappingRecipeCategory(
            IGuiHelper guiHelper,
            IRecipeType<KnappingJeiRecipe> recipeType,
            String titleKey) {
        super(
                recipeType,
                Component.translatable(titleKey),
                guiHelper.createDrawableItemLike(primitiveisolation.KNAPPING_STATION.get()),
                148,
                66);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, KnappingJeiRecipe recipe, IFocusGroup focuses) {
        for (int index = 0; index < recipe.inputs().size(); index++) {
            int x = 8 + (index % 3) * 18;
            int y = 8 + index / 3 * 18;
            var slot = builder.addInputSlot(x, y).setStandardSlotBackground();
            if (!recipe.inputs().get(index).isEmpty()) {
                slot.add(recipe.inputs().get(index));
            }
        }

        builder.addOutputSlot(112, 26)
                .setOutputSlotBackground()
                .add(recipe.output());
        builder.addInvisibleIngredients(RecipeIngredientRole.CRAFTING_STATION)
                .add(primitiveisolation.KNAPPING_STATION.get());
    }
}
