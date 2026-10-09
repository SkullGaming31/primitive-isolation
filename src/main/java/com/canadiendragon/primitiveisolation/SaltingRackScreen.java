package com.canadiendragon.primitiveisolation;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SaltingRackScreen extends AbstractContainerScreen<SaltingRackMenu> {
    public SaltingRackScreen(SaltingRackMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 178);
        this.inventoryLabelY = 84;
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

        for (int slot = 0; slot < 3; slot++) {
            drawSlot(graphics, 30 + slot * 18, 17);
            drawSlot(graphics, 30 + slot * 18, 66);
        }
        drawSlot(graphics, 48, 42);
        for (int column = 0; column < 9; column++) {
            drawSlot(graphics, 8 + column * 18, 96);
            drawSlot(graphics, 8 + column * 18, 114);
            drawSlot(graphics, 8 + column * 18, 132);
            drawSlot(graphics, 8 + column * 18, 154);
        }

        graphics.fill(this.leftPos + 103, this.topPos + 44, this.leftPos + 158, this.topPos + 53, 0xFF555555);
        graphics.fill(this.leftPos + 104, this.topPos + 45, this.leftPos + 157, this.topPos + 52, 0xFF303030);
        int progressWidth = 51 * this.menu.getProcessTicks() / SaltingRackBlockEntity.TICKS_PER_PIECE;
        if (progressWidth > 0) {
            graphics.fill(
                    this.leftPos + 105,
                    this.topPos + 46,
                    this.leftPos + 105 + progressWidth,
                    this.topPos + 51,
                    0xFF87A85B);
        }
    }

    private void drawSlot(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(this.leftPos + x, this.topPos + y, this.leftPos + x + 18, this.topPos + y + 18, 0xFF373737);
        graphics.fill(this.leftPos + x + 1, this.topPos + y + 1, this.leftPos + x + 17, this.topPos + y + 17, 0xFF8B8B8B);
    }
}
