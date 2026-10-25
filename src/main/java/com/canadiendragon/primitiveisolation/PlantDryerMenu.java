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

public class PlantDryerMenu extends AbstractContainerMenu {
    private final PlantDryerBlockEntity dryer;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public PlantDryerMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, getDryer(playerInventory, extraData.readBlockPos()));
    }

    public PlantDryerMenu(int containerId, Inventory playerInventory, PlantDryerBlockEntity dryer) {
        super(primitiveisolation.PLANT_DRYER_MENU.get(), containerId);
        this.dryer = dryer;
        SimpleContainer inventory = dryer.getInventory();
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), dryer.getBlockPos());
        this.data = dryer.getContainerData();

        this.addSlot(new Slot(inventory, PlantDryerBlockEntity.INPUT_SLOT, 53, 30) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return PlantDryerBlockEntity.isDryable(stack);
            }

            @Override
            public boolean mayPickup(Player player) {
                return PlantDryerMenu.this.dryer.getContainerData().get(0) == 0;
            }
        });
        this.addSlot(new Slot(inventory, PlantDryerBlockEntity.OUTPUT_SLOT, 107, 30) {
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
        return stillValid(this.access, player, primitiveisolation.PLANT_DRYER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        if (slotIndex == PlantDryerBlockEntity.INPUT_SLOT && this.data.get(0) != 0) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved;
        if (slotIndex < PlantDryerBlockEntity.SLOT_COUNT) {
            moved = this.moveItemStackTo(stack, PlantDryerBlockEntity.SLOT_COUNT, 38, true);
        } else if (PlantDryerBlockEntity.isDryable(stack)) {
            moved = this.moveItemStackTo(
                    stack,
                    PlantDryerBlockEntity.INPUT_SLOT,
                    PlantDryerBlockEntity.INPUT_SLOT + 1,
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

    private static PlantDryerBlockEntity getDryer(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof PlantDryerBlockEntity dryer) {
            return dryer;
        }
        throw new IllegalStateException("Plant dryer block entity missing at " + pos);
    }
}
