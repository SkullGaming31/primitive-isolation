package com.canadiendragon.primitiveisolation.jei;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.canadiendragon.primitiveisolation.primitiveisolation;

public record ButcheryJeiRecipe(ItemStack carcass, List<ItemStack> outputs, String detailsKey) {
    public ButcheryJeiRecipe {
        carcass = carcass.copy();
        outputs = outputs.stream().map(ItemStack::copy).toList();
    }

    public static List<ButcheryJeiRecipe> all() {
        return List.of(
                recipe(
                        primitiveisolation.COW_CARCASS.get().getDefaultInstance(),
                        "jei.primitiveisolation.butchery.cow",
                        new ItemStack(primitiveisolation.RAW_BEEF.get(), 3),
                        new ItemStack(primitiveisolation.RAW_HIDE.get(), 2),
                        new ItemStack(Items.BONE)),
                recipe(
                        primitiveisolation.PIG_CARCASS.get().getDefaultInstance(),
                        new ItemStack(primitiveisolation.RAW_PORK.get(), 3)),
                recipe(
                        primitiveisolation.CHICKEN_CARCASS.get().getDefaultInstance(),
                        new ItemStack(primitiveisolation.RAW_CHICKEN.get(), 2),
                        new ItemStack(Items.FEATHER),
                        new ItemStack(Items.BONE)),
                variableRecipe(
                        primitiveisolation.MOOSHROOM_CARCASS.get().getDefaultInstance(),
                        "jei.primitiveisolation.butchery.mooshroom",
                        new ItemStack(primitiveisolation.RAW_BEEF.get(), 3),
                        new ItemStack(primitiveisolation.RAW_HIDE.get(), 2),
                        new ItemStack(Items.BONE)),
                variableRecipe(
                        primitiveisolation.SHEEP_CARCASS.get().getDefaultInstance(),
                        "jei.primitiveisolation.butchery.sheep",
                        new ItemStack(primitiveisolation.RAW_MUTTON.get(), 2)),
                variableRecipe(
                        primitiveisolation.RABBIT_CARCASS.get().getDefaultInstance(),
                        "jei.primitiveisolation.butchery.rabbit",
                        new ItemStack(primitiveisolation.RAW_RABBIT.get())),
                variableRecipe(
                        primitiveisolation.POLAR_BEAR_CARCASS.get().getDefaultInstance(),
                        "jei.primitiveisolation.butchery.polar_bear",
                        new ItemStack(primitiveisolation.RAW_COD.get()),
                        new ItemStack(primitiveisolation.RAW_SALMON.get())),
                variableRecipe(
                        primitiveisolation.HOGLIN_CARCASS.get().getDefaultInstance(),
                        "jei.primitiveisolation.butchery.hoglin",
                        new ItemStack(primitiveisolation.RAW_PORK.get(), 4)));
    }

    private static ButcheryJeiRecipe recipe(ItemStack carcass, ItemStack... outputs) {
        return new ButcheryJeiRecipe(
                carcass,
                List.of(outputs),
                "jei.primitiveisolation.butchery.fixed");
    }

    private static ButcheryJeiRecipe recipe(
            ItemStack carcass,
            String detailsKey,
            ItemStack... outputs) {
        return new ButcheryJeiRecipe(carcass, List.of(outputs), detailsKey);
    }

    private static ButcheryJeiRecipe variableRecipe(
            ItemStack carcass,
            String detailsKey,
            ItemStack... outputs) {
        return new ButcheryJeiRecipe(carcass, List.of(outputs), detailsKey);
    }
}
