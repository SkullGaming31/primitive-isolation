package com.canadiendragon.primitiveisolation;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class SquidInkProjectile extends ThrowableItemProjectile {
    public SquidInkProjectile(EntityType<? extends SquidInkProjectile> type, Level level) {
        super(type, level);
    }

    public static SquidInkProjectile fromSquid(Squid squid) {
        SquidInkProjectile projectile = new SquidInkProjectile(
                primitiveisolation.SQUID_INK_PROJECTILE.get(),
                squid.level());
        projectile.setOwner(squid);
        projectile.setPos(squid.getX(), squid.getEyeY(), squid.getZ());
        return projectile;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.INK_SAC;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (!this.level().isClientSide() && hitResult.getEntity() instanceof LivingEntity target) {
            target.hurt(this.damageSources().thrown(this, this.getOwner()), 2.0F);
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }
}
