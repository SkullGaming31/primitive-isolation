package com.canadiendragon.primitiveisolation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BasketMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;

    public BasketMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBasket(playerInventory, extraData.readBlockPos()));
    }

    public BasketMenu(int containerId, Inventory playerInventory, BasketBlockEntity basket) {
        super(primitiveisolation.BASKET_MENU.get(), containerId);
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), basket.getBlockPos());

        for (int slot = 0; slot < BasketBlockEntity.SLOT_COUNT; slot++) {
            int x = slot < 4 ? 44 + slot * 18 : 53 + (slot - 4) * 18;
            int y = slot < 4 ? 18 : 36;
            this.addSlot(new Slot(basket, slot, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return BasketBlockEntity.isStorable(stack);
                }
            });
        }

        this.addStandardInventorySlots(playerInventory, 8, 84);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, primitiveisolation.BASKET.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int playerInventoryStart = BasketBlockEntity.SLOT_COUNT;
        int playerMainInventoryEnd = playerInventoryStart + 27;
        int menuEnd = playerInventoryStart + 36;
        boolean moved;

        if (slotIndex < playerInventoryStart) {
            moved = this.moveItemStackTo(stack, playerInventoryStart, menuEnd, true);
        } else {
            moved = BasketBlockEntity.isStorable(stack)
                    && this.moveItemStackTo(stack, 0, BasketBlockEntity.SLOT_COUNT, false);
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

    private static BasketBlockEntity getBasket(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof BasketBlockEntity basket) {
            return basket;
        }
        throw new IllegalStateException("Basket block entity missing at " + pos);
    }
}
