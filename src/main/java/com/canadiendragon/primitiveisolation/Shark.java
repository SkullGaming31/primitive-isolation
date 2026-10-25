package com.canadiendragon.primitiveisolation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Shark extends AquaticPredator {
    public Shark(EntityType<? extends Shark> type, Level level) {
        super(type, level);
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
        return AquaticPredator.createAttributes(24.0, 0.24, 6.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetPlayersInWater();
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.spawnAtLocation(level, new ItemStack(primitiveisolation.RAW_SHARK_MEAT.get(), 1 + this.random.nextInt(2)));
    }
}
