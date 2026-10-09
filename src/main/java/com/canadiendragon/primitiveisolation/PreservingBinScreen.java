package com.canadiendragon.primitiveisolation;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PreservingBinScreen extends AbstractContainerScreen<PreservingBinMenu> {
    public PreservingBinScreen(PreservingBinMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 194);
        this.inventoryLabelY = 98;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(
                this.leftPos,
                this.topPos,
                this.leftPos + this.imageWidth,
                this.topPos + this.imageHeight,
                0xFF39302A);
        graphics.fill(
                this.leftPos + 1,
                this.topPos + 1,
                this.leftPos + this.imageWidth - 1,
                this.topPos + this.imageHeight - 1,
                0xFFC6C6C6);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(graphics, 8 + column * 18, 18 + row * 18);
            }
        }
        drawSlot(graphics, 8, 78);
        graphics.text(
                this.font,
                Component.translatable("container.primitiveisolation.preserving_bin.ice_slot"),
                this.leftPos + 30,
                this.topPos + 83,
                0xFF404040,
                false);
        for (int column = 0; column < 9; column++) {
            drawSlot(graphics, 8 + column * 18, 110);
            drawSlot(graphics, 8 + column * 18, 128);
            drawSlot(graphics, 8 + column * 18, 146);
            drawSlot(graphics, 8 + column * 18, 168);
        }
    }

    private void drawSlot(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(this.leftPos + x, this.topPos + y, this.leftPos + x + 18, this.topPos + y + 18, 0xFF373737);
        graphics.fill(this.leftPos + x + 1, this.topPos + y + 1, this.leftPos + x + 17, this.topPos + y + 17, 0xFF8B8B8B);
    }
}
