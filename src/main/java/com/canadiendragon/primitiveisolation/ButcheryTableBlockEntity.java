package com.canadiendragon.primitiveisolation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class ButcheryTableBlockEntity extends BlockEntity {
    private ButcheryTableBlock.CarcassType cachedCarcassType = ButcheryTableBlock.CarcassType.EMPTY;
    @Nullable
    private Entity displayEntity;

    public ButcheryTableBlockEntity(BlockPos pos, BlockState state) {
        super(primitiveisolation.BUTCHERY_TABLE_BLOCK_ENTITY.get(), pos, state);
    }

    public @Nullable Entity getDisplayEntity() {
        Level level = this.level;
        ButcheryTableBlock.CarcassType carcassType = this.getBlockState().getValue(ButcheryTableBlock.CARCASS);
        if (level == null || carcassType == ButcheryTableBlock.CarcassType.EMPTY) {
            this.displayEntity = null;
            this.cachedCarcassType = ButcheryTableBlock.CarcassType.EMPTY;
            return null;
        }

        if (this.displayEntity == null || this.cachedCarcassType != carcassType) {
            EntityType<?> entityType = switch (carcassType) {
                case COW -> primitiveisolation.COW_CORPSE.get();
                case MOOSHROOM -> primitiveisolation.MOOSHROOM_CORPSE.get();
                case PIG -> primitiveisolation.PIG_CORPSE.get();
                case CHICKEN -> primitiveisolation.CHICKEN_CORPSE.get();
                case SHEEP -> primitiveisolation.SHEEP_CORPSE.get();
                case RABBIT -> primitiveisolation.RABBIT_CORPSE.get();
                case POLAR_BEAR -> primitiveisolation.POLAR_BEAR_CORPSE.get();
                case HOGLIN -> primitiveisolation.HOGLIN_CORPSE.get();
                case DEER -> primitiveisolation.DEER_CORPSE.get();
                case MOOSE -> primitiveisolation.MOOSE_CORPSE.get();
                case EMPTY -> throw new IllegalStateException("Empty carcasses do not have a display entity");
            };
            this.displayEntity = entityType.create(level, EntitySpawnReason.LOAD);
            this.cachedCarcassType = carcassType;
            if (this.displayEntity instanceof LivingEntity livingEntity) {
                livingEntity.setYRot(0.0F);
                livingEntity.setXRot(0.0F);
                livingEntity.yRotO = 0.0F;
                livingEntity.xRotO = 0.0F;
                livingEntity.setYHeadRot(0.0F);
                livingEntity.setYBodyRot(0.0F);
                livingEntity.yHeadRotO = 0.0F;
                livingEntity.yBodyRotO = 0.0F;
            }
        }

        if (this.displayEntity != null) {
            this.displayEntity.setPos(
                    this.worldPosition.getX() + 0.5,
                    this.worldPosition.getY() + 1.0,
                    this.worldPosition.getZ() + 0.5);
        }
        return this.displayEntity;
    }
}
