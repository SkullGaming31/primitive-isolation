package com.canadiendragon.primitiveisolation.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.recipe.types.IRecipeType;

import net.minecraft.resources.Identifier;

import com.canadiendragon.primitiveisolation.primitiveisolation;

@JeiPlugin
public final class CharcoalPitJeiPlugin implements IModPlugin {
    public static final IRecipeType<CharcoalPitJeiRecipe> CHARCOAL_PIT =
            IRecipeType.create(
                    primitiveisolation.MODID,
                    "charcoal_pit",
                    CharcoalPitJeiRecipe.class);
    public static final IRecipeType<KnappingJeiRecipe> FLINT_HEAD_KNAPPING =
            IRecipeType.create(
                    primitiveisolation.MODID,
                    "flint_head_knapping",
                    KnappingJeiRecipe.class);
    public static final IRecipeType<KnappingJeiRecipe> FLINT_TOOL_ASSEMBLY =
            IRecipeType.create(
                    primitiveisolation.MODID,
                    "flint_tool_assembly",
                    KnappingJeiRecipe.class);
    public static final IRecipeType<ButcheryJeiRecipe> BUTCHERY =
            IRecipeType.create(
                    primitiveisolation.MODID,
                    "butchery",
                    ButcheryJeiRecipe.class);
    public static final IRecipeType<SaltingJeiRecipe> SALTING =
            IRecipeType.create(
                    primitiveisolation.MODID,
                    "salting",
                    SaltingJeiRecipe.class);
    public static final IRecipeType<FieldButcheryJeiRecipe> FIELD_BUTCHERY =
            IRecipeType.create(
                    primitiveisolation.MODID,
                    "field_butchery",
                    FieldButcheryJeiRecipe.class);
    public static final IRecipeType<DryingRackJeiRecipe> DRYING_RACK =
            IRecipeType.create(
                    primitiveisolation.MODID,
                    "drying_rack",
                    DryingRackJeiRecipe.class);
    public static final IRecipeType<HideScrapingJeiRecipe> HIDE_SCRAPING =
            IRecipeType.create(
                    primitiveisolation.MODID,
                    "hide_scraping",
                    HideScrapingJeiRecipe.class);

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(primitiveisolation.MODID, "charcoal_pit");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new CharcoalPitRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new KnappingRecipeCategory(
                        registration.getJeiHelpers().getGuiHelper(),
                        FLINT_HEAD_KNAPPING,
                        "jei.primitiveisolation.flint_head_knapping"),
                new KnappingRecipeCategory(
                        registration.getJeiHelpers().getGuiHelper(),
                        FLINT_TOOL_ASSEMBLY,
                        "jei.primitiveisolation.flint_tool_assembly"),
                new ButcheryRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new SaltingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new FieldButcheryRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new DryingRackRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new HideScrapingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(CHARCOAL_PIT, CharcoalPitJeiRecipe.allPitSizes());
        registration.addRecipes(FLINT_HEAD_KNAPPING, KnappingJeiRecipe.headKnappingRecipes());
        registration.addRecipes(FLINT_TOOL_ASSEMBLY, KnappingJeiRecipe.toolAssemblyRecipes());
        registration.addRecipes(BUTCHERY, ButcheryJeiRecipe.all());
        registration.addRecipes(SALTING, SaltingJeiRecipe.all());
        registration.addRecipes(FIELD_BUTCHERY, FieldButcheryJeiRecipe.all());
        registration.addRecipes(DRYING_RACK, java.util.List.of(DryingRackJeiRecipe.hide()));
        registration.addRecipes(HIDE_SCRAPING, java.util.List.of(HideScrapingJeiRecipe.rawHide()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(CHARCOAL_PIT, primitiveisolation.CHARCOAL_PIT.get());
        registration.addCraftingStation(FLINT_HEAD_KNAPPING, primitiveisolation.KNAPPING_STATION.get());
        registration.addCraftingStation(FLINT_TOOL_ASSEMBLY, primitiveisolation.KNAPPING_STATION.get());
        registration.addCraftingStation(BUTCHERY, primitiveisolation.BUTCHERY_TABLE.get());
        registration.addCraftingStation(SALTING, primitiveisolation.SALTING_RACK.get());
        registration.addCraftingStation(FIELD_BUTCHERY, primitiveisolation.FLINT_KNIFE.get());
        registration.addCraftingStation(DRYING_RACK, primitiveisolation.DRYING_RACK.get());
        registration.addCraftingStation(HIDE_SCRAPING, primitiveisolation.FLINT_KNIFE.get());
    }
}
