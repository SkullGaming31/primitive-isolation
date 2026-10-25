package com.canadiendragon.primitiveisolation.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;

import net.minecraft.network.chat.Component;

import com.canadiendragon.primitiveisolation.primitiveisolation;

public final class DryingRackRecipeCategory extends AbstractRecipeCategory<DryingRackJeiRecipe> {
    public DryingRackRecipeCategory(IGuiHelper guiHelper) {
        super(
                CharcoalPitJeiPlugin.DRYING_RACK,
                Component.translatable("jei.primitiveisolation.drying_rack"),
                guiHelper.createDrawableItemLike(primitiveisolation.DRYING_RACK.get()),
                148,
                66);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DryingRackJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(10, 18)
                .setStandardSlotBackground()
                .add(recipe.input());
        builder.addOutputSlot(108, 18)
                .setOutputSlotBackground()
                .add(recipe.output());
        builder.addInvisibleIngredients(RecipeIngredientRole.CRAFTING_STATION)
                .add(primitiveisolation.DRYING_RACK.get());
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder,
            DryingRackJeiRecipe recipe,
            IFocusGroup focuses) {
        builder.addAnimatedRecipeArrowWidget(recipe.durationTicks()).setPosition(58, 18);
        builder.addText(
                        Component.translatable("jei.primitiveisolation.drying_rack.details"),
                        140,
                        22)
                .setPosition(4, 46);
    }
}
