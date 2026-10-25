package com.canadiendragon.primitiveisolation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.MenuProvider;

public class DryingRackBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int SLOT_COUNT = 2;
    public static final int DRYING_PROGRESS_REQUIRED = 24_000;

    private int dryingProgress;
    private int dryingRate = 1;
    private final SimpleContainer inventory = new SimpleContainer(SLOT_COUNT) {
        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return slot == INPUT_SLOT && stack.is(primitiveisolation.TANNED_HIDE.get());
        }

        @Override
        public boolean canTakeItem(Container into, int slot, ItemStack stack) {
            return slot != INPUT_SLOT || DryingRackBlockEntity.this.dryingProgress == 0;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            DryingRackBlockEntity.this.setChanged();
        }
    };
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> DryingRackBlockEntity.this.dryingProgress;
                case 1 -> DRYING_PROGRESS_REQUIRED;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                DryingRackBlockEntity.this.dryingProgress =
                        Math.clamp(value, 0, DRYING_PROGRESS_REQUIRED);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(primitiveisolation.DRYING_RACK_BLOCK_ENTITY.get(), pos, state);
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity rack) {
        if (!(level instanceof ServerLevel)) {
            return;
        }

        ItemStack input = rack.inventory.getItem(INPUT_SLOT);
        if (input.isEmpty()) {
            if (rack.dryingProgress != 0) {
                rack.dryingProgress = 0;
                rack.setChanged();
            }
            rack.updateStage(rack.inventory.getItem(OUTPUT_SLOT).isEmpty()
                    ? DryingRackBlock.DryingStage.IDLE
                    : DryingRackBlock.DryingStage.READY);
            return;
        }
        if (!input.is(primitiveisolation.TANNED_HIDE.get())) {
            rack.inventory.setItem(INPUT_SLOT, ItemStack.EMPTY);
            rack.dryingProgress = 0;
            rack.updateStage(DryingRackBlock.DryingStage.IDLE);
            rack.setChanged();
            return;
        }

        boolean exposed = level.canSeeSky(pos.above());
        boolean rainingOnRack = exposed && level.isRainingAt(pos.above());
        int rate = exposed ? 2 : 1;
        if (hasNearbyLitCampfire(level, pos)) {
            rate *= 2;
        }
        rack.dryingRate = rate;

        if (!rainingOnRack && rack.dryingProgress < DRYING_PROGRESS_REQUIRED) {
            rack.dryingProgress = Math.min(
                    DRYING_PROGRESS_REQUIRED,
                    rack.dryingProgress + rate);
            if (rack.dryingProgress % 20 < rate) {
                rack.setChanged();
            }
        }

        if (rack.dryingProgress >= DRYING_PROGRESS_REQUIRED && rack.hasOutputSpace()) {
            ItemStack output = rack.inventory.getItem(OUTPUT_SLOT);
            if (output.isEmpty()) {
                rack.inventory.setItem(OUTPUT_SLOT, new ItemStack(Items.LEATHER));
            } else {
                output.grow(1);
                rack.inventory.setChanged();
            }
            input.shrink(1);
            if (input.isEmpty()) {
                rack.inventory.setItem(INPUT_SLOT, ItemStack.EMPTY);
            } else {
                rack.inventory.setChanged();
            }
            rack.dryingProgress = 0;
            rack.setChanged();
        }

        rack.updateStage(rack.getProgressStage());
    }

    private static boolean hasNearbyLitCampfire(Level level, BlockPos pos) {
        MutableBlockPos cursor = new MutableBlockPos();
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    if (x * x + y * y + z * z > 9) {
                        continue;
                    }
                    cursor.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    BlockState nearby = level.getBlockState(cursor);
                    if (nearby.is(net.minecraft.tags.BlockTags.CAMPFIRES)
                            && nearby.hasProperty(CampfireBlock.LIT)
                            && nearby.getValue(CampfireBlock.LIT)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasOutputSpace() {
        ItemStack output = this.inventory.getItem(OUTPUT_SLOT);
        return output.isEmpty() || (output.is(Items.LEATHER) && output.getCount() < output.getMaxStackSize());
    }

    private DryingRackBlock.DryingStage getProgressStage() {
        if (this.dryingProgress * 3 < DRYING_PROGRESS_REQUIRED) {
            return DryingRackBlock.DryingStage.WET;
        }
        if (this.dryingProgress * 3 < DRYING_PROGRESS_REQUIRED * 2) {
            return DryingRackBlock.DryingStage.PARTIALLY_DRY;
        }
        return DryingRackBlock.DryingStage.NEARLY_DRY;
    }

    private void updateStage(DryingRackBlock.DryingStage stage) {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        BlockState current = this.getBlockState();
        if (current.getBlock() instanceof DryingRackBlock && current.getValue(DryingRackBlock.STAGE) != stage) {
            this.level.setBlock(
                    this.worldPosition,
                    current.setValue(DryingRackBlock.STAGE, stage),
                    Block.UPDATE_ALL);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.primitiveisolation.drying_rack");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DryingRackMenu(containerId, inventory, this);
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
