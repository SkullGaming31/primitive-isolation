package com.canadiendragon.primitiveisolation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.MenuProvider;

public class PreservingBinBlockEntity extends BlockEntity implements MenuProvider {
    public static final int STORAGE_SLOT_COUNT = 27;
    public static final int ICE_SLOT = STORAGE_SLOT_COUNT;
    public static final int SLOT_COUNT = STORAGE_SLOT_COUNT + 1;
    public static final int TICKS_PER_ICE = 20 * 60 * 20 * 7;
    private int iceTicksRemaining;

    private final SimpleContainer inventory = new SimpleContainer(SLOT_COUNT) {
        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return slot >= 0 && slot < STORAGE_SLOT_COUNT
                    || slot == ICE_SLOT && stack.is(Items.ICE);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            PreservingBinBlockEntity.this.setChanged();
        }
    };

    public PreservingBinBlockEntity(BlockPos pos, BlockState state) {
        super(primitiveisolation.PRESERVING_BIN_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null) {
            Containers.dropContents(this.level, pos, this.inventory);
        }
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PreservingBinBlockEntity bin) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ItemStack ice = bin.inventory.getItem(ICE_SLOT);
        boolean cooling = !ice.isEmpty() && ice.is(Items.ICE);
        if (!cooling) {
            bin.iceTicksRemaining = 0;
        } else if (bin.iceTicksRemaining <= 0) {
            bin.iceTicksRemaining = TICKS_PER_ICE;
        }

        for (int slot = 0; slot < STORAGE_SLOT_COUNT; slot++) {
            ItemStack food = bin.inventory.getItem(slot);
            long spoilDuration = primitiveisolation.spoilDurationFor(food.getItem());
            if (spoilDuration <= 0) {
                continue;
            }

            if (PerishableFoodItem.hasSpoiled(food, serverLevel, spoilDuration)) {
                bin.inventory.setItem(slot, PerishableFoodItem.getSpoiledStack(food));
            } else if (cooling) {
                PerishableFoodItem.pauseSpoilage(food, serverLevel, spoilDuration);
            }
        }

        if (cooling && --bin.iceTicksRemaining <= 0) {
            ice.shrink(1);
            if (ice.isEmpty()) {
                bin.inventory.setItem(ICE_SLOT, ItemStack.EMPTY);
            } else {
                bin.inventory.setChanged();
            }
            bin.iceTicksRemaining = 0;
        }

        if (serverLevel.getGameTime() % 20 == 0) {
            bin.setChanged();
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.primitiveisolation.preserving_bin");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new PreservingBinMenu(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.inventory.getItems());
        this.iceTicksRemaining = Math.clamp(input.getIntOr("IceTicksRemaining", 0), 0, TICKS_PER_ICE);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.inventory.getItems());
        output.putInt("IceTicksRemaining", this.iceTicksRemaining);
    }
}
