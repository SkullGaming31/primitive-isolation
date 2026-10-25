package com.canadiendragon.primitiveisolation;

import java.util.Comparator;
import java.util.EnumSet;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SquidGrabPlayerGoal extends Goal {
    private static final double HORIZONTAL_RANGE_SQUARED = 16.0;
    private static final double MAX_VERTICAL_DISTANCE = 3.5;
    private static final int GRAB_COOLDOWN_TICKS = 100;

    private final Squid squid;
    @Nullable
    private Player target;
    private int cooldown;

    public SquidGrabPlayerGoal(Squid squid) {
        this.squid = squid;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    public static boolean isWithinGrabRange(Squid squid, Player player) {
        double dx = squid.getX() - player.getX();
        double dz = squid.getZ() - player.getZ();
        return squid.isInWater()
                && dx * dx + dz * dz <= HORIZONTAL_RANGE_SQUARED
                && Math.abs(squid.getY() - player.getY()) <= MAX_VERTICAL_DISTANCE;
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        this.target = this.squid.level()
                .getEntitiesOfClass(
                        Player.class,
                        this.squid.getBoundingBox().inflate(
                                Math.sqrt(HORIZONTAL_RANGE_SQUARED),
                                MAX_VERTICAL_DISTANCE,
                                Math.sqrt(HORIZONTAL_RANGE_SQUARED)),
                        player -> player.isAlive()
                                && !player.isInWater()
                                && isWithinGrabRange(this.squid, player))
                .stream()
                .min(Comparator.comparingDouble(this.squid::distanceToSqr))
                .orElse(null);
        if (this.target == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null
                && this.target.isAlive()
                && !this.target.isInWater()
                && this.squid.isInWater()
                && isWithinGrabRange(this.squid, this.target);
    }

    @Override
    public void stop() {
        if (this.target != null) {
            this.cooldown = GRAB_COOLDOWN_TICKS;
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
        Vec3 pull = this.squid.position().subtract(this.target.position()).normalize();
        this.target.push(pull.x * 0.32, Math.min(-0.12, pull.y * 0.25), pull.z * 0.32);
        this.target.hurtMarked = true;
    }
}
