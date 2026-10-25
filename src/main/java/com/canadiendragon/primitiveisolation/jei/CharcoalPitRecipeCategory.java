package com.canadiendragon.primitiveisolation.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.crafting.Ingredient;

import com.canadiendragon.primitiveisolation.primitiveisolation;

public final class CharcoalPitRecipeCategory extends AbstractRecipeCategory<CharcoalPitJeiRecipe> {
    public CharcoalPitRecipeCategory(IGuiHelper guiHelper) {
        super(
                CharcoalPitJeiPlugin.CHARCOAL_PIT,
                Component.translatable("jei.primitiveisolation.charcoal_pit"),
                guiHelper.createDrawableItemLike(primitiveisolation.CHARCOAL_PIT.get()),
                148,
                78);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CharcoalPitJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(10, 12)
                .setStandardSlotBackground()
                .add(Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ItemTags.LOGS)));

        var outputs = recipe.outputs();
        int firstOutputX = outputs.size() == 1 ? 104 : 91;
        for (int index = 0; index < outputs.size(); index++) {
            builder.addOutputSlot(firstOutputX + index * 22, 12)
                    .setOutputSlotBackground()
                    .add(outputs.get(index));
        }

        builder.addInvisibleIngredients(RecipeIngredientRole.CRAFTING_STATION)
                .add(primitiveisolation.CHARCOAL_PIT.get());
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder,
            CharcoalPitJeiRecipe recipe,
            IFocusGroup focuses) {
        builder.addAnimatedRecipeArrowWidget(recipe.durationTicks()).setPosition(48, 12);
        builder.addText(
                        Component.translatable(
                                "jei.primitiveisolation.charcoal_pit.details",
                                recipe.pitSize(),
                                recipe.pitSize(),
                                recipe.logCount()),
                        138,
                        12)
                .setPosition(5, 42);
        builder.addText(
                        Component.translatable(
                                "jei.primitiveisolation.charcoal_pit.time",
                                recipe.durationSeconds() / 60,
                                recipe.durationSeconds() % 60),
                        138,
                        12)
                .setPosition(5, 54);
        builder.addText(
                        Component.translatable("jei.primitiveisolation.charcoal_pit.structure"),
                        138,
                        12)
                .setPosition(5, 66);
    }

}
