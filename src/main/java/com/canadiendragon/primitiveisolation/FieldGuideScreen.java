package com.canadiendragon.primitiveisolation;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

public class FieldGuideScreen extends Screen {
    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 210;
    private static final String[] FIELDS = {"category", "habitat", "harvest", "raw", "preparation"};

    private final ItemStack guide;
    private final List<WildPlantBlock.Species> entries;
    private int currentEntry;

    public FieldGuideScreen(ItemStack guide) {
        super(Component.translatable("item.primitiveisolation.field_guide"));
        this.guide = guide.copy();
        this.entries = FieldGuideItem.identifiedSpecies(this.guide);
    }

    @Override
    protected void init() {
        int buttonY = this.height - 30;
        Button previous = this.addRenderableWidget(Button.builder(
                        Component.translatable("item.primitiveisolation.field_guide.previous"),
                        button -> this.moveEntry(-1))
                .bounds(this.width / 2 - 111, buttonY, 72, 20)
                .build());
        Button next = this.addRenderableWidget(Button.builder(
                        Component.translatable("item.primitiveisolation.field_guide.next"),
                        button -> this.moveEntry(1))
                .bounds(this.width / 2 + 39, buttonY, 72, 20)
                .build());
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.done"),
                        button -> this.onClose())
                .bounds(this.width / 2 - 35, buttonY, 70, 20)
                .build());
        previous.active = this.entries.size() > 1;
        next.active = this.entries.size() > 1;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xA0000000);

        int panelWidth = Math.min(PANEL_WIDTH, this.width - 12);
        int panelHeight = Math.min(PANEL_HEIGHT, this.height - 58);
        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2 - 4;
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xFF39302A);
        graphics.fill(left + 2, top + 2, left + panelWidth - 2, top + panelHeight - 2, 0xFFE3D5B8);
        graphics.text(
                this.font,
                this.title,
                (this.width - this.font.width(this.title)) / 2,
                top + 12,
                0xFF493A2A,
                false);

        if (this.entries.isEmpty()) {
            Component emptyMessage = Component.translatable("item.primitiveisolation.field_guide.empty");
            graphics.text(
                    this.font,
                    emptyMessage,
                    (this.width - this.font.width(emptyMessage)) / 2,
                    top + 82,
                    0xFF554D42,
                    false);
            return;
        }

        WildPlantBlock.Species species = this.entries.get(this.currentEntry);
        Component name = FieldGuideItem.getEntryName(this.guide, species);
        graphics.text(
                this.font,
                name,
                (this.width - this.font.width(name)) / 2,
                top + 31,
                0xFF5D6F35,
                false);

        Component page = Component.translatable(
                "item.primitiveisolation.field_guide.page",
                this.currentEntry + 1,
                this.entries.size(),
                WildPlantBlock.Species.values().length);
        graphics.text(
                this.font,
                page,
                (this.width - this.font.width(page)) / 2,
                top + 45,
                0xFF766B58,
                false);

        int labelX = left + 16;
        int valueX = left + 112;
        int valueWidth = panelWidth - 126;
        int y = top + 66;
        for (String field : FIELDS) {
            Component label = Component.translatable("item.primitiveisolation.field_guide." + field);
            List<FormattedCharSequence> lines = this.font.split(
                    FieldGuideItem.getEntryField(this.guide, species, field),
                    valueWidth);
            graphics.text(this.font, label, labelX, y, 0xFF685333, false);
            for (FormattedCharSequence line : lines) {
                graphics.text(this.font, line, valueX, y, 0xFF39342D, false);
                y += 11;
            }
            y += 4;
        }
    }

    private void moveEntry(int direction) {
        if (this.entries.isEmpty()) {
            return;
        }
        this.currentEntry = Math.floorMod(this.currentEntry + direction, this.entries.size());
    }
}
