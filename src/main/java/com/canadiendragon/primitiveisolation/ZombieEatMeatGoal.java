package com.canadiendragon.primitiveisolation;

import java.util.Comparator;
import java.util.EnumSet;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Items;

public class ZombieEatMeatGoal extends Goal {
    private static final double SEARCH_RADIUS = 16.0;
    private static final double EAT_DISTANCE_SQUARED = 2.25;
    private static final double SPEED = 1.0;
    private static final int EAT_DURATION_TICKS = 200;

    private final Zombie zombie;
    private Entity food;
    private int pathRecalculationDelay;
    private int eatingTicks;

    public ZombieEatMeatGoal(Zombie zombie) {
        this.zombie = zombie;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        this.food = this.zombie.level()
                .getEntitiesOfClass(Entity.class, this.zombie.getBoundingBox().inflate(SEARCH_RADIUS), ZombieEatMeatGoal::isFood)
                .stream()
                .min(Comparator.comparingDouble(this.zombie::distanceToSqr))
                .orElse(null);
        return this.food != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.food != null && isFood(this.food);
    }

    @Override
    public void start() {
        this.pathRecalculationDelay = 0;
        this.eatingTicks = 0;
    }

    @Override
    public void stop() {
        this.zombie.getNavigation().stop();
        this.food = null;
        this.eatingTicks = 0;
    }

    @Override
    public void tick() {
        if (this.food == null) {
            return;
        }

        if (this.zombie.distanceToSqr(this.food) <= EAT_DISTANCE_SQUARED) {
            this.zombie.getNavigation().stop();
            if (++this.eatingTicks >= EAT_DURATION_TICKS) {
                this.food.discard();
                this.food = null;
            }
            return;
        }

        this.eatingTicks = 0;
        if (this.pathRecalculationDelay-- <= 0) {
            this.pathRecalculationDelay = 10;
            this.zombie.getNavigation().moveTo(this.food.getX(), this.food.getY(), this.food.getZ(), SPEED);
        }
    }

    private static boolean isFood(Entity entity) {
        return entity.isAlive()
                && (entity instanceof CowCorpse
                        || entity instanceof PigCorpse
                        || entity instanceof ChickenCorpse
                        || entity instanceof ItemEntity item && isMeat(item));
    }

    private static boolean isMeat(ItemEntity item) {
        return item.isAlive() && (item.getItem().is(Items.BEEF)
                || item.getItem().is(Items.PORKCHOP)
                || item.getItem().is(Items.CHICKEN)
                || item.getItem().is(primitiveisolation.COW_CARCASS.get())
                || item.getItem().is(primitiveisolation.PIG_CARCASS.get())
                || item.getItem().is(primitiveisolation.CHICKEN_CARCASS.get()));
    }
}
