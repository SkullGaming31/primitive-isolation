package com.canadiendragon.primitiveisolation;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WildPlantBlock extends BushBlock {
    private final MapCodec<BushBlock> codec;
    private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 13.0);
    private static final int WILD_GRAIN_SEED_DROP_CHANCE = 3;

    private final Species species;

    public WildPlantBlock(Species species, BlockBehaviour.Properties properties) {
        super(properties);
        this.species = species;
        this.codec = BlockBehaviour.simpleCodec(
                blockProperties -> (BushBlock) new WildPlantBlock(species, blockProperties));
    }

    @Override
    public MapCodec<BushBlock> codec() {
        return this.codec;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    public boolean isBonemealSuccess(Level level, net.minecraft.util.RandomSource random, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    public void performBonemeal(ServerLevel level, net.minecraft.util.RandomSource random, BlockPos pos, BlockState state) {
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult) {
        return this.interact(level, pos, player, player.getMainHandItem(), InteractionHand.MAIN_HAND);
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
        return this.interact(level, pos, player, itemStack, hand);
    }

    public Species getSpecies() {
        return this.species;
    }

    private InteractionResult interact(Level level, BlockPos pos, Player player, ItemStack heldItem, InteractionHand hand) {
        if (heldItem.is(primitiveisolation.FIELD_GUIDE.get())) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            FieldGuideItem.identify(heldItem, this.species, player);
            return InteractionResult.SUCCESS_SERVER;
        }

        if (this.species.requiresDiggingStick() && !heldItem.is(primitiveisolation.DIGGING_STICK.get())) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.primitiveisolation.need_digging_stick"));
            }
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack harvest = new ItemStack(this.getHarvestItem());
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        popResource(level, pos, harvest);
        if (this.species == Species.WILD_GRAIN
                && level.getRandom().nextInt(WILD_GRAIN_SEED_DROP_CHANCE) == 0) {
            popResource(level, pos, new ItemStack(primitiveisolation.WILD_BARLEY.get()));
        }
        if (this.species.requiresDiggingStick()) {
            heldItem.hurtAndBreak(1, player, hand);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    private Item getHarvestItem() {
        return switch (this.species) {
            case WILD_GRAIN -> primitiveisolation.BARLEY_GRAIN.get();
            case EDIBLE_ROOT -> primitiveisolation.WILD_TURNIP.get();
            case BERRY -> primitiveisolation.WILD_BERRY.get();
            case MEDICINAL_HERB -> primitiveisolation.YARROW.get();
            case POISONOUS_HERB -> primitiveisolation.BITTERLEAF.get();
        };
    }

    public enum Species {
        WILD_GRAIN("wild_barley", false),
        EDIBLE_ROOT("wild_turnip", true),
        BERRY("wild_berry", false),
        MEDICINAL_HERB("yarrow", false),
        POISONOUS_HERB("bitterleaf", false);

        private final String id;
        private final boolean requiresDiggingStick;

        Species(String id, boolean requiresDiggingStick) {
            this.id = id;
            this.requiresDiggingStick = requiresDiggingStick;
        }

        public String id() {
            return this.id;
        }

        public boolean requiresDiggingStick() {
            return this.requiresDiggingStick;
        }

        public String guideKey(String field) {
            return "item.primitiveisolation.field_guide.entry." + this.id + "." + field;
        }

        public String nameKey() {
            return "block.primitiveisolation." + this.id + "_plant";
        }
    }
}
