package com.canadiendragon.primitiveisolation.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;

import net.minecraft.network.chat.Component;

import com.canadiendragon.primitiveisolation.primitiveisolation;

public final class ButcheryRecipeCategory extends AbstractRecipeCategory<ButcheryJeiRecipe> {
    public ButcheryRecipeCategory(IGuiHelper guiHelper) {
        super(
                CharcoalPitJeiPlugin.BUTCHERY,
                Component.translatable("jei.primitiveisolation.butchery"),
                guiHelper.createDrawableItemLike(primitiveisolation.BUTCHERY_TABLE.get()),
                160,
                70);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ButcheryJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(5, 16)
                .setStandardSlotBackground()
                .add(recipe.carcass());
        builder.addInputSlot(27, 16)
                .setStandardSlotBackground()
                .add(primitiveisolation.FLINT_KNIFE.get());
        builder.addInvisibleIngredients(RecipeIngredientRole.CRAFTING_STATION)
                .add(primitiveisolation.BUTCHERY_TABLE.get());

        for (int index = 0; index < recipe.outputs().size(); index++) {
            builder.addOutputSlot(90 + index * 22, 16)
                    .setOutputSlotBackground()
                    .add(recipe.outputs().get(index));
        }
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder,
            ButcheryJeiRecipe recipe,
            IFocusGroup focuses) {
        builder.addAnimatedRecipeArrowWidget(200).setPosition(54, 16);
        builder.addText(Component.translatable(recipe.detailsKey()), 152, 12)
                .setPosition(4, 49);
    }
}
