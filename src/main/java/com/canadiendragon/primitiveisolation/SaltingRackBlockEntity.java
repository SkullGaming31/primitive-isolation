package com.canadiendragon.primitiveisolation;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.MenuProvider;

public class SaltingRackBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT_COUNT = 3;
    public static final int SALT_SLOT = 3;
    public static final int OUTPUT_SLOT_START = 4;
    public static final int OUTPUT_SLOT_COUNT = 3;
    public static final int SLOT_COUNT = OUTPUT_SLOT_START + OUTPUT_SLOT_COUNT;
    public static final int TICKS_PER_PIECE = 20 * 60;
    private int processTicks;
    private int activeInputSlot = -1;
    private int activeOutputSlot = -1;
    private final SimpleContainer inventory = new SimpleContainer(SLOT_COUNT) {
        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return SaltingRackBlockEntity.this.canPlaceInSlot(slot, stack);
        }

        @Override
        public boolean canTakeItem(Container into, int slot, ItemStack stack) {
            return slot != SaltingRackBlockEntity.this.activeInputSlot;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            SaltingRackBlockEntity.this.setChanged();
        }
    };
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> SaltingRackBlockEntity.this.processTicks;
                case 1 -> SaltingRackBlockEntity.this.activeInputSlot;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                SaltingRackBlockEntity.this.processTicks = value;
            } else if (index == 1) {
                SaltingRackBlockEntity.this.activeInputSlot = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public SaltingRackBlockEntity(BlockPos pos, BlockState state) {
        super(primitiveisolation.SALTING_RACK_BLOCK_ENTITY.get(), pos, state);
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

    public int getActiveInputSlot() {
        return this.activeInputSlot;
    }

    public static boolean isRawFood(ItemStack stack) {
        return saltedFoodFor(stack.getItem()) != null;
    }

    private boolean canPlaceInSlot(int slot, ItemStack stack) {
        if (slot >= 0 && slot < INPUT_SLOT_COUNT) {
            return isRawFood(stack);
        }
        return slot == SALT_SLOT && stack.is(primitiveisolation.SALT.get());
    }

    private int findOutputSlot(Item outputItem) {
        for (int slot = OUTPUT_SLOT_START; slot < SLOT_COUNT; slot++) {
            ItemStack output = this.inventory.getItem(slot);
            if (output.is(outputItem) && output.getCount() < output.getMaxStackSize()) {
                return slot;
            }
        }
        for (int slot = OUTPUT_SLOT_START; slot < SLOT_COUNT; slot++) {
            if (this.inventory.getItem(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private void startNextPiece() {
        ItemStack salt = this.inventory.getItem(SALT_SLOT);
        if (salt.getCount() < 2) {
            return;
        }

        for (int slot = 0; slot < INPUT_SLOT_COUNT; slot++) {
            ItemStack input = this.inventory.getItem(slot);
            Item outputItem = saltedFoodFor(input.getItem());
            if (outputItem == null) {
                continue;
            }

            int outputSlot = this.findOutputSlot(outputItem);
            if (outputSlot < 0) {
                continue;
            }

            salt.shrink(2);
            if (salt.isEmpty()) {
                this.inventory.setItem(SALT_SLOT, ItemStack.EMPTY);
            } else {
                this.inventory.setChanged();
            }
            this.activeInputSlot = slot;
            this.activeOutputSlot = outputSlot;
            this.processTicks = 0;
            this.setChanged();
            return;
        }
    }

    private void finishPiece(Item outputItem) {
        ItemStack output = this.inventory.getItem(this.activeOutputSlot);
        if (!output.isEmpty()
                && (!output.is(outputItem) || output.getCount() >= output.getMaxStackSize())) {
            return;
        }

        if (output.isEmpty()) {
            this.inventory.setItem(this.activeOutputSlot, new ItemStack(outputItem));
        } else {
            output.grow(1);
            this.inventory.setChanged();
        }

        ItemStack input = this.inventory.getItem(this.activeInputSlot);
        input.shrink(1);
        if (input.isEmpty()) {
            this.inventory.setItem(this.activeInputSlot, ItemStack.EMPTY);
        } else {
            this.inventory.setChanged();
        }

        this.activeInputSlot = -1;
        this.activeOutputSlot = -1;
        this.processTicks = 0;
        this.setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SaltingRackBlockEntity rack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        rack.spoilExpiredInputs(serverLevel, pos);

        if (rack.activeInputSlot < 0) {
            rack.startNextPiece();
            return;
        }

        ItemStack input = rack.inventory.getItem(rack.activeInputSlot);
        Item outputItem = saltedFoodFor(input.getItem());
        if (outputItem == null || rack.activeOutputSlot < OUTPUT_SLOT_START || rack.activeOutputSlot >= SLOT_COUNT) {
            rack.activeInputSlot = -1;
            rack.activeOutputSlot = -1;
            rack.processTicks = 0;
            rack.setChanged();
            return;
        }

        if (rack.processTicks < TICKS_PER_PIECE) {
            rack.processTicks++;
        }
        if (rack.processTicks >= TICKS_PER_PIECE) {
            rack.finishPiece(outputItem);
        } else if (rack.processTicks % 20 == 0) {
            rack.setChanged();
        }
    }

    private void spoilExpiredInputs(ServerLevel level, BlockPos pos) {
        for (int slot = 0; slot < INPUT_SLOT_COUNT; slot++) {
            ItemStack input = this.inventory.getItem(slot);
            if (!isRawFood(input)) {
                continue;
            }

            long spoilDuration = primitiveisolation.spoilDurationFor(input.getItem());
            if (spoilDuration <= 0
                    || !PerishableFoodItem.hasSpoiled(input, level, spoilDuration)) {
                continue;
            }

            Containers.dropItemStack(
                    level,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    PerishableFoodItem.getSpoiledStack(input));
            this.inventory.setItem(slot, ItemStack.EMPTY);
            if (this.activeInputSlot == slot) {
                this.activeInputSlot = -1;
                this.activeOutputSlot = -1;
                this.processTicks = 0;
            }
            this.setChanged();
        }
    }

    private static Item saltedFoodFor(Item item) {
        if (item == Items.BEEF || item == primitiveisolation.RAW_BEEF.get()) {
            return primitiveisolation.SALTED_BEEF.get();
        }
        if (item == Items.PORKCHOP || item == primitiveisolation.RAW_PORK.get()) {
            return primitiveisolation.SALTED_PORK.get();
        }
        if (item == Items.CHICKEN || item == primitiveisolation.RAW_CHICKEN.get()) {
            return primitiveisolation.SALTED_CHICKEN.get();
        }
        if (item == Items.COD || item == primitiveisolation.RAW_COD.get()) {
            return primitiveisolation.SALTED_COD.get();
        }
        if (item == Items.SALMON || item == primitiveisolation.RAW_SALMON.get()) {
            return primitiveisolation.SALTED_SALMON.get();
        }
        if (item == Items.TROPICAL_FISH || item == primitiveisolation.RAW_TROPICAL_FISH.get()) {
            return primitiveisolation.SALTED_TROPICAL_FISH.get();
        }
        if (item == Items.PUFFERFISH || item == primitiveisolation.RAW_PUFFERFISH.get()) {
            return primitiveisolation.SALTED_PUFFERFISH.get();
        }
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.primitiveisolation.salting_rack");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SaltingRackMenu(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.inventory.getItems());
        this.processTicks = Math.clamp(input.getIntOr("ProcessTicks", 0), 0, TICKS_PER_PIECE);
        this.activeInputSlot = input.getIntOr("ActiveInputSlot", -1);
        this.activeOutputSlot = input.getIntOr("ActiveOutputSlot", -1);
        if (this.activeInputSlot < 0
                || this.activeInputSlot >= INPUT_SLOT_COUNT
                || this.activeOutputSlot < OUTPUT_SLOT_START
                || this.activeOutputSlot >= SLOT_COUNT
                || saltedFoodFor(this.inventory.getItem(this.activeInputSlot).getItem()) == null) {
            this.activeInputSlot = -1;
            this.activeOutputSlot = -1;
            this.processTicks = 0;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.inventory.getItems());
        output.putInt("ProcessTicks", this.processTicks);
        output.putInt("ActiveInputSlot", this.activeInputSlot);
        output.putInt("ActiveOutputSlot", this.activeOutputSlot);
    }
}
