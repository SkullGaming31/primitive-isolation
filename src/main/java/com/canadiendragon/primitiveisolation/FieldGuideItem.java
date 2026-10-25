package com.canadiendragon.primitiveisolation;

import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class FieldGuideItem extends Item {
    private static final String IDENTIFIED_PREFIX = "PrimitiveIsolationPlant:";

    public FieldGuideItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            ClientSide.open(player.getItemInHand(hand));
        }
        return InteractionResult.SUCCESS;
    }

    public static void identify(ItemStack guide, WildPlantBlock.Species species, Player player) {
        String key = IDENTIFIED_PREFIX + species.id();
        var existingData = guide.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        boolean alreadyKnown = existingData.getBoolean(key).orElse(false);
        if (!alreadyKnown) {
            CustomData.update(
                    DataComponents.CUSTOM_DATA,
                    guide,
                    data -> {
                        data.putBoolean(key, true);
                        data.putString(key + ":name", species.nameKey());
                        for (String field : new String[] {"category", "habitat", "harvest", "raw", "preparation"}) {
                            data.putString(key + ":" + field, species.guideKey(field));
                        }
                    });
        }

        player.sendOverlayMessage(Component.translatable(
                alreadyKnown
                        ? "message.primitiveisolation.field_guide.already_identified"
                        : "message.primitiveisolation.field_guide.identified",
                Component.translatable(species.nameKey())));
    }

    public static List<WildPlantBlock.Species> identifiedSpecies(ItemStack guide) {
        var data = guide.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        List<WildPlantBlock.Species> identified = new ArrayList<>();
        for (WildPlantBlock.Species species : WildPlantBlock.Species.values()) {
            if (data.getBoolean(IDENTIFIED_PREFIX + species.id()).orElse(false)) {
                identified.add(species);
            }
        }
        return List.copyOf(identified);
    }

    public static Component getEntryName(ItemStack guide, WildPlantBlock.Species species) {
        String key = IDENTIFIED_PREFIX + species.id();
        String nameKey = guide.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getString(key + ":name")
                .orElse(species.nameKey());
        return Component.translatable(nameKey);
    }

    public static Component getEntryField(
            ItemStack guide,
            WildPlantBlock.Species species,
            String field) {
        String key = IDENTIFIED_PREFIX + species.id();
        String fieldKey = guide.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getString(key + ":" + field)
                .orElse(species.guideKey(field));
        return Component.translatable(fieldKey);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> tooltip,
            TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.primitiveisolation.field_guide.use")
                .withStyle(ChatFormatting.GRAY));
    }

    private static final class ClientSide {
        private static void open(ItemStack guide) {
            primitiveisolationClient.openFieldGuide(guide);
        }
    }
}
