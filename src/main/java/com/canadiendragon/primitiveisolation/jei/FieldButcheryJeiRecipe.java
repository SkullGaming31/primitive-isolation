package com.canadiendragon.primitiveisolation.jei;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.canadiendragon.primitiveisolation.primitiveisolation;

public record FieldButcheryJeiRecipe(ItemStack carcass, List<ItemStack> outputs, String detailsKey) {
    public FieldButcheryJeiRecipe {
        carcass = carcass.copy();
        outputs = outputs.stream().map(ItemStack::copy).toList();
    }

    public static List<FieldButcheryJeiRecipe> all() {
        return List.of(
                recipe(
                        primitiveisolation.COW_CARCASS.get().getDefaultInstance(),
                        "jei.primitiveisolation.field_butchery.fixed",
                        new ItemStack(primitiveisolation.RAW_BEEF.get()),
                        new ItemStack(primitiveisolation.RAW_HIDE.get())),
                recipe(
                        primitiveisolation.PIG_CARCASS.get().getDefaultInstance(),
                        "jei.primitiveisolation.field_butchery.fixed",
                        new ItemStack(primitiveisolation.RAW_PORK.get())),
                recipe(
                        primitiveisolation.CHICKEN_CARCASS.get().getDefaultInstance(),
                        "jei.primitiveisolation.field_butchery.fixed",
                        new ItemStack(primitiveisolation.RAW_CHICKEN.get())),
                recipe(
                        primitiveisolation.MOOSHROOM_CARCASS.get().getDefaultInstance(),
                        "jei.primitiveisolation.field_butchery.mooshroom",
                        new ItemStack(primitiveisolation.RAW_BEEF.get(), 3),
                        new ItemStack(primitiveisolation.RAW_HIDE.get(), 2),
                        new ItemStack(Items.BONE)),
                recipe(
                        primitiveisolation.SHEEP_CARCASS.get().getDefaultInstance(),
                        "jei.primitiveisolation.field_butchery.sheep",
                        new ItemStack(primitiveisolation.RAW_MUTTON.get(), 2)),
                recipe(
                        primitiveisolation.RABBIT_CARCASS.get().getDefaultInstance(),
                        "jei.primitiveisolation.field_butchery.rabbit",
                        new ItemStack(primitiveisolation.RAW_RABBIT.get())),
                recipe(
                        primitiveisolation.POLAR_BEAR_CARCASS.get().getDefaultInstance(),
                        "jei.primitiveisolation.field_butchery.polar_bear",
                        new ItemStack(primitiveisolation.RAW_COD.get(), 2),
                        new ItemStack(primitiveisolation.RAW_SALMON.get(), 2)),
                recipe(
                        primitiveisolation.HOGLIN_CARCASS.get().getDefaultInstance(),
                        "jei.primitiveisolation.field_butchery.hoglin",
                        new ItemStack(primitiveisolation.RAW_PORK.get(), 4)));
    }

    private static FieldButcheryJeiRecipe recipe(
            ItemStack carcass,
            String detailsKey,
            ItemStack... outputs) {
        return new FieldButcheryJeiRecipe(carcass, List.of(outputs), detailsKey);
    }
}
