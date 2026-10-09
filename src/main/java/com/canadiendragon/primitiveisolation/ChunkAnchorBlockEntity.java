package com.canadiendragon.primitiveisolation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ChunkAnchorBlockEntity extends BlockEntity {
    public static final long TICKS_PER_PEARL = 20L * 60L * 60L;
    private long fuelTicks;
    private boolean checkedChunkTicket;

    public ChunkAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(primitiveisolation.CHUNK_ANCHOR_BLOCK_ENTITY.get(), pos, state);
    }

    public boolean hasFuel() {
        return this.fuelTicks > 0;
    }

    public void addFuel(ServerLevel level, long ticks) {
        this.fuelTicks = this.fuelTicks > Long.MAX_VALUE - ticks
                ? Long.MAX_VALUE
                : this.fuelTicks + ticks;
        this.checkedChunkTicket = true;
        this.setChanged();
        ChunkAnchorBlock.refreshChunkTicket(level, this.worldPosition);
    }

    public Component getStatus() {
        if (!Config.CHUNK_ANCHOR_REQUIRES_FUEL.getAsBoolean()) {
            return Component.translatable("message.primitiveisolation.chunk_anchor.always_loaded");
        }
        if (!this.hasFuel()) {
            return Component.translatable("message.primitiveisolation.chunk_anchor.unfueled");
        }

        long remainingMinutes = this.fuelTicks / 1200 + (this.fuelTicks % 1200 == 0 ? 0 : 1);
        return Component.translatable(
                "message.primitiveisolation.chunk_anchor.remaining",
                remainingMinutes / 60,
                remainingMinutes % 60);
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            ChunkAnchorBlockEntity anchor) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!anchor.checkedChunkTicket) {
            anchor.checkedChunkTicket = true;
            ChunkAnchorBlock.refreshChunkTicket(serverLevel, pos);
        }
        if (!Config.CHUNK_ANCHOR_REQUIRES_FUEL.getAsBoolean()) {
            return;
        }
        if (anchor.fuelTicks <= 0) {
            return;
        }

        serverLevel.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
        anchor.fuelTicks--;
        if (anchor.fuelTicks == 0) {
            anchor.setChanged();
            ChunkAnchorBlock.refreshChunkTicket(serverLevel, pos);
        } else if (anchor.fuelTicks % 20 == 0) {
            anchor.setChanged();
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.fuelTicks = Math.max(0, input.getLongOr("FuelTicks", 0));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("FuelTicks", this.fuelTicks);
    }
}
