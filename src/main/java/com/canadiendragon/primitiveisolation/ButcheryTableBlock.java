package com.canadiendragon.primitiveisolation;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ButcheryTableBlock extends Block {
    private static final int BUTCHERING_TIME_TICKS = 200;
    private static final double BUTCHERING_RANGE_SQUARED = 25.0;
    private static final Map<UUID, ButcheringSession> ACTIVE_BUTCHERING = new HashMap<>();
    public static final EnumProperty<CarcassType> CARCASS = EnumProperty.create("carcass", CarcassType.class);

    public ButcheryTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CARCASS, CarcassType.EMPTY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CARCASS);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        CarcassType carcassType = state.getValue(CARCASS);
        if (carcassType == CarcassType.EMPTY) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            ItemStack carcass = getCarcassItem(carcassType);
            if (!player.getInventory().add(carcass)) {
                player.drop(carcass, false);
            }
            level.setBlock(pos, state.setValue(CARCASS, CarcassType.EMPTY), 3);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack itemStack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        CarcassType carcassType = state.getValue(CARCASS);
        if (carcassType == CarcassType.EMPTY && itemStack.getItem() instanceof CarcassItem) {
            CarcassType heldType = getCarcassType(itemStack);
            if (heldType == CarcassType.EMPTY) {
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(CARCASS, heldType), 3);
                if (!player.hasInfiniteMaterials()) {
                    itemStack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (carcassType != CarcassType.EMPTY && CarcassItem.hasFlintKnife(player, hand)) {
            ACTIVE_BUTCHERING.put(player.getUUID(), new ButcheringSession(level, pos.immutable(), hand));
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }

        if (carcassType != CarcassType.EMPTY && itemStack.getItem() instanceof CarcassItem) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("message.primitiveisolation.butchery_table.occupied"));
            }
            return InteractionResult.FAIL;
        }

        if (carcassType != CarcassType.EMPTY && !itemStack.isEmpty()) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("message.primitiveisolation.butchery_table.requires_knife"));
            }
            return InteractionResult.FAIL;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    static void tickButchering(Player player) {
        ButcheringSession session = ACTIVE_BUTCHERING.get(player.getUUID());
        if (session == null) {
            return;
        }

        if (!player.isAlive()
                || !player.isUsingItem()
                || player.getUsedItemHand() != session.knifeHand()
                || !CarcassItem.hasFlintKnife(player, session.knifeHand())
                || player.level() != session.level()
                || player.distanceToSqr(Vec3.atCenterOf(session.pos())) > BUTCHERING_RANGE_SQUARED) {
            cancelAndStop(player);
            return;
        }

        HitResult hit = player.pick(5.0, 0.0F, false);
        if (!(hit instanceof BlockHitResult blockHit) || !blockHit.getBlockPos().equals(session.pos())) {
            cancelAndStop(player);
            return;
        }

        BlockState state = session.level().getBlockState(session.pos());
        if (!(state.getBlock() instanceof ButcheryTableBlock)) {
            cancelAndStop(player);
            return;
        }

        CarcassType carcassType = state.getValue(CARCASS);
        if (carcassType == CarcassType.EMPTY) {
            cancelAndStop(player);
            return;
        }

        if (player.getTicksUsingItem() >= BUTCHERING_TIME_TICKS) {
            CarcassItem.process(player, session.knifeHand(), getButcheredDrops(carcassType));
            session.level().setBlock(session.pos(), state.setValue(CARCASS, CarcassType.EMPTY), 3);
            cancelAndStop(player);
        }
    }

    static void cancelButchering(Player player) {
        ACTIVE_BUTCHERING.remove(player.getUUID());
    }

    private static void cancelAndStop(Player player) {
        cancelButchering(player);
        player.stopUsingItem();
    }

    private static CarcassType getCarcassType(ItemStack stack) {
        if (stack.is(primitiveisolation.COW_CARCASS.get())) {
            return CarcassType.COW;
        }
        if (stack.is(primitiveisolation.PIG_CARCASS.get())) {
            return CarcassType.PIG;
        }
        if (stack.is(primitiveisolation.CHICKEN_CARCASS.get())) {
            return CarcassType.CHICKEN;
        }
        return CarcassType.EMPTY;
    }

    private static ItemStack getCarcassItem(CarcassType type) {
        return switch (type) {
            case COW -> primitiveisolation.COW_CARCASS.get().getDefaultInstance();
            case PIG -> primitiveisolation.PIG_CARCASS.get().getDefaultInstance();
            case CHICKEN -> primitiveisolation.CHICKEN_CARCASS.get().getDefaultInstance();
            case EMPTY -> ItemStack.EMPTY;
        };
    }

    private static List<ItemStack> getButcheredDrops(CarcassType type) {
        return switch (type) {
            case COW -> primitiveisolation.cowButcheredDrops();
            case PIG -> primitiveisolation.pigButcheredDrops();
            case CHICKEN -> primitiveisolation.chickenButcheredDrops();
            case EMPTY -> List.of();
        };
    }

    private record ButcheringSession(Level level, BlockPos pos, InteractionHand knifeHand) {
    }

    public enum CarcassType implements StringRepresentable {
        EMPTY("empty"),
        COW("cow"),
        PIG("pig"),
        CHICKEN("chicken");

        private final String name;

        CarcassType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
