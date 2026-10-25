package com.canadiendragon.primitiveisolation;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PlantDryerScreen extends AbstractContainerScreen<PlantDryerMenu> {
    public PlantDryerScreen(PlantDryerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        this.inventoryLabelY = 72;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xFF39302A);
        graphics.fill(this.leftPos + 1, this.topPos + 1, this.leftPos + this.imageWidth - 1, this.topPos + this.imageHeight - 1, 0xFFC6C6C6);
        drawSlot(graphics, 53, 30);
        drawSlot(graphics, 107, 30);
        for (int column = 0; column < 9; column++) {
            drawSlot(graphics, 8 + column * 18, 84);
            drawSlot(graphics, 8 + column * 18, 102);
            drawSlot(graphics, 8 + column * 18, 120);
            drawSlot(graphics, 8 + column * 18, 142);
        }

        graphics.fill(this.leftPos + 42, this.topPos + 57, this.leftPos + 134, this.topPos + 66, 0xFF555555);
        graphics.fill(this.leftPos + 43, this.topPos + 58, this.leftPos + 133, this.topPos + 65, 0xFF303030);
        int required = this.menu.getDryingProgressRequired();
        int progressWidth = required <= 0 ? 0 : 88 * this.menu.getDryingProgress() / required;
        if (progressWidth > 0) {
            graphics.fill(
                    this.leftPos + 44,
                    this.topPos + 59,
                    this.leftPos + 44 + progressWidth,
                    this.topPos + 64,
                    0xFF638B53);
        }
    }

    private void drawSlot(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(this.leftPos + x, this.topPos + y, this.leftPos + x + 18, this.topPos + y + 18, 0xFF373737);
        graphics.fill(this.leftPos + x + 1, this.topPos + y + 1, this.leftPos + x + 17, this.topPos + y + 17, 0xFF8B8B8B);
    }
}
