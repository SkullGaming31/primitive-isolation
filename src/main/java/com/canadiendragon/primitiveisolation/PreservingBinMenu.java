package com.canadiendragon.primitiveisolation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PreservingBinMenu extends AbstractContainerMenu {
    private final PreservingBinBlockEntity bin;
    private final SimpleContainer inventory;
    private final ContainerLevelAccess access;

    public PreservingBinMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBin(playerInventory, extraData.readBlockPos()));
    }

    public PreservingBinMenu(int containerId, Inventory playerInventory, PreservingBinBlockEntity bin) {
        super(primitiveisolation.PRESERVING_BIN_MENU.get(), containerId);
        this.bin = bin;
        this.inventory = bin.getInventory();
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), bin.getBlockPos());

        for (int slot = 0; slot < PreservingBinBlockEntity.STORAGE_SLOT_COUNT; slot++) {
            this.addSlot(new Slot(this.inventory, slot, 8 + slot % 9 * 18, 18 + slot / 9 * 18));
        }
        this.addSlot(new Slot(
                this.inventory,
                PreservingBinBlockEntity.ICE_SLOT,
                8,
                78) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.ICE);
            }
        });

        this.addStandardInventorySlots(playerInventory, 8, 110);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, primitiveisolation.PRESERVING_BIN.get());
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
        int playerInventoryStart = PreservingBinBlockEntity.SLOT_COUNT;
        int playerMainInventoryEnd = playerInventoryStart + 27;
        int menuEnd = playerInventoryStart + 36;

        if (slotIndex < playerInventoryStart) {
            moved = this.moveItemStackTo(stack, playerInventoryStart, menuEnd, true);
        } else if (stack.is(Items.ICE)) {
            moved = this.moveItemStackTo(
                    stack,
                    PreservingBinBlockEntity.ICE_SLOT,
                    PreservingBinBlockEntity.ICE_SLOT + 1,
                    false);
            if (!moved) {
                moved = this.moveItemStackTo(stack, 0, PreservingBinBlockEntity.STORAGE_SLOT_COUNT, false);
            }
        } else {
            moved = this.moveItemStackTo(stack, 0, PreservingBinBlockEntity.STORAGE_SLOT_COUNT, false);
            if (!moved) {
                moved = slotIndex < playerMainInventoryEnd
                        ? this.moveItemStackTo(stack, playerMainInventoryEnd, menuEnd, false)
                        : this.moveItemStackTo(stack, playerInventoryStart, playerMainInventoryEnd, false);
            }
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

    private static PreservingBinBlockEntity getBin(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof PreservingBinBlockEntity bin) {
            return bin;
        }
        throw new IllegalStateException("Preserving bin block entity missing at " + pos);
    }
}
