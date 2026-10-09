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

public class SaltingRackMenu extends AbstractContainerMenu {
    private final SaltingRackBlockEntity rack;
    private final SimpleContainer inventory;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public SaltingRackMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, getRack(playerInventory, extraData.readBlockPos()));
    }

    public SaltingRackMenu(int containerId, Inventory playerInventory, SaltingRackBlockEntity rack) {
        super(primitiveisolation.SALTING_RACK_MENU.get(), containerId);
        this.rack = rack;
        this.inventory = rack.getInventory();
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), rack.getBlockPos());
        this.data = rack.getContainerData();

        for (int inputSlot = 0; inputSlot < SaltingRackBlockEntity.INPUT_SLOT_COUNT; inputSlot++) {
            int slotIndex = inputSlot;
            this.addSlot(new Slot(this.inventory, inputSlot, 30 + inputSlot * 18, 17) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return SaltingRackBlockEntity.isRawFood(stack);
                }

                @Override
                public boolean mayPickup(Player player) {
                    return SaltingRackMenu.this.rack.getActiveInputSlot() != slotIndex;
                }
            });
        }

        this.addSlot(new Slot(this.inventory, SaltingRackBlockEntity.SALT_SLOT, 48, 42) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(primitiveisolation.SALT.get());
            }
        });

        for (int outputIndex = 0; outputIndex < SaltingRackBlockEntity.OUTPUT_SLOT_COUNT; outputIndex++) {
            this.addSlot(new Slot(
                    this.inventory,
                    SaltingRackBlockEntity.OUTPUT_SLOT_START + outputIndex,
                    30 + outputIndex * 18,
                    66) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }

        this.addStandardInventorySlots(playerInventory, 8, 96);
        this.addDataSlots(this.data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, primitiveisolation.SALTING_RACK.get());
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

        if (slotIndex < SaltingRackBlockEntity.SLOT_COUNT) {
            moved = this.moveItemStackTo(stack, SaltingRackBlockEntity.SLOT_COUNT, 43, true);
        } else if (SaltingRackBlockEntity.isRawFood(stack)) {
            moved = this.moveItemStackTo(stack, 0, SaltingRackBlockEntity.INPUT_SLOT_COUNT, false);
            if (!moved) {
                moved = slotIndex < 34
                        ? this.moveItemStackTo(stack, 34, 43, false)
                        : this.moveItemStackTo(stack, 7, 34, false);
            }
        } else if (stack.is(primitiveisolation.SALT.get())) {
            moved = this.moveItemStackTo(
                    stack,
                    SaltingRackBlockEntity.SALT_SLOT,
                    SaltingRackBlockEntity.SALT_SLOT + 1,
                    false);
            if (!moved) {
                moved = slotIndex < 34
                        ? this.moveItemStackTo(stack, 34, 43, false)
                        : this.moveItemStackTo(stack, 7, 34, false);
            }
        } else if (slotIndex < 34) {
            moved = this.moveItemStackTo(stack, 34, 43, false);
        } else {
            moved = this.moveItemStackTo(stack, 7, 34, false);
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

    public int getProcessTicks() {
        return this.data.get(0);
    }

    private static SaltingRackBlockEntity getRack(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof SaltingRackBlockEntity rack) {
            return rack;
        }
        throw new IllegalStateException("Salting rack block entity missing at " + pos);
    }
}
