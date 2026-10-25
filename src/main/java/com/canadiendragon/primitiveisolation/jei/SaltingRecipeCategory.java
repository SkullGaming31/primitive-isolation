package com.canadiendragon.primitiveisolation.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.canadiendragon.primitiveisolation.primitiveisolation;

public final class SaltingRecipeCategory extends AbstractRecipeCategory<SaltingJeiRecipe> {
    public SaltingRecipeCategory(IGuiHelper guiHelper) {
        super(
                CharcoalPitJeiPlugin.SALTING,
                Component.translatable("jei.primitiveisolation.salting"),
                guiHelper.createDrawableItemLike(primitiveisolation.SALTING_RACK.get()),
                148,
                64);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SaltingJeiRecipe recipe, IFocusGroup focuses) {
        var rawSlot = builder.addInputSlot(8, 18).setStandardSlotBackground();
        for (ItemStack rawInput : recipe.rawInputs()) {
            rawSlot.add(rawInput);
        }
        builder.addInputSlot(32, 18)
                .setStandardSlotBackground()
                .add(new ItemStack(primitiveisolation.SALT.get(), 2));
        builder.addOutputSlot(108, 18)
                .setOutputSlotBackground()
                .add(recipe.output());
        builder.addInvisibleIngredients(RecipeIngredientRole.CRAFTING_STATION)
                .add(primitiveisolation.SALTING_RACK.get());
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder,
            SaltingJeiRecipe recipe,
            IFocusGroup focuses) {
        builder.addAnimatedRecipeArrowWidget(20 * 60).setPosition(58, 18);
        builder.addText(Component.translatable("jei.primitiveisolation.salting.details"), 140, 12)
                .setPosition(4, 46);
    }
}
