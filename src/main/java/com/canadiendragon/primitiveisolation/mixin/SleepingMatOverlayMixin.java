package com.canadiendragon.primitiveisolation.mixin;

import com.canadiendragon.primitiveisolation.primitiveisolation;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
abstract class SleepingMatOverlayMixin {
    @Inject(method = "extractSleepOverlay", at = @At("HEAD"), cancellable = true)
    private void hideSleepingMatOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player != null
                && minecraft.level != null
                && player.getSleepingPos()
                        .map(pos -> minecraft.level.getBlockState(pos).is(primitiveisolation.SLEEPING_MAT.get()))
                        .orElse(false)) {
            ci.cancel();
        }
    }
}
