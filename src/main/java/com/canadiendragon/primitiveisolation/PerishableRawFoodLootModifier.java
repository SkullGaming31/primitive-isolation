package com.canadiendragon.primitiveisolation;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class PerishableRawFoodLootModifier extends LootModifier {
    public static final MapCodec<PerishableRawFoodLootModifier> CODEC = RecordCodecBuilder.mapCodec(
            instance -> LootModifier.codecStart(instance).apply(instance, PerishableRawFoodLootModifier::new));

    public PerishableRawFoodLootModifier(LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        for (int i = 0; i < generatedLoot.size(); i++) {
            ItemStack stack = generatedLoot.get(i);
            Item replacement = primitiveisolation.perishableFoodFor(stack.getItem());
            if (replacement != null) {
                generatedLoot.set(i, new ItemStack(replacement, stack.getCount()));
            }
        }
        return generatedLoot;
    }
}
