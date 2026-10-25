package com.canadiendragon.primitiveisolation;

import java.util.List;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MooshroomCorpse extends MushroomCow {
    private final CorpseButchering butchering;

    public MooshroomCorpse(EntityType<? extends MooshroomCorpse> type, Level level) {
        super(type, level);
        this.butchering = new CorpseButchering(
                this,
                () -> primitiveisolation.MOOSHROOM_CARCASS.get().getDefaultInstance(),
                () -> primitiveisolation.mooshroomButcheredDrops(this.random));
    }

    @Override
    public void tick() {
        super.tick();
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
        return EntityDimensions.scalable(0.9F, 1.4F);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return this.butchering.mobInteract(player, hand);
    }
}
