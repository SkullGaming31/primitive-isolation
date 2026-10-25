package com.canadiendragon.primitiveisolation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class WildPlantItem extends Item {
    private final WildPlantBlock plant;
    private final Effect effect;

    public WildPlantItem(Properties properties, WildPlantBlock plant, Effect effect) {
        super(properties);
        this.plant = plant;
        this.effect = effect;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (this.plant == null) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        BlockState state = this.plant.defaultBlockState();
        if (!level.getBlockState(pos).canBeReplaced() || !state.canSurvive(level, pos)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state, 3);
            if (context.getPlayer() == null || !context.getPlayer().hasInfiniteMaterials()) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (level.isClientSide()) {
            return result;
        }

        switch (this.effect) {
            case MEDICINAL -> {
                entity.heal(2.0F);
                entity.removeEffect(MobEffects.POISON);
            }
            case POISONOUS -> entity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
            case NONE -> {
            }
        }
        return result;
    }

    public enum Effect {
        NONE,
        MEDICINAL,
        POISONOUS
    }
}
