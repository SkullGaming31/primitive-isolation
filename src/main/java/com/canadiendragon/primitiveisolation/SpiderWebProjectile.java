package com.canadiendragon.primitiveisolation;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class SpiderWebProjectile extends ThrowableItemProjectile {
    public SpiderWebProjectile(EntityType<? extends SpiderWebProjectile> type, Level level) {
        super(type, level);
    }

    public static SpiderWebProjectile fromSpider(Spider spider) {
        SpiderWebProjectile projectile = new SpiderWebProjectile(
                primitiveisolation.SPIDER_WEB_PROJECTILE.get(),
                spider.level());
        projectile.setOwner(spider);
        projectile.setPos(spider.getX(), spider.getEyeY() - 0.1F, spider.getZ());
        return projectile;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.COBWEB;
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (!this.level().isClientSide() && hitResult.getEntity() instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2), this.getOwner());
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
