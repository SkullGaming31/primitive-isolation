package com.canadiendragon.primitiveisolation;

import java.util.EnumSet;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class SquidInkAttackGoal extends Goal {
    private static final double MIN_RANGE_SQUARED = 0.25;
    private static final double MAX_RANGE_SQUARED = 100.0;
    private static final double CONTINUE_RANGE_SQUARED = 256.0;
    private static final int INK_COOLDOWN_TICKS = 600;

    private final Squid squid;
    @Nullable
    private Player target;
    private int cooldown;

    public SquidInkAttackGoal(Squid squid) {
        this.squid = squid;
        this.cooldown = 40;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.squid.isInWater()
                || !(this.squid.getTarget() instanceof Player target)
                || !target.isAlive()
                || !target.isInWater()) {
            return false;
        }

        double distanceSquared = this.squid.distanceToSqr(target);
        if (distanceSquared < MIN_RANGE_SQUARED
                || distanceSquared > MAX_RANGE_SQUARED) {
            return false;
        }
        this.target = target;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null
                && this.target.isAlive()
                && this.target.isInWater()
                && this.squid.isInWater()
                && this.squid.getTarget() == this.target
                && this.squid.distanceToSqr(this.target) <= CONTINUE_RANGE_SQUARED;
    }

    @Override
    public void stop() {
        if (this.target != null && !this.target.isInWater() && this.squid.getTarget() == this.target) {
            this.squid.setTarget(null);
        }
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

        this.squid.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        if (this.cooldown > 0) {
            this.cooldown--;
        } else if (this.target.isInWater()) {
            SquidInkProjectile projectile = SquidInkProjectile.fromSquid(this.squid);
            Vec3 aim = new Vec3(
                    this.target.getX() - projectile.getX(),
                    this.target.getY() + this.target.getBbHeight() * 0.5 - projectile.getY(),
                    this.target.getZ() - projectile.getZ());
            projectile.shoot(aim.x, aim.y, aim.z, 2.5F, 0.1F);
            this.squid.level().addFreshEntity(projectile);
            this.cooldown = INK_COOLDOWN_TICKS;
        }
    }
}
