package com.canadiendragon.primitiveisolation.mixin;

import com.canadiendragon.primitiveisolation.SleepingMatSleep;

import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerLevel.class)
abstract class ServerLevelSleepMixin {
    @Invoker("wakeUpAllPlayers")
    protected abstract void primitiveisolation$wakeUpAllPlayers();

    @Redirect(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;wakeUpAllPlayers()V"))
    private void keepSleepingMatPlayersAsleep(ServerLevel level) {
        if (!SleepingMatSleep.hasSleepingMatPlayer(level)) {
            this.primitiveisolation$wakeUpAllPlayers();
        }
    }
}
