package com.canadiendragon.primitiveisolation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BasketBlockEntity extends BaseContainerBlockEntity {
    public static final int SLOT_COUNT = 7;
    private static final TagKey<Item> GATHERABLE_ITEMS = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(primitiveisolation.MODID, "basket_gatherables"));
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    public BasketBlockEntity(BlockPos pos, BlockState state) {
        super(primitiveisolation.BASKET_BLOCK_ENTITY.get(), pos, state);
    }

    public static boolean isStorable(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(GATHERABLE_ITEMS) || stack.has(DataComponents.FOOD));
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot >= 0 && slot < SLOT_COUNT && isStorable(stack);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            this.items.set(slot, slot < items.size() ? items.get(slot) : ItemStack.EMPTY);
        }
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            this.items.set(slot, ItemStack.EMPTY);
        }
        this.setChanged();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        // The loot table copies these contents into the dropped basket item.
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.primitiveisolation.basket");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new BasketMenu(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
    }
}
