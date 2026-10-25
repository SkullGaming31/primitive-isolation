package com.canadiendragon.primitiveisolation.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;

import net.minecraft.network.chat.Component;

import com.canadiendragon.primitiveisolation.CorpseButchering;
import com.canadiendragon.primitiveisolation.primitiveisolation;

public final class FieldButcheryRecipeCategory extends AbstractRecipeCategory<FieldButcheryJeiRecipe> {
    public FieldButcheryRecipeCategory(IGuiHelper guiHelper) {
        super(
                CharcoalPitJeiPlugin.FIELD_BUTCHERY,
                Component.translatable("jei.primitiveisolation.field_butchery"),
                guiHelper.createDrawableItemLike(primitiveisolation.FLINT_KNIFE.get()),
                160,
                74);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FieldButcheryJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(5, 16)
                .setStandardSlotBackground()
                .add(recipe.carcass());
        builder.addInputSlot(27, 16)
                .setStandardSlotBackground()
                .add(primitiveisolation.FLINT_KNIFE.get());
        builder.addInvisibleIngredients(RecipeIngredientRole.CRAFTING_STATION)
                .add(primitiveisolation.FLINT_KNIFE.get());

        for (int index = 0; index < recipe.outputs().size(); index++) {
            builder.addOutputSlot(90 + index * 22, 16)
                    .setOutputSlotBackground()
                    .add(recipe.outputs().get(index));
        }
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder,
            FieldButcheryJeiRecipe recipe,
            IFocusGroup focuses) {
        builder.addAnimatedRecipeArrowWidget(CorpseButchering.BUTCHERING_TIME_TICKS)
                .setPosition(54, 16);
        builder.addText(Component.translatable(recipe.detailsKey()), 152, 22)
                .setPosition(4, 48);
    }
}
