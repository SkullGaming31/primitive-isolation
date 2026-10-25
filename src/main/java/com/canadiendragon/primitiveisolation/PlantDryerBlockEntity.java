package com.canadiendragon.primitiveisolation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.MenuProvider;

public class PlantDryerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int SLOT_COUNT = 2;
    public static final int DRYING_PROGRESS_REQUIRED = 1_200;

    private int dryingProgress;
    private final SimpleContainer inventory = new SimpleContainer(SLOT_COUNT) {
        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return slot == INPUT_SLOT && isDryable(stack);
        }

        @Override
        public boolean canTakeItem(Container into, int slot, ItemStack stack) {
            return slot != INPUT_SLOT || PlantDryerBlockEntity.this.dryingProgress == 0;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            PlantDryerBlockEntity.this.setChanged();
        }
    };
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> PlantDryerBlockEntity.this.dryingProgress;
                case 1 -> DRYING_PROGRESS_REQUIRED;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                PlantDryerBlockEntity.this.dryingProgress = Math.clamp(value, 0, DRYING_PROGRESS_REQUIRED);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public PlantDryerBlockEntity(BlockPos pos, BlockState state) {
        super(primitiveisolation.PLANT_DRYER_BLOCK_ENTITY.get(), pos, state);
    }

    public static boolean isDryable(ItemStack stack) {
        return stack.is(primitiveisolation.YARROW.get())
                || stack.is(primitiveisolation.BITTERLEAF.get());
    }

    public static ItemStack getDriedResult(ItemStack input) {
        Item output = input.is(primitiveisolation.YARROW.get())
                ? primitiveisolation.DRIED_YARROW.get()
                : primitiveisolation.DRIED_BITTERLEAF.get();
        return new ItemStack(output);
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

    public ContainerData getContainerData() {
        return this.data;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PlantDryerBlockEntity dryer) {
        if (level.isClientSide()) {
            return;
        }

        ItemStack input = dryer.inventory.getItem(INPUT_SLOT);
        if (input.isEmpty()) {
            if (dryer.dryingProgress != 0) {
                dryer.dryingProgress = 0;
                dryer.setChanged();
            }
            return;
        }

        if (!isDryable(input)) {
            dryer.inventory.setItem(INPUT_SLOT, ItemStack.EMPTY);
            dryer.dryingProgress = 0;
            dryer.setChanged();
            return;
        }

        if (dryer.dryingProgress < DRYING_PROGRESS_REQUIRED) {
            dryer.dryingProgress++;
            if (dryer.dryingProgress % 20 == 0) {
                dryer.setChanged();
            }
        }
        if (dryer.dryingProgress < DRYING_PROGRESS_REQUIRED || !dryer.hasOutputSpace()) {
            return;
        }

        ItemStack result = getDriedResult(input);
        ItemStack output = dryer.inventory.getItem(OUTPUT_SLOT);
        if (output.isEmpty()) {
            dryer.inventory.setItem(OUTPUT_SLOT, result);
        } else if (ItemStack.isSameItem(output, result) && output.getCount() < output.getMaxStackSize()) {
            output.grow(result.getCount());
            dryer.inventory.setChanged();
        } else {
            return;
        }

        input.shrink(1);
        if (input.isEmpty()) {
            dryer.inventory.setItem(INPUT_SLOT, ItemStack.EMPTY);
        } else {
            dryer.inventory.setChanged();
        }
        dryer.dryingProgress = 0;
        dryer.setChanged();
    }

    private boolean hasOutputSpace() {
        ItemStack output = this.inventory.getItem(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        ItemStack result = getDriedResult(this.inventory.getItem(INPUT_SLOT));
        return ItemStack.isSameItem(output, result) && output.getCount() < output.getMaxStackSize();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.primitiveisolation.plant_dryer");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new PlantDryerMenu(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.inventory.getItems());
        this.dryingProgress = Math.clamp(
                input.getIntOr("DryingProgress", 0),
                0,
                DRYING_PROGRESS_REQUIRED);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.inventory.getItems());
        output.putInt("DryingProgress", this.dryingProgress);
    }
}
