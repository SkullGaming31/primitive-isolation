package com.canadiendragon.primitiveisolation;

import java.util.List;
import java.util.Map;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class KnappingRecipes {
    public static final List<PatternRecipe> HEADS = List.of(
            new PatternRecipe(
                    primitiveisolation.FLINT_AXE_HEAD.get(),
                    List.of("FF ", "FS ", "   "),
                    Map.of('F', Items.FLINT, 'S', Items.STRING)),
            new PatternRecipe(
                    primitiveisolation.FLINT_PICKAXE_HEAD.get(),
                    List.of("FFF", " S ", "   "),
                    Map.of('F', Items.FLINT, 'S', Items.STRING)),
            new PatternRecipe(
                    primitiveisolation.FLINT_HOE_HEAD.get(),
                    List.of("FF ", "S  ", "   "),
                    Map.of('F', Items.FLINT, 'S', Items.STRING)),
            new PatternRecipe(
                    primitiveisolation.FLINT_SHOVEL_HEAD.get(),
                    List.of("F  ", "S  ", "   "),
                    Map.of('F', Items.FLINT, 'S', Items.STRING)),
            new PatternRecipe(
                    primitiveisolation.FLINT_KNIFE_HEAD.get(),
                    List.of("FS ", "   ", "   "),
                    Map.of('F', Items.FLINT, 'S', Items.STRING)));

    public static final List<PatternRecipe> ASSEMBLIES = List.of(
            assembly(primitiveisolation.FLINT_AXE.get(), primitiveisolation.FLINT_AXE_HEAD.get()),
            assembly(primitiveisolation.FLINT_PICKAXE.get(), primitiveisolation.FLINT_PICKAXE_HEAD.get()),
            assembly(primitiveisolation.FLINT_HOE.get(), primitiveisolation.FLINT_HOE_HEAD.get()),
            assembly(primitiveisolation.FLINT_SHOVEL.get(), primitiveisolation.FLINT_SHOVEL_HEAD.get()),
            assembly(primitiveisolation.FLINT_KNIFE.get(), primitiveisolation.FLINT_KNIFE_HEAD.get()));

    private KnappingRecipes() {
    }

    private static PatternRecipe assembly(Item output, Item head) {
        return new PatternRecipe(
                output,
                List.of(" H ", " T ", " S "),
                Map.of('H', head, 'T', Items.STRING, 'S', Items.STICK));
    }

    public record PatternRecipe(Item output, List<String> pattern, Map<Character, Item> key) {
        public PatternRecipe {
            pattern = List.copyOf(pattern);
            key = Map.copyOf(key);
        }
    }
}
