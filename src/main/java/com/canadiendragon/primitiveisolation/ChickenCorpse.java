package com.canadiendragon.primitiveisolation;

import java.util.List;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ChickenCorpse extends Chicken {
    private final CorpseButchering butchering;

    public ChickenCorpse(EntityType<? extends ChickenCorpse> type, Level level) {
        super(type, level);
        this.butchering = new CorpseButchering(
                this,
                () -> primitiveisolation.CHICKEN_CARCASS.get().getDefaultInstance(),
                () -> List.of(new ItemStack(primitiveisolation.RAW_CHICKEN.get())));
        this.eggTime = Integer.MAX_VALUE;
        this.refreshDimensions();
    }

    @Override
    public void tick() {
        super.tick();
        this.eggTime = Integer.MAX_VALUE;
        this.flap = 0.0F;
        this.oFlap = 0.0F;
        this.flapSpeed = 0.0F;
        this.oFlapSpeed = 0.0F;
        this.butchering.tick();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        this.butchering.addAdditionalSaveData(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.butchering.readAdditionalSaveData(input);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return EntityDimensions.scalable(0.8F, 0.5F);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return this.butchering.mobInteract(player, hand);
    }
}
