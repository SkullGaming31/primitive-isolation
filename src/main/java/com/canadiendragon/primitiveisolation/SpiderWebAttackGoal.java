package com.canadiendragon.primitiveisolation;

import java.util.EnumSet;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class SpiderWebAttackGoal extends Goal {
    private static final double MIN_RANGE_SQUARED = 16.0;
    private static final double MAX_RANGE_SQUARED = 144.0;
    private static final double STOP_RANGE_SQUARED = 9.0;
    private static final double CONTINUE_RANGE_SQUARED = 196.0;
    private static final double APPROACH_RANGE_SQUARED = 64.0;
    private static final int WEB_COOLDOWN_TICKS = 80;

    private final Spider spider;
    @Nullable
    private Player target;
    private int cooldown;

    public SpiderWebAttackGoal(Spider spider) {
        this.spider = spider;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!(this.spider.getTarget() instanceof Player target) || !target.isAlive()) {
            return false;
        }

        double distanceSquared = this.spider.distanceToSqr(target);
        if (distanceSquared < MIN_RANGE_SQUARED || distanceSquared > MAX_RANGE_SQUARED
                || !this.spider.getSensing().hasLineOfSight(target)) {
            return false;
        }
        this.target = target;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.target == null || !this.target.isAlive() || this.spider.getTarget() != this.target) {
            return false;
        }
        double distanceSquared = this.spider.distanceToSqr(this.target);
        return distanceSquared > STOP_RANGE_SQUARED && distanceSquared <= CONTINUE_RANGE_SQUARED;
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.target == null) {
            return;
        }

        this.spider.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        if (this.spider.distanceToSqr(this.target) > APPROACH_RANGE_SQUARED) {
            this.spider.getNavigation().moveTo(this.target, 1.0);
        } else {
            this.spider.getNavigation().stop();
        }

        if (this.cooldown > 0) {
            this.cooldown--;
        } else if (this.spider.getSensing().hasLineOfSight(this.target)) {
            SpiderWebProjectile projectile = SpiderWebProjectile.fromSpider(this.spider);
            Vec3 aim = new Vec3(
                    this.target.getX() - projectile.getX(),
                    this.target.getY() + this.target.getBbHeight() * 0.5 - projectile.getY(),
                    this.target.getZ() - projectile.getZ());
            projectile.shoot(aim.x, aim.y, aim.z, 1.25F, 1.0F);
            this.spider.level().addFreshEntity(projectile);
            this.cooldown = WEB_COOLDOWN_TICKS;
        }
    }
}
