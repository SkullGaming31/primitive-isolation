package com.canadiendragon.primitiveisolation;

import java.util.HashMap;
import java.util.Map;

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
import net.minecraft.world.item.Items;

public class KnappingStationMenu extends AbstractContainerMenu {
    private static final String[] AXE_HEAD_PATTERN = {"FF ", "FS ", "   "};
    private static final String[] PICKAXE_HEAD_PATTERN = {"FFF", " S ", "   "};
    private static final String[] HOE_HEAD_PATTERN = {"FF ", "S  ", "   "};
    private static final String[] SHOVEL_HEAD_PATTERN = {"F  ", "S  ", "   "};
    private static final String[] KNIFE_HEAD_PATTERN = {"FS ", "   ", "   "};

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
        RecipeMatch match = this.matchPattern(AXE_HEAD_PATTERN, primitiveisolation.FLINT_AXE_HEAD.get());
        if (match == null) {
            match = this.matchPattern(PICKAXE_HEAD_PATTERN, primitiveisolation.FLINT_PICKAXE_HEAD.get());
        }
        if (match == null) {
            match = this.matchPattern(HOE_HEAD_PATTERN, primitiveisolation.FLINT_HOE_HEAD.get());
        }
        if (match == null) {
            match = this.matchPattern(SHOVEL_HEAD_PATTERN, primitiveisolation.FLINT_SHOVEL_HEAD.get());
        }
        if (match == null) {
            match = this.matchPattern(KNIFE_HEAD_PATTERN, primitiveisolation.FLINT_KNIFE_HEAD.get());
        }
        if (match == null) {
            match = this.matchShapeless(
                    primitiveisolation.FLINT_AXE.get(),
                    primitiveisolation.FLINT_AXE_HEAD.get(),
                    Items.STICK,
                    Items.STICK);
        }
        if (match == null) {
            match = this.matchShapeless(
                    primitiveisolation.FLINT_PICKAXE.get(),
                    primitiveisolation.FLINT_PICKAXE_HEAD.get(),
                    Items.STICK,
                    Items.STICK);
        }
        if (match == null) {
            match = this.matchShapeless(
                    primitiveisolation.FLINT_HOE.get(),
                    primitiveisolation.FLINT_HOE_HEAD.get(),
                    Items.STICK,
                    Items.STICK);
        }
        if (match == null) {
            match = this.matchShapeless(
                    primitiveisolation.FLINT_SHOVEL.get(),
                    primitiveisolation.FLINT_SHOVEL_HEAD.get(),
                    Items.STICK,
                    Items.STICK);
        }
        if (match == null) {
            match = this.matchShapeless(
                    primitiveisolation.FLINT_KNIFE.get(),
                    primitiveisolation.FLINT_KNIFE_HEAD.get(),
                    Items.STICK);
        }
        return match;
    }

    private RecipeMatch matchPattern(String[] pattern, Item result) {
        int[] consume = new int[9];
        for (int slot = 0; slot < 9; slot++) {
            char symbol = pattern[slot / 3].charAt(slot % 3);
            ItemStack stack = this.input.getItem(slot);
            if (symbol == ' ') {
                if (!stack.isEmpty()) {
                    return null;
                }
                continue;
            }

            Item expected = symbol == 'F' ? Items.FLINT : Items.STRING;
            if (!stack.is(expected)) {
                return null;
            }
            consume[slot] = 1;
        }
        return new RecipeMatch(result, consume);
    }

    private RecipeMatch matchShapeless(Item result, Item... ingredients) {
        Map<Item, Integer> required = new HashMap<>();
        for (Item ingredient : ingredients) {
            required.merge(ingredient, 1, Integer::sum);
        }

        int[] consume = new int[9];
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = this.input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            Integer needed = required.get(stack.getItem());
            if (needed == null) {
                return null;
            }
            int used = Math.min(stack.getCount(), needed);
            consume[slot] = used;
            required.put(stack.getItem(), needed - used);
        }

        if (required.values().stream().anyMatch(count -> count > 0)) {
            return null;
        }
        return new RecipeMatch(result, consume);
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
