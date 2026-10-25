package com.canadiendragon.primitiveisolation;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

public abstract class AquaticPredator extends WaterAnimal {
    protected AquaticPredator(EntityType<? extends AquaticPredator> type, Level level) {
        super(type, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.5F, 0.1F, false);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes(double health, double speed, double attackDamage) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, attackDamage)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15, true));
        this.goalSelector.addGoal(2, new RandomSwimmingGoal(this, 1.0, 40));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void travelInWater(Vec3 input, double baseGravity, boolean isFalling, double oldY) {
        this.moveRelative(this.getSpeed(), input);
        this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
        if (this.getTarget() == null) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
        }
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()
                && this.getTarget() instanceof Player player
                && !player.isInWater()) {
            this.setTarget(null);
        }
        super.tick();
    }

    protected void targetPlayersInWater() {
        this.targetSelector.addGoal(
                1,
                new NearestAttackableTargetGoal<>(
                        this,
                        Player.class,
                        false,
                        (player, level) -> player.isInWater()));
    }
}
