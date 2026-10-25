package com.canadiendragon.primitiveisolation.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;

import net.minecraft.network.chat.Component;

import com.canadiendragon.primitiveisolation.primitiveisolation;

public final class HideScrapingRecipeCategory extends AbstractRecipeCategory<HideScrapingJeiRecipe> {
    public HideScrapingRecipeCategory(IGuiHelper guiHelper) {
        super(
                CharcoalPitJeiPlugin.HIDE_SCRAPING,
                Component.translatable("jei.primitiveisolation.hide_scraping"),
                guiHelper.createDrawableItemLike(primitiveisolation.FLINT_KNIFE.get()),
                148,
                66);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, HideScrapingJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(8, 18)
                .setStandardSlotBackground()
                .add(recipe.input());
        builder.addInputSlot(32, 18)
                .setStandardSlotBackground()
                .add(recipe.tool());
        builder.addOutputSlot(108, 18)
                .setOutputSlotBackground()
                .add(recipe.output());
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder,
            HideScrapingJeiRecipe recipe,
            IFocusGroup focuses) {
        builder.addText(
                        Component.translatable("jei.primitiveisolation.hide_scraping.details"),
                        140,
                        22)
                .setPosition(4, 46);
    }
}
