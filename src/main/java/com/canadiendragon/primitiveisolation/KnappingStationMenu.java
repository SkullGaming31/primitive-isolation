package com.canadiendragon.primitiveisolation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class KnappingStationMenu extends AbstractContainerMenu {
    private final Inventory playerInventory;
    private final ContainerLevelAccess access;
    private final SimpleContainer input = new SimpleContainer(9);
    private final SimpleContainer output = new SimpleContainer(1);
    private RecipeMatch currentMatch;

    public KnappingStationMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(
                containerId,
                inventory,
                ContainerLevelAccess.create(inventory.player.level(), extraData.readBlockPos()));
    }

    public KnappingStationMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(primitiveisolation.KNAPPING_STATION_MENU.get(), containerId);
        this.playerInventory = inventory;
        this.access = access;

        this.addSlot(new Slot(this.output, 0, 124, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                KnappingStationMenu.this.consumeRecipe();
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int inputSlot = column + row * 3;
                this.addSlot(new Slot(this.input, inputSlot, 30 + column * 18, 17 + row * 18) {
                    @Override
                    public void setChanged() {
                        super.setChanged();
                        KnappingStationMenu.this.updateResult();
                    }
                });
            }
        }

        this.addStandardInventorySlots(inventory, 8, 84);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, primitiveisolation.KNAPPING_STATION.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.input));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (slotIndex == 0) {
            if (!this.moveItemStackTo(stack, 10, 46, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        } else if (slotIndex < 10) {
            if (!this.moveItemStackTo(stack, 10, 46, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 1, 10, false)) {
            if (slotIndex < 37) {
                if (!this.moveItemStackTo(stack, 37, 46, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 10, 37, false)) {
                return ItemStack.EMPTY;
            }
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

    private void updateResult() {
        if (this.playerInventory.player.level().isClientSide()) {
            return;
        }

        this.currentMatch = this.findMatch();
        this.output.setItem(0, this.currentMatch == null ? ItemStack.EMPTY : new ItemStack(this.currentMatch.output));
    }

    private RecipeMatch findMatch() {
        for (KnappingRecipes.PatternRecipe recipe : KnappingRecipes.HEADS) {
            RecipeMatch match = this.matchPattern(recipe);
            if (match != null) {
                return match;
            }
        }
        for (KnappingRecipes.PatternRecipe recipe : KnappingRecipes.ASSEMBLIES) {
            RecipeMatch match = this.matchPattern(recipe);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    private RecipeMatch matchPattern(KnappingRecipes.PatternRecipe recipe) {
        int[] consume = new int[9];
        for (int slot = 0; slot < 9; slot++) {
            char symbol = recipe.pattern().get(slot / 3).charAt(slot % 3);
            ItemStack stack = this.input.getItem(slot);
            if (symbol == ' ') {
                if (!stack.isEmpty()) {
                    return null;
                }
                continue;
            }

            Item expected = recipe.key().get(symbol);
            if (expected == null || !stack.is(expected)) {
                return null;
            }
            consume[slot] = 1;
        }
        return new RecipeMatch(recipe.output(), consume);
    }

    private void consumeRecipe() {
        if (this.currentMatch == null) {
            return;
        }
        for (int slot = 0; slot < this.currentMatch.consume.length; slot++) {
            int amount = this.currentMatch.consume[slot];
            if (amount > 0) {
                ItemStack stack = this.input.getItem(slot);
                if (stack.getCount() <= amount) {
                    this.input.setItem(slot, ItemStack.EMPTY);
                } else {
                    stack.shrink(amount);
                }
            }
        }
        this.updateResult();
    }

    private record RecipeMatch(Item output, int[] consume) {
    }
}
