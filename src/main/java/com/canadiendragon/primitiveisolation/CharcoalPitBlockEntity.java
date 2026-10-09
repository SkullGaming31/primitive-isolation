package com.canadiendragon.primitiveisolation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class CharcoalPitBlockEntity extends BlockEntity {
    private static final int MIN_SIZE = 3;
    private static final int MAX_SIZE = 9;
    private static final int TICKS_PER_LOG = 150;
    private static final int MIN_CORE_USES = 1;
    private static final int MAX_CORE_USES = 10;
    private int pitSize;
    private int remainingTicks;
    private int coreMaxUses;
    private int coreCompletedUses;

    public CharcoalPitBlockEntity(BlockPos pos, BlockState state) {
        super(primitiveisolation.CHARCOAL_PIT_BLOCK_ENTITY.get(), pos, state);
    }

    public boolean isActive() {
        return this.getBlockState().getValue(CharcoalPitBlock.ACTIVE);
    }

    public int getLogCount() {
        return Math.max(0, this.pitSize * this.pitSize - 1);
    }

    public int getRemainingTimeSeconds() {
        return Math.max(0, (this.remainingTicks + 19) / 20);
    }

    public Component getInvalidReason() {
        if (this.level == null) {
            return Component.translatable("message.primitiveisolation.charcoal_pit.invalid.unavailable");
        }
        Component reason = validatePit(this.level, this.worldPosition, MAX_SIZE);
        return reason != null
                ? reason
                : Component.translatable("message.primitiveisolation.charcoal_pit.invalid.unavailable");
    }

    public int startPit() {
        if (this.level == null || this.level.isClientSide()) {
            return 0;
        }

        for (int size = MAX_SIZE; size >= MIN_SIZE; size -= 2) {
            if (validatePit(this.level, this.worldPosition, size) == null) {
                this.pitSize = size;
                this.ensureCoreLifetime();
                this.remainingTicks = this.getLogCount() * TICKS_PER_LOG;
                this.setActive(true);
                this.setChanged();
                return this.remainingTicks;
            }
        }
        return 0;
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            CharcoalPitBlockEntity pit) {
        if (!pit.isActive()) {
            return;
        }

        pit.ensureCoreLifetime();
        if (pit.pitSize < MIN_SIZE
                || pit.pitSize > MAX_SIZE
                || pit.pitSize % 2 == 0
                || pit.remainingTicks <= 0) {
            pit.cancel();
            return;
        }

        pit.remainingTicks--;
        if (pit.remainingTicks == 0) {
            pit.finish((ServerLevel) level);
        } else if (pit.remainingTicks % 20 == 0) {
            pit.setChanged();
        }
    }

    private void finish(ServerLevel level) {
        if (validatePit(level, this.worldPosition, this.pitSize) != null) {
            this.cancel();
            return;
        }

        int logs = this.getLogCount();
        int half = this.pitSize / 2;
        for (int x = -half; x <= half; x++) {
            for (int z = -half; z <= half; z++) {
                if (x != 0 || z != 0) {
                    level.setBlock(this.worldPosition.offset(x, 0, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        int remainingCharcoal = logs;
        while (remainingCharcoal > 0) {
            int stackSize = Math.min(remainingCharcoal, Items.CHARCOAL.getDefaultMaxStackSize());
            ItemEntity charcoal = new ItemEntity(
                    level,
                    this.worldPosition.getX() + 0.5,
                    this.worldPosition.getY() + 1.0,
                    this.worldPosition.getZ() + 0.5,
                    new ItemStack(Items.CHARCOAL, stackSize));
            if (!level.addFreshEntity(charcoal)) {
                primitiveisolation.LOGGER.error("Could not spawn charcoal output for pit at {}", this.worldPosition);
            }
            remainingCharcoal -= stackSize;
        }

        this.coreCompletedUses++;
        if (this.coreCompletedUses >= this.coreMaxUses) {
            level.setBlock(this.worldPosition, Blocks.AIR.defaultBlockState(), 3);
        } else {
            this.reset();
        }
    }

    private void cancel() {
        this.reset();
    }

    private void reset() {
        this.pitSize = 0;
        this.remainingTicks = 0;
        this.setActive(false);
        this.setChanged();
    }

    private void ensureCoreLifetime() {
        if (this.coreMaxUses <= 0 && this.level != null) {
            this.coreMaxUses = this.level.getRandom().nextIntBetweenInclusive(MIN_CORE_USES, MAX_CORE_USES);
            this.coreCompletedUses = 0;
            this.setChanged();
        }
    }

    private void setActive(boolean active) {
        if (this.level != null && this.getBlockState().getValue(CharcoalPitBlock.ACTIVE) != active) {
            this.level.setBlock(
                    this.worldPosition,
                    this.getBlockState().setValue(CharcoalPitBlock.ACTIVE, active),
                    3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.pitSize = input.getIntOr("PitSize", 0);
        this.remainingTicks = input.getIntOr("RemainingTicks", 0);
        this.coreMaxUses = Math.clamp(input.getIntOr("CoreMaxUses", 0), 0, MAX_CORE_USES);
        this.coreCompletedUses = Math.clamp(input.getIntOr("CoreCompletedUses", 0), 0, MAX_CORE_USES);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("PitSize", this.pitSize);
        output.putInt("RemainingTicks", this.remainingTicks);
        output.putInt("CoreMaxUses", this.coreMaxUses);
        output.putInt("CoreCompletedUses", this.coreCompletedUses);
    }

    private static @Nullable Component validatePit(Level level, BlockPos center, int size) {
        if (size < MIN_SIZE || size > MAX_SIZE || size % 2 == 0) {
            return Component.translatable("message.primitiveisolation.charcoal_pit.invalid.size");
        }

        int half = size / 2;
        if (!level.getBlockState(center).is(primitiveisolation.CHARCOAL_PIT.get())) {
            return Component.translatable(
                    "message.primitiveisolation.charcoal_pit.invalid.core",
                    formatPos(center));
        }
        if (!level.getBlockState(center.above()).isAir()) {
            return Component.translatable(
                    "message.primitiveisolation.charcoal_pit.invalid.chimney",
                    formatPos(center.above()));
        }

        for (int x = -half; x <= half; x++) {
            for (int z = -half; z <= half; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }

                BlockPos logPos = center.offset(x, 0, z);
                if (!level.getBlockState(logPos).is(BlockTags.LOGS)) {
                    return Component.translatable(
                            "message.primitiveisolation.charcoal_pit.invalid.log",
                            size,
                            size,
                            formatPos(logPos));
                }
                if (!isPitEarth(level.getBlockState(logPos.above()))) {
                    return Component.translatable(
                            "message.primitiveisolation.charcoal_pit.invalid.cover",
                            formatPos(logPos.above()));
                }
            }
        }

        int wall = half + 1;
        for (int x = -wall; x <= wall; x++) {
            for (int z = -wall; z <= wall; z++) {
                if (Math.abs(x) != wall && Math.abs(z) != wall) {
                    continue;
                }

                BlockPos wallPos = center.offset(x, 0, z);
                boolean vent = x == 0 && Math.abs(z) == wall;
                if (vent && !level.getBlockState(wallPos).isAir()) {
                    return Component.translatable(
                            "message.primitiveisolation.charcoal_pit.invalid.vent",
                            formatPos(wallPos));
                }
                if (!vent && !isPitEarth(level.getBlockState(wallPos))) {
                    return Component.translatable(
                            "message.primitiveisolation.charcoal_pit.invalid.wall",
                            formatPos(wallPos));
                }
            }
        }
        return null;
    }

    private static String formatPos(BlockPos pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    private static boolean isPitEarth(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.CLAY);
    }
}
