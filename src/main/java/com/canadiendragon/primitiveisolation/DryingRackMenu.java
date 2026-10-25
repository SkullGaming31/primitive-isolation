package com.canadiendragon.primitiveisolation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DryingRackMenu extends AbstractContainerMenu {
    private final DryingRackBlockEntity rack;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public DryingRackMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, getRack(playerInventory, extraData.readBlockPos()));
    }

    public DryingRackMenu(int containerId, Inventory playerInventory, DryingRackBlockEntity rack) {
        super(primitiveisolation.DRYING_RACK_MENU.get(), containerId);
        this.rack = rack;
        SimpleContainer inventory = rack.getInventory();
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), rack.getBlockPos());
        this.data = rack.getContainerData();

        this.addSlot(new Slot(inventory, DryingRackBlockEntity.INPUT_SLOT, 53, 30) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(primitiveisolation.TANNED_HIDE.get());
            }

            @Override
            public boolean mayPickup(Player player) {
                return DryingRackMenu.this.rack.getContainerData().get(0) == 0;
            }
        });
        this.addSlot(new Slot(inventory, DryingRackBlockEntity.OUTPUT_SLOT, 107, 30) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addStandardInventorySlots(playerInventory, 8, 84);
        this.addDataSlots(this.data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, primitiveisolation.DRYING_RACK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved;
        if (slotIndex < DryingRackBlockEntity.SLOT_COUNT) {
            moved = this.moveItemStackTo(stack, DryingRackBlockEntity.SLOT_COUNT, 38, true);
        } else if (stack.is(primitiveisolation.TANNED_HIDE.get())) {
            moved = this.moveItemStackTo(
                    stack,
                    DryingRackBlockEntity.INPUT_SLOT,
                    DryingRackBlockEntity.INPUT_SLOT + 1,
                    false);
            if (!moved) {
                moved = slotIndex < 29
                        ? this.moveItemStackTo(stack, 29, 38, false)
                        : this.moveItemStackTo(stack, 2, 29, false);
            }
        } else if (slotIndex < 29) {
            moved = this.moveItemStackTo(stack, 29, 38, false);
        } else {
            moved = this.moveItemStackTo(stack, 2, 29, false);
        }

        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    public int getDryingProgress() {
        return this.data.get(0);
    }

    public int getDryingProgressRequired() {
        return this.data.get(1);
    }

    private static DryingRackBlockEntity getRack(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof DryingRackBlockEntity rack) {
            return rack;
        }
        throw new IllegalStateException("Drying rack block entity missing at " + pos);
    }
}
