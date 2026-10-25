package com.canadiendragon.primitiveisolation;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.player.Player;

public final class DeerAvoidPlayerGoal extends AvoidEntityGoal<Player> {
    @Nullable
    private Player checkedCrouchingPlayer;
    private boolean allowCrouchingPlayerToApproach;

    public DeerAvoidPlayerGoal(Deer deer) {
        super(deer, Player.class, 10.0F, 1.4, 1.7);
    }

    @Override
    public boolean canUse() {
        boolean canAvoidPlayer = super.canUse();
        Player player = this.toAvoid;
        if (player == null || !player.isCrouching()) {
            this.checkedCrouchingPlayer = null;
            this.allowCrouchingPlayerToApproach = false;
            return canAvoidPlayer;
        }

        if (player != this.checkedCrouchingPlayer) {
            this.checkedCrouchingPlayer = player;
            this.allowCrouchingPlayerToApproach = this.mob.getRandom().nextInt(5) == 0;
        }

        return canAvoidPlayer && !this.allowCrouchingPlayerToApproach;
    }
}
