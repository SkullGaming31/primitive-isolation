package com.canadiendragon.primitiveisolation;

import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.ModifyRecipeJsonsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(primitiveisolation.MODID)
public class primitiveisolation {
    private final Map<ServerLevel, Set<BlockPos>> trackedCampfires = new IdentityHashMap<>();
    private final Map<Holder<WorldClock>, Float> sleepingMatOriginalClockRates = new HashMap<>();

    // Define mod id in a common place for everything to reference
    public static final String MODID = "primitiveisolation";
    private static final TagKey<Item> FLINT_TOOL_REPAIR_ITEMS = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(MODID, "flint_tool_repairs"));
    private static final ToolMaterial FLINT_TOOL_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            ToolMaterial.WOOD.durability() / 2,
            ToolMaterial.STONE.speed(),
            ToolMaterial.STONE.attackDamageBonus(),
            ToolMaterial.STONE.enchantmentValue(),
            FLINT_TOOL_REPAIR_ITEMS);
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "primitiveisolation" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "primitiveisolation" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "primitiveisolation" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);
    public static final DeferredHolder<
            MapCodec<? extends IGlobalLootModifier>,
            MapCodec<PerishableRawFoodLootModifier>> PERISHABLE_RAW_FOOD_LOOT_MODIFIER =
            GLOBAL_LOOT_MODIFIER_SERIALIZERS.register(
                    "perishable_raw_food",
                    () -> PerishableRawFoodLootModifier.CODEC);
    public static final DeferredHolder<EntityType<?>, EntityType<CowCorpse>> COW_CORPSE = ENTITY_TYPES.register(
            "cow_corpse",
            id -> EntityType.Builder.of(CowCorpse::new, MobCategory.MISC)
                    .sized(0.9F, 1.4F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .noLootTable()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<Deer>> DEER = ENTITY_TYPES.register(
            "deer",
            id -> EntityType.Builder.of(Deer::new, MobCategory.CREATURE)
                    .sized(0.9F, 1.5F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .noLootTable()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<Piranha>> PIRANHA = ENTITY_TYPES.register(
            "piranha",
            id -> EntityType.Builder.of(Piranha::new, MobCategory.WATER_CREATURE)
                    .sized(0.55F, 0.4F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .noLootTable()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<Shark>> SHARK = ENTITY_TYPES.register(
            "shark",
            id -> EntityType.Builder.of(Shark::new, MobCategory.WATER_CREATURE)
                    .sized(2.0F, 1.3F)
                    .clientTrackingRange(12)
                    .updateInterval(3)
                    .noLootTable()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<Moose>> MOOSE = ENTITY_TYPES.register(
            "moose",
            id -> EntityType.Builder.of(Moose::new, MobCategory.CREATURE)
                    .sized(2.4F, 3.4F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .noLootTable()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<DeerCorpse>> DEER_CORPSE = ENTITY_TYPES.register(
            "deer_corpse",
            id -> EntityType.Builder.of(DeerCorpse::new, MobCategory.MISC)
                    .sized(0.9F, 1.5F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .noLootTable()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<MooseCorpse>> MOOSE_CORPSE = ENTITY_TYPES.register(
            "moose_corpse",
            id -> EntityType.Builder.of(MooseCorpse::new, MobCategory.MISC)
                    .sized(2.4F, 3.4F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .noLootTable()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<PigCorpse>> PIG_CORPSE = ENTITY_TYPES.register(
            "pig_corpse",
            id -> EntityType.Builder.of(PigCorpse::new, MobCategory.MISC)
                    .sized(1.0F, 0.75F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .noLootTable()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<ChickenCorpse>> CHICKEN_CORPSE = ENTITY_TYPES.register(
            "chicken_corpse",
            id -> EntityType.Builder.of(ChickenCorpse::new, MobCategory.MISC)
                    .sized(0.8F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .noLootTable()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<MooshroomCorpse>> MOOSHROOM_CORPSE = ENTITY_TYPES.register(
            "mooshroom_corpse",
            id -> EntityType.Builder.of(MooshroomCorpse::new, MobCategory.MISC)
                    .sized(0.9F, 1.4F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .noLootTable()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<SheepCorpse>> SHEEP_CORPSE = ENTITY_TYPES.register(
            "sheep_corpse",
            id -> EntityType.Builder.of(SheepCorpse::new, MobCategory.MISC)
                    .sized(0.9F, 1.3F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .noLootTable()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<RabbitCorpse>> RABBIT_CORPSE = ENTITY_TYPES.register(
            "rabbit_corpse",
            id -> EntityType.Builder.of(RabbitCorpse::new, MobCategory.MISC)
                    .sized(0.4F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .noLootTable()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<PolarBearCorpse>> POLAR_BEAR_CORPSE = ENTITY_TYPES.register(
            "polar_bear_corpse",
            id -> EntityType.Builder.of(PolarBearCorpse::new, MobCategory.MISC)
                    .sized(1.4F, 1.4F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .noLootTable()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<HoglinCorpse>> HOGLIN_CORPSE = ENTITY_TYPES.register(
            "hoglin_corpse",
            id -> EntityType.Builder.of(HoglinCorpse::new, MobCategory.MISC)
                    .sized(1.4F, 1.4F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .noLootTable()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<SpiderWebProjectile>> SPIDER_WEB_PROJECTILE = ENTITY_TYPES.register(
            "spider_web_projectile",
            id -> EntityType.Builder.of(SpiderWebProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .noLootTable()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<SquidInkProjectile>> SQUID_INK_PROJECTILE = ENTITY_TYPES.register(
            "squid_ink_projectile",
            id -> EntityType.Builder.of(SquidInkProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .noLootTable()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<MenuType<?>, MenuType<KnappingStationMenu>> KNAPPING_STATION_MENU = MENU_TYPES.register(
            "knapping_station",
            () -> IMenuTypeExtension.create(KnappingStationMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<SaltingRackMenu>> SALTING_RACK_MENU = MENU_TYPES.register(
            "salting_rack",
            () -> IMenuTypeExtension.create(SaltingRackMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<DryingRackMenu>> DRYING_RACK_MENU = MENU_TYPES.register(
            "drying_rack",
            () -> IMenuTypeExtension.create(DryingRackMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<PlantDryerMenu>> PLANT_DRYER_MENU = MENU_TYPES.register(
            "plant_dryer",
            () -> IMenuTypeExtension.create(PlantDryerMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<PreservingBinMenu>> PRESERVING_BIN_MENU = MENU_TYPES.register(
            "preserving_bin",
            () -> IMenuTypeExtension.create(PreservingBinMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<BasketMenu>> BASKET_MENU = MENU_TYPES.register(
            "basket",
            () -> IMenuTypeExtension.create(BasketMenu::new));

    public static final DeferredBlock<Block> KNAPPING_STATION = BLOCKS.registerBlock(
            "knapping_station",
            KnappingStationBlock::new,
            properties -> properties.mapColor(MapColor.WOOD).strength(2.5F).sound(net.minecraft.world.level.block.SoundType.WOOD));
    public static final DeferredItem<BlockItem> KNAPPING_STATION_ITEM = ITEMS.registerSimpleBlockItem("knapping_station", KNAPPING_STATION);
    public static final DeferredBlock<Block> BUTCHERY_TABLE = BLOCKS.registerBlock(
            "butchery_table",
            ButcheryTableBlock::new,
            properties -> properties.mapColor(MapColor.WOOD).strength(2.5F).sound(net.minecraft.world.level.block.SoundType.WOOD));
    public static final DeferredItem<BlockItem> BUTCHERY_TABLE_ITEM = ITEMS.registerSimpleBlockItem("butchery_table", BUTCHERY_TABLE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ButcheryTableBlockEntity>> BUTCHERY_TABLE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("butchery_table", () -> new BlockEntityType<>(
                    ButcheryTableBlockEntity::new,
                    BUTCHERY_TABLE.get()));
    public static final DeferredBlock<Block> SALT_DEPOSIT = BLOCKS.registerSimpleBlock(
            "salt_deposit",
            properties -> properties.mapColor(MapColor.SNOW).strength(0.6F).sound(net.minecraft.world.level.block.SoundType.GRAVEL));
    public static final DeferredItem<BlockItem> SALT_DEPOSIT_ITEM = ITEMS.registerSimpleBlockItem("salt_deposit", SALT_DEPOSIT);
    public static final DeferredBlock<CharcoalPitBlock> CHARCOAL_PIT = BLOCKS.registerBlock(
            "charcoal_pit",
            CharcoalPitBlock::new,
            properties -> properties
                    .mapColor(MapColor.DIRT)
                    .strength(0.5F)
                    .sound(net.minecraft.world.level.block.SoundType.GRAVEL)
                    .lightLevel(state -> state.getValue(CharcoalPitBlock.ACTIVE) ? 15 : 0));
    public static final DeferredItem<BlockItem> CHARCOAL_PIT_ITEM = ITEMS.registerSimpleBlockItem("charcoal_pit", CHARCOAL_PIT);
    public static final DeferredBlock<ChunkAnchorBlock> CHUNK_ANCHOR = BLOCKS.registerBlock(
            "chunk_anchor",
            ChunkAnchorBlock::new,
            properties -> properties
                    .mapColor(MapColor.METAL)
                    .strength(5.0F, 6.0F)
                    .sound(net.minecraft.world.level.block.SoundType.METAL));
    public static final DeferredItem<BlockItem> CHUNK_ANCHOR_ITEM = ITEMS.registerSimpleBlockItem("chunk_anchor", CHUNK_ANCHOR);
    public static final DeferredBlock<SaltingRackBlock> SALTING_RACK = BLOCKS.registerBlock(
            "salting_rack",
            SaltingRackBlock::new,
            properties -> properties
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(net.minecraft.world.level.block.SoundType.WOOD));
    public static final DeferredItem<BlockItem> SALTING_RACK_ITEM = ITEMS.registerSimpleBlockItem("salting_rack", SALTING_RACK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SaltingRackBlockEntity>> SALTING_RACK_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("salting_rack", () -> new BlockEntityType<>(
                    SaltingRackBlockEntity::new,
                    SALTING_RACK.get()));
    public static final DeferredBlock<DryingRackBlock> DRYING_RACK = BLOCKS.registerBlock(
            "drying_rack",
            DryingRackBlock::new,
            properties -> properties
                    .mapColor(MapColor.WOOD)
                    .noOcclusion()
                    .strength(2.5F)
                    .sound(net.minecraft.world.level.block.SoundType.WOOD));
    public static final DeferredItem<BlockItem> DRYING_RACK_ITEM = ITEMS.registerSimpleBlockItem(
            "drying_rack",
            DRYING_RACK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DryingRackBlockEntity>> DRYING_RACK_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("drying_rack", () -> new BlockEntityType<>(
                    DryingRackBlockEntity::new,
                    DRYING_RACK.get()));
    public static final DeferredBlock<PlantDryerBlock> PLANT_DRYER = BLOCKS.registerBlock(
            "plant_dryer",
            PlantDryerBlock::new,
            properties -> properties
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(net.minecraft.world.level.block.SoundType.WOOD));
    public static final DeferredItem<BlockItem> PLANT_DRYER_ITEM = ITEMS.registerSimpleBlockItem(
            "plant_dryer",
            PLANT_DRYER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlantDryerBlockEntity>> PLANT_DRYER_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("plant_dryer", () -> new BlockEntityType<>(
                    PlantDryerBlockEntity::new,
                    PLANT_DRYER.get()));
    public static final DeferredBlock<WildPlantBlock> WILD_BARLEY_PLANT = registerWildPlant(
            "wild_barley_plant",
            WildPlantBlock.Species.WILD_GRAIN);
    public static final DeferredBlock<WildPlantBlock> WILD_TURNIP_PLANT = registerWildPlant(
            "wild_turnip_plant",
            WildPlantBlock.Species.EDIBLE_ROOT);
    public static final DeferredBlock<WildPlantBlock> WILD_BERRY_PLANT = registerWildPlant(
            "wild_berry_plant",
            WildPlantBlock.Species.BERRY);
    public static final DeferredBlock<WildPlantBlock> YARROW_PLANT = registerWildPlant(
            "yarrow_plant",
            WildPlantBlock.Species.MEDICINAL_HERB);
    public static final DeferredBlock<WildPlantBlock> BITTERLEAF_PLANT = registerWildPlant(
            "bitterleaf_plant",
            WildPlantBlock.Species.POISONOUS_HERB);
    public static final DeferredBlock<PreservingBinBlock> PRESERVING_BIN = BLOCKS.registerBlock(
            "preserving_bin",
            PreservingBinBlock::new,
            properties -> properties
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(net.minecraft.world.level.block.SoundType.WOOD));
    public static final DeferredItem<BlockItem> PRESERVING_BIN_ITEM = ITEMS.registerSimpleBlockItem(
            "preserving_bin",
            PRESERVING_BIN);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PreservingBinBlockEntity>> PRESERVING_BIN_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("preserving_bin", () -> new BlockEntityType<>(
                    PreservingBinBlockEntity::new,
                    PRESERVING_BIN.get()));
    public static final DeferredBlock<BasketBlock> BASKET = BLOCKS.registerBlock(
            "basket",
            BasketBlock::new,
            properties -> properties
                    .mapColor(MapColor.WOOD)
                    .noOcclusion()
                    .strength(1.0F)
                    .sound(net.minecraft.world.level.block.SoundType.GRASS));
    public static final DeferredItem<BlockItem> BASKET_ITEM = ITEMS.registerItem(
            "basket",
            properties -> new BlockItem(
                    BASKET.get(),
                    properties.stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasketBlockEntity>> BASKET_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("basket", () -> new BlockEntityType<>(
                    BasketBlockEntity::new,
                    BASKET.get()));
    public static final DeferredBlock<TimberBeamBlock> TIMBER_BEAM = BLOCKS.registerBlock(
            "timber_beam",
            TimberBeamBlock::new,
            properties -> properties
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(net.minecraft.world.level.block.SoundType.WOOD));
    public static final DeferredItem<BlockItem> TIMBER_BEAM_ITEM =
            ITEMS.registerSimpleBlockItem("timber_beam", TIMBER_BEAM);
    public static final DeferredBlock<Block> SUPPORT_POST = BLOCKS.registerBlock(
            "support_post",
            Block::new,
            properties -> properties
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(net.minecraft.world.level.block.SoundType.WOOD));
    public static final DeferredItem<BlockItem> SUPPORT_POST_ITEM =
            ITEMS.registerSimpleBlockItem("support_post", SUPPORT_POST);
    public static final DeferredBlock<ClimbableRopeBlock> ROPE = BLOCKS.registerBlock(
            "rope",
            ClimbableRopeBlock::new,
            properties -> properties
                    .mapColor(MapColor.WOOD)
                    .noCollision()
                    .noOcclusion()
                    .strength(0.2F)
                    .sound(net.minecraft.world.level.block.SoundType.WOOL));
    public static final DeferredItem<BlockItem> ROPE_ITEM = ITEMS.registerSimpleBlockItem("rope", ROPE);
    public static final DeferredBlock<SleepingMatBlock> SLEEPING_MAT = BLOCKS.registerBlock(
            "sleeping_mat",
            SleepingMatBlock::new,
            properties -> properties
                    .mapColor(MapColor.WOOD)
                    .noOcclusion()
                    .strength(0.2F)
                    .sound(net.minecraft.world.level.block.SoundType.WOOL));
    public static final DeferredItem<BlockItem> SLEEPING_MAT_ITEM = ITEMS.registerItem(
            "sleeping_mat",
            properties -> new BlockItem(SLEEPING_MAT.get(), properties.stacksTo(1)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChunkAnchorBlockEntity>> CHUNK_ANCHOR_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("chunk_anchor", () -> new BlockEntityType<>(
                    ChunkAnchorBlockEntity::new,
                    CHUNK_ANCHOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CharcoalPitBlockEntity>> CHARCOAL_PIT_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("charcoal_pit", () -> new BlockEntityType<>(
                    CharcoalPitBlockEntity::new,
                    CHARCOAL_PIT.get()));

    static final long FRESH_FOOD_SPOIL_TICKS = 20L * 60 * 20;
    private static final long SALTED_FOOD_SPOIL_TICKS = FRESH_FOOD_SPOIL_TICKS * 3 / 2;
    private static final long SMOKED_FOOD_SPOIL_TICKS = SALTED_FOOD_SPOIL_TICKS * 5;
    private static final int ANIMAL_DUNG_DROP_CHANCE = 12_000;
    public static final DeferredItem<Item> FIELD_GUIDE = ITEMS.registerItem(
            "field_guide",
            FieldGuideItem::new);
    public static final DeferredItem<Item> DIGGING_STICK = ITEMS.registerItem(
            "digging_stick",
            properties -> new Item(properties.durability(64)));
    public static final DeferredItem<WildPlantItem> WILD_BARLEY = ITEMS.registerItem(
            "wild_barley",
            properties -> new WildPlantItem(properties, WILD_BARLEY_PLANT.get(), WildPlantItem.Effect.NONE));
    public static final DeferredItem<WildPlantItem> BARLEY_GRAIN = ITEMS.registerItem(
            "barley_grain",
            properties -> new WildPlantItem(
                    properties.food(foodProperties(1, 0.1F)),
                    null,
                    WildPlantItem.Effect.NONE));
    public static final DeferredItem<WildPlantItem> WILD_TURNIP = registerWildPlantItem(
            "wild_turnip", WILD_TURNIP_PLANT, 3, 0.3F, WildPlantItem.Effect.NONE);
    public static final DeferredItem<WildPlantItem> WILD_BERRY = registerWildPlantItem(
            "wild_berry", WILD_BERRY_PLANT, 2, 0.2F, WildPlantItem.Effect.NONE);
    public static final DeferredItem<WildPlantItem> YARROW = registerWildPlantItem(
            "yarrow", YARROW_PLANT, 1, 0.1F, WildPlantItem.Effect.MEDICINAL);
    public static final DeferredItem<WildPlantItem> BITTERLEAF = registerWildPlantItem(
            "bitterleaf", BITTERLEAF_PLANT, 1, 0.1F, WildPlantItem.Effect.POISONOUS);
    public static final DeferredItem<WildPlantItem> DRIED_YARROW = ITEMS.registerItem(
            "dried_yarrow",
            properties -> new WildPlantItem(properties.food(foodProperties(2, 0.3F)), null, WildPlantItem.Effect.MEDICINAL));
    public static final DeferredItem<WildPlantItem> DRIED_BITTERLEAF = ITEMS.registerItem(
            "dried_bitterleaf",
            properties -> new WildPlantItem(properties.food(foodProperties(1, 0.1F)), null, WildPlantItem.Effect.NONE));
    public static final DeferredItem<Item> SALT = ITEMS.registerSimpleItem("salt");
    public static final DeferredItem<Item> ICE_SHARD = ITEMS.registerSimpleItem("ice_shard");
    public static final DeferredItem<Item> DUNG = ITEMS.registerSimpleItem("dung");
    public static final DeferredItem<RawHideItem> RAW_HIDE = ITEMS.registerItem(
            "raw_hide",
            RawHideItem::new);
    public static final DeferredItem<Item> CLEANED_HIDE = ITEMS.registerSimpleItem("cleaned_hide");
    public static final DeferredItem<Item> TANNED_HIDE = ITEMS.registerSimpleItem("tanned_hide");
    public static final DeferredItem<PerishableFoodItem> RAW_VENISON = ITEMS.registerItem(
            "raw_venison",
            properties -> new PerishableFoodItem(properties.food(foodProperties(3, 0.3F)), FRESH_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> RAW_MOOSE_MEAT = ITEMS.registerItem(
            "raw_moose_meat",
            properties -> new PerishableFoodItem(properties.food(foodProperties(4, 0.3F)), FRESH_FOOD_SPOIL_TICKS));
    public static final DeferredItem<SpawnEggItem> DEER_SPAWN_EGG = ITEMS.registerItem(
            "deer_spawn_egg",
            properties -> new SpawnEggItem(properties.component(
                    DataComponents.ENTITY_DATA,
                    TypedEntityData.of(DEER.get(), new CompoundTag()))));
    public static final DeferredItem<SpawnEggItem> MOOSE_SPAWN_EGG = ITEMS.registerItem(
            "moose_spawn_egg",
            properties -> new SpawnEggItem(properties.component(
                    DataComponents.ENTITY_DATA,
                    TypedEntityData.of(MOOSE.get(), new CompoundTag()))));
    public static final DeferredItem<SpawnEggItem> PIRANHA_SPAWN_EGG = ITEMS.registerItem(
            "piranha_spawn_egg",
            properties -> new SpawnEggItem(properties.component(
                    DataComponents.ENTITY_DATA,
                    TypedEntityData.of(PIRANHA.get(), new CompoundTag()))));
    public static final DeferredItem<SpawnEggItem> SHARK_SPAWN_EGG = ITEMS.registerItem(
            "shark_spawn_egg",
            properties -> new SpawnEggItem(properties.component(
                    DataComponents.ENTITY_DATA,
                    TypedEntityData.of(SHARK.get(), new CompoundTag()))));
    public static final DeferredItem<PerishableFoodItem> RAW_SHARK_MEAT = ITEMS.registerItem(
            "raw_shark_meat",
            properties -> new PerishableFoodItem(properties.food(foodProperties(4, 0.3F)), FRESH_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> RAW_BEEF = ITEMS.registerItem(
            "raw_beef",
            properties -> new PerishableFoodItem(properties.food(foodProperties(3, 0.3F)), FRESH_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> RAW_PORK = ITEMS.registerItem(
            "raw_pork",
            properties -> new PerishableFoodItem(properties.food(foodProperties(3, 0.3F)), FRESH_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> RAW_CHICKEN = ITEMS.registerItem(
            "raw_chicken",
            properties -> new PerishableFoodItem(properties.food(foodProperties(2, 0.3F)), FRESH_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> RAW_MUTTON = ITEMS.registerItem(
            "raw_mutton",
            properties -> new PerishableFoodItem(properties.food(foodProperties(2, 0.3F)), FRESH_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> RAW_RABBIT = ITEMS.registerItem(
            "raw_rabbit",
            properties -> new PerishableFoodItem(properties.food(foodProperties(3, 0.3F)), FRESH_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> SALTED_BEEF = ITEMS.registerItem(
            "salted_beef",
            properties -> new PerishableFoodItem(properties.food(foodProperties(3, 0.3F)), SALTED_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> SALTED_PORK = ITEMS.registerItem(
            "salted_pork",
            properties -> new PerishableFoodItem(properties.food(foodProperties(3, 0.3F)), SALTED_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> SALTED_CHICKEN = ITEMS.registerItem(
            "salted_chicken",
            properties -> new PerishableFoodItem(properties.food(foodProperties(2, 0.3F)), SALTED_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> SALTED_MUTTON = ITEMS.registerItem(
            "salted_mutton",
            properties -> new PerishableFoodItem(properties.food(foodProperties(2, 0.3F)), SALTED_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> SALTED_RABBIT = ITEMS.registerItem(
            "salted_rabbit",
            properties -> new PerishableFoodItem(properties.food(foodProperties(3, 0.3F)), SALTED_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> SMOKED_BEEF = ITEMS.registerItem(
            "smoked_beef",
            properties -> new PerishableFoodItem(
                    properties.food(foodProperties(8, 0.8F)),
                    SMOKED_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> SMOKED_PORK = ITEMS.registerItem(
            "smoked_pork",
            properties -> new PerishableFoodItem(
                    properties.food(foodProperties(8, 0.8F)),
                    SMOKED_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> SMOKED_CHICKEN = ITEMS.registerItem(
            "smoked_chicken",
            properties -> new PerishableFoodItem(
                    properties.food(foodProperties(6, 0.6F)),
                    SMOKED_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> SMOKED_MUTTON = ITEMS.registerItem(
            "smoked_mutton",
            properties -> new PerishableFoodItem(
                    properties.food(foodProperties(6, 0.8F)),
                    SMOKED_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> SMOKED_RABBIT = ITEMS.registerItem(
            "smoked_rabbit",
            properties -> new PerishableFoodItem(
                    properties.food(foodProperties(5, 0.8F)),
                    SMOKED_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> RAW_COD = registerPerishableFish(
            "raw_cod", Foods.COD, FRESH_FOOD_SPOIL_TICKS);
    public static final DeferredItem<PerishableFoodItem> RAW_SALMON = registerPerishableFish(
            "raw_salmon", Foods.SALMON, FRESH_FOOD_SPOIL_TICKS);
    public static final DeferredItem<PerishableFoodItem> RAW_TROPICAL_FISH = registerPerishableFish(
            "raw_tropical_fish", Foods.TROPICAL_FISH, FRESH_FOOD_SPOIL_TICKS);
    public static final DeferredItem<PerishableFoodItem> RAW_PUFFERFISH = registerPerishablePufferfish(
            "raw_pufferfish", FRESH_FOOD_SPOIL_TICKS);
    public static final DeferredItem<PerishableFoodItem> RAW_SQUID = registerPerishableFish(
            "raw_squid", Foods.COD, FRESH_FOOD_SPOIL_TICKS);
    public static final DeferredItem<PerishableFoodItem> SALTED_COD = registerPerishableFish(
            "salted_cod", Foods.COD, SALTED_FOOD_SPOIL_TICKS);
    public static final DeferredItem<PerishableFoodItem> SALTED_SALMON = registerPerishableFish(
            "salted_salmon", Foods.SALMON, SALTED_FOOD_SPOIL_TICKS);
    public static final DeferredItem<PerishableFoodItem> SALTED_TROPICAL_FISH = registerPerishableFish(
            "salted_tropical_fish", Foods.TROPICAL_FISH, SALTED_FOOD_SPOIL_TICKS);
    public static final DeferredItem<PerishableFoodItem> SALTED_PUFFERFISH = registerPerishablePufferfish(
            "salted_pufferfish", SALTED_FOOD_SPOIL_TICKS);
    public static final DeferredItem<PerishableFoodItem> SMOKED_COD = registerPerishableFish(
            "smoked_cod", Foods.COOKED_COD, SMOKED_FOOD_SPOIL_TICKS);
    public static final DeferredItem<PerishableFoodItem> SMOKED_SALMON = registerPerishableFish(
            "smoked_salmon", Foods.COOKED_SALMON, SMOKED_FOOD_SPOIL_TICKS);
    public static final DeferredItem<PerishableFoodItem> SMOKED_TROPICAL_FISH = registerPerishableFish(
            "smoked_tropical_fish", Foods.TROPICAL_FISH, SMOKED_FOOD_SPOIL_TICKS);
    public static final DeferredItem<PerishableFoodItem> SMOKED_PUFFERFISH = registerPerishablePufferfish(
            "smoked_pufferfish", SMOKED_FOOD_SPOIL_TICKS);

    public static final DeferredItem<CarcassItem> COW_CARCASS = ITEMS.registerItem("cow_carcass", properties -> new CarcassItem(
            properties,
            primitiveisolation::cowButcheredDrops));
    public static final DeferredItem<CarcassItem> PIG_CARCASS = ITEMS.registerItem("pig_carcass", properties -> new CarcassItem(
            properties,
            primitiveisolation::pigButcheredDrops));
    public static final DeferredItem<CarcassItem> CHICKEN_CARCASS = ITEMS.registerItem("chicken_carcass", properties -> new CarcassItem(
            properties,
            primitiveisolation::chickenButcheredDrops));
    public static final DeferredItem<CarcassItem> MOOSHROOM_CARCASS = ITEMS.registerItem(
            "mooshroom_carcass",
            properties -> new CarcassItem(properties, primitiveisolation::mooshroomButcheredDrops));
    public static final DeferredItem<CarcassItem> SHEEP_CARCASS = ITEMS.registerItem(
            "sheep_carcass",
            properties -> new CarcassItem(properties, primitiveisolation::sheepButcheredDrops));
    public static final DeferredItem<CarcassItem> RABBIT_CARCASS = ITEMS.registerItem(
            "rabbit_carcass",
            properties -> new CarcassItem(properties, primitiveisolation::rabbitButcheredDrops));
    public static final DeferredItem<CarcassItem> POLAR_BEAR_CARCASS = ITEMS.registerItem(
            "polar_bear_carcass",
            properties -> new CarcassItem(properties, primitiveisolation::polarBearButcheredDrops));
    public static final DeferredItem<CarcassItem> HOGLIN_CARCASS = ITEMS.registerItem(
            "hoglin_carcass",
            properties -> new CarcassItem(properties, primitiveisolation::hoglinButcheredDrops));
    public static final DeferredItem<CarcassItem> DEER_CARCASS = ITEMS.registerItem(
            "deer_carcass",
            properties -> new CarcassItem(properties, primitiveisolation::deerButcheredDrops));
    public static final DeferredItem<CarcassItem> MOOSE_CARCASS = ITEMS.registerItem(
            "moose_carcass",
            properties -> new CarcassItem(properties, primitiveisolation::mooseButcheredDrops));
    public static final DeferredItem<HandDrillItem> HAND_DRILL = ITEMS.registerItem(
            "hand_drill",
            properties -> new HandDrillItem(properties.durability(24)));
    public static final DeferredItem<Item> BARK = ITEMS.registerSimpleItem("bark");
    public static final DeferredItem<Item> GRASS_FIBER = ITEMS.registerSimpleItem("grass_fiber");
    public static final DeferredItem<FlintKnifeItem> FLINT_KNIFE = ITEMS.registerItem(
            "flint_knife",
            properties -> new FlintKnifeItem(properties.durability(29).repairable(FLINT_TOOL_REPAIR_ITEMS)));
    public static final DeferredItem<Item> FLINT_AXE_HEAD = ITEMS.registerSimpleItem("flint_axe_head");
    public static final DeferredItem<Item> FLINT_PICKAXE_HEAD = ITEMS.registerSimpleItem("flint_pickaxe_head");
    public static final DeferredItem<Item> FLINT_HOE_HEAD = ITEMS.registerSimpleItem("flint_hoe_head");
    public static final DeferredItem<Item> FLINT_SHOVEL_HEAD = ITEMS.registerSimpleItem("flint_shovel_head");
    public static final DeferredItem<Item> FLINT_KNIFE_HEAD = ITEMS.registerSimpleItem("flint_knife_head");
    public static final DeferredItem<Item> FLINT_AXE = ITEMS.registerItem(
            "flint_axe",
            properties -> new AxeItem(FLINT_TOOL_MATERIAL, 7.0F, -3.2F, properties));
    public static final DeferredItem<Item> FLINT_PICKAXE = ITEMS.registerSimpleItem(
            "flint_pickaxe",
            properties -> properties.pickaxe(FLINT_TOOL_MATERIAL, 1.0F, -2.8F));
    public static final DeferredItem<Item> FLINT_HOE = ITEMS.registerItem(
            "flint_hoe",
            properties -> new HoeItem(FLINT_TOOL_MATERIAL, -1.0F, -2.0F, properties));
    public static final DeferredItem<Item> FLINT_SHOVEL = ITEMS.registerItem(
            "flint_shovel",
            properties -> new ShovelItem(FLINT_TOOL_MATERIAL, 1.5F, -3.0F, properties));

    // Creates the mod's creative tab.
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.primitiveisolation")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> FLINT_KNIFE.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(SALT.get());
                output.accept(ICE_SHARD.get());
                output.accept(DUNG.get());
                output.accept(SALT_DEPOSIT_ITEM.get());
                output.accept(CHARCOAL_PIT_ITEM.get());
                output.accept(CHUNK_ANCHOR_ITEM.get());
                output.accept(SALTING_RACK_ITEM.get());
                output.accept(DRYING_RACK_ITEM.get());
                output.accept(PLANT_DRYER_ITEM.get());
                output.accept(PRESERVING_BIN_ITEM.get());
                output.accept(BASKET_ITEM.get());
                output.accept(TIMBER_BEAM_ITEM.get());
                output.accept(SUPPORT_POST_ITEM.get());
                output.accept(ROPE_ITEM.get());
                output.accept(SLEEPING_MAT_ITEM.get());
                output.accept(RAW_BEEF.get());
                output.accept(RAW_PORK.get());
                output.accept(RAW_CHICKEN.get());
                output.accept(RAW_MUTTON.get());
                output.accept(RAW_RABBIT.get());
                output.accept(SALTED_BEEF.get());
                output.accept(SALTED_PORK.get());
                output.accept(SALTED_CHICKEN.get());
                output.accept(SALTED_MUTTON.get());
                output.accept(SALTED_RABBIT.get());
                output.accept(SMOKED_BEEF.get());
                output.accept(SMOKED_PORK.get());
                output.accept(SMOKED_CHICKEN.get());
                output.accept(SMOKED_MUTTON.get());
                output.accept(SMOKED_RABBIT.get());
                output.accept(RAW_COD.get());
                output.accept(RAW_SALMON.get());
                output.accept(RAW_TROPICAL_FISH.get());
                output.accept(RAW_PUFFERFISH.get());
                output.accept(RAW_SQUID.get());
                output.accept(SALTED_COD.get());
                output.accept(SALTED_SALMON.get());
                output.accept(SALTED_TROPICAL_FISH.get());
                output.accept(SALTED_PUFFERFISH.get());
                output.accept(SMOKED_COD.get());
                output.accept(SMOKED_SALMON.get());
                output.accept(SMOKED_TROPICAL_FISH.get());
                output.accept(SMOKED_PUFFERFISH.get());
                output.accept(COW_CARCASS.get());
                output.accept(PIG_CARCASS.get());
                output.accept(CHICKEN_CARCASS.get());
                output.accept(MOOSHROOM_CARCASS.get());
                output.accept(SHEEP_CARCASS.get());
                output.accept(RABBIT_CARCASS.get());
                output.accept(POLAR_BEAR_CARCASS.get());
                output.accept(HOGLIN_CARCASS.get());
                output.accept(HAND_DRILL.get());
                output.accept(BARK.get());
                output.accept(GRASS_FIBER.get());
                output.accept(FIELD_GUIDE.get());
                output.accept(DIGGING_STICK.get());
                output.accept(WILD_BARLEY.get());
                output.accept(BARLEY_GRAIN.get());
                output.accept(WILD_TURNIP.get());
                output.accept(WILD_BERRY.get());
                output.accept(YARROW.get());
                output.accept(BITTERLEAF.get());
                output.accept(DRIED_YARROW.get());
                output.accept(DRIED_BITTERLEAF.get());
                output.accept(RAW_HIDE.get());
                output.accept(CLEANED_HIDE.get());
                output.accept(TANNED_HIDE.get());
                output.accept(RAW_VENISON.get());
                output.accept(RAW_MOOSE_MEAT.get());
                output.accept(RAW_SHARK_MEAT.get());
                output.accept(DEER_SPAWN_EGG.get());
                output.accept(MOOSE_SPAWN_EGG.get());
                output.accept(PIRANHA_SPAWN_EGG.get());
                output.accept(SHARK_SPAWN_EGG.get());
                output.accept(DEER_CARCASS.get());
                output.accept(MOOSE_CARCASS.get());
                output.accept(FLINT_KNIFE.get());
                output.accept(FLINT_AXE_HEAD.get());
                output.accept(FLINT_PICKAXE_HEAD.get());
                output.accept(FLINT_HOE_HEAD.get());
                output.accept(FLINT_SHOVEL_HEAD.get());
                output.accept(FLINT_KNIFE_HEAD.get());
                output.accept(KNAPPING_STATION_ITEM.get());
                output.accept(BUTCHERY_TABLE_ITEM.get());
                output.accept(FLINT_AXE.get());
                output.accept(FLINT_PICKAXE.get());
                output.accept(FLINT_HOE.get());
                output.accept(FLINT_SHOVEL.get());
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public primitiveisolation(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerEntityAttributes);
        modEventBus.addListener(this::registerSpawnPlacements);
        modEventBus.addListener(this::registerSleepingMatBedEntity);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        GLOBAL_LOOT_MODIFIER_SERIALIZERS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (primitiveisolation) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void registerSleepingMatBedEntity(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.BED, SLEEPING_MAT.get());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(DEER.get(), Deer.createAttributes().build());
        event.put(MOOSE.get(), Moose.createAttributes().build());
        event.put(PIRANHA.get(), Piranha.createAttributes().build());
        event.put(SHARK.get(), Shark.createAttributes().build());
        event.put(COW_CORPSE.get(), Animal.createAnimalAttributes().build());
        event.put(PIG_CORPSE.get(), Pig.createAttributes().build());
        event.put(CHICKEN_CORPSE.get(), Chicken.createAttributes().build());
        event.put(MOOSHROOM_CORPSE.get(), MushroomCow.createAttributes().build());
        event.put(SHEEP_CORPSE.get(), Sheep.createAttributes().build());
        event.put(RABBIT_CORPSE.get(), Rabbit.createAttributes().build());
        event.put(POLAR_BEAR_CORPSE.get(), PolarBear.createAttributes().build());
        event.put(HOGLIN_CORPSE.get(), Hoglin.createAttributes().build());
        event.put(DEER_CORPSE.get(), Deer.createAttributes().build());
        event.put(MOOSE_CORPSE.get(), Moose.createAttributes().build());
    }

    private void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                DEER.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(
                MOOSE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(
                PIRANHA.get(),
                SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WaterAnimal::checkSurfaceWaterAnimalSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(
                SHARK.get(),
                SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WaterAnimal::checkSurfaceWaterAnimalSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    static List<ItemStack> cowButcheredDrops() {
        return List.of(
                new ItemStack(RAW_BEEF.get(), 3),
                new ItemStack(RAW_HIDE.get(), 2),
                new ItemStack(Items.BONE));
    }

    static List<ItemStack> pigButcheredDrops() {
        return List.of(new ItemStack(RAW_PORK.get(), 3));
    }

    static List<ItemStack> chickenButcheredDrops() {
        return List.of(new ItemStack(RAW_CHICKEN.get(), 2), new ItemStack(Items.FEATHER), new ItemStack(Items.BONE));
    }

    static List<ItemStack> mooshroomButcheredDrops() {
        return mooshroomButcheredDrops(RandomSource.create());
    }

    static List<ItemStack> mooshroomButcheredDrops(RandomSource random) {
        int hideCount = random.nextInt(3);
        if (hideCount == 0) {
            return List.of(
                    new ItemStack(Items.BONE),
                    new ItemStack(RAW_BEEF.get(), 1 + random.nextInt(3)));
        }
        return List.of(
                new ItemStack(Items.BONE),
                new ItemStack(RAW_BEEF.get(), 1 + random.nextInt(3)),
                new ItemStack(RAW_HIDE.get(), hideCount));
    }

    static List<ItemStack> sheepButcheredDrops() {
        return sheepButcheredDrops(RandomSource.create());
    }

    static List<ItemStack> sheepButcheredDrops(RandomSource random) {
        return meatDrop(RAW_MUTTON.get(), 1, 2, random);
    }

    static List<ItemStack> rabbitButcheredDrops() {
        return rabbitButcheredDrops(RandomSource.create());
    }

    static List<ItemStack> rabbitButcheredDrops(RandomSource random) {
        return meatDrop(RAW_RABBIT.get(), 0, 1, random);
    }

    static List<ItemStack> polarBearButcheredDrops() {
        return polarBearButcheredDrops(RandomSource.create());
    }

    static List<ItemStack> polarBearButcheredDrops(RandomSource random) {
        int fishCount = random.nextInt(3);
        if (fishCount == 0) {
            return List.of();
        }
        Item fish = random.nextInt(4) == 0 ? RAW_SALMON.get() : RAW_COD.get();
        return List.of(new ItemStack(fish, fishCount));
    }

    static List<ItemStack> polarBearTableButcheredDrops() {
        Item fish = RandomSource.create().nextInt(4) == 0 ? RAW_SALMON.get() : RAW_COD.get();
        return List.of(new ItemStack(fish));
    }

    static List<ItemStack> hoglinButcheredDrops() {
        return hoglinButcheredDrops(RandomSource.create());
    }

    static List<ItemStack> hoglinButcheredDrops(RandomSource random) {
        return meatDrop(RAW_PORK.get(), 2, 4, random);
    }

    static List<ItemStack> deerButcheredDrops() {
        return deerButcheredDrops(RandomSource.create());
    }

    static List<ItemStack> deerButcheredDrops(RandomSource random) {
        return List.of(
                new ItemStack(RAW_VENISON.get(), 2 + random.nextInt(3)),
                new ItemStack(RAW_HIDE.get(), 1 + random.nextInt(2)));
    }

    static List<ItemStack> mooseButcheredDrops() {
        return mooseButcheredDrops(RandomSource.create());
    }

    static List<ItemStack> mooseButcheredDrops(RandomSource random) {
        return List.of(
                new ItemStack(RAW_MOOSE_MEAT.get(), 4 + random.nextInt(4)),
                new ItemStack(RAW_HIDE.get(), 2 + random.nextInt(3)),
                new ItemStack(Items.BONE, 1 + random.nextInt(2)));
    }

    private static List<ItemStack> meatDrop(Item meat, int minimum, int maximum, RandomSource random) {
        int count = minimum + random.nextInt(maximum - minimum + 1);
        return count == 0 ? List.of() : List.of(new ItemStack(meat, count));
    }

    private static FoodProperties foodProperties(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
    }

    private static DeferredBlock<WildPlantBlock> registerWildPlant(String name, WildPlantBlock.Species species) {
        return BLOCKS.registerBlock(
                name,
                properties -> new WildPlantBlock(species, properties),
                properties -> properties
                        .mapColor(MapColor.PLANT)
                        .noCollision()
                        .noOcclusion()
                        .instabreak()
                        .sound(net.minecraft.world.level.block.SoundType.GRASS));
    }

    private static DeferredItem<WildPlantItem> registerWildPlantItem(
            String name,
            DeferredBlock<WildPlantBlock> plant,
            int nutrition,
            float saturation,
            WildPlantItem.Effect effect) {
        return ITEMS.registerItem(
                name,
                properties -> new WildPlantItem(
                        properties.food(foodProperties(nutrition, saturation)),
                        plant.get(),
                        effect));
    }

    private static DeferredItem<PerishableFoodItem> registerPerishableFish(
            String name,
            FoodProperties foodProperties,
            long spoilDurationTicks) {
        return ITEMS.registerItem(name, properties -> new PerishableFoodItem(
                properties.food(foodProperties),
                spoilDurationTicks));
    }

    private static DeferredItem<PerishableFoodItem> registerPerishablePufferfish(
            String name,
            long spoilDurationTicks) {
        return ITEMS.registerItem(name, properties -> new PerishableFoodItem(
                properties.food(Foods.PUFFERFISH, Consumables.PUFFERFISH),
                spoilDurationTicks));
    }

    static Item perishableFoodFor(Item item) {
        if (item == Items.BEEF) {
            return RAW_BEEF.get();
        }
        if (item == Items.PORKCHOP) {
            return RAW_PORK.get();
        }
        if (item == Items.CHICKEN) {
            return RAW_CHICKEN.get();
        }
        if (item == Items.MUTTON) {
            return RAW_MUTTON.get();
        }
        if (item == Items.RABBIT) {
            return RAW_RABBIT.get();
        }
        if (item == Items.COD) {
            return RAW_COD.get();
        }
        if (item == Items.SALMON) {
            return RAW_SALMON.get();
        }
        if (item == Items.TROPICAL_FISH) {
            return RAW_TROPICAL_FISH.get();
        }
        if (item == Items.PUFFERFISH) {
            return RAW_PUFFERFISH.get();
        }
        return null;
    }

    static long spoilDurationFor(Item item) {
        if (item instanceof PerishableFoodItem perishableFood) {
            return perishableFood.getSpoilDurationTicks();
        }
        return perishableFoodFor(item) == null ? 0 : FRESH_FOOD_SPOIL_TICKS;
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void removeRabbitHideLeatherRecipe(ModifyRecipeJsonsEvent event) {
        event.getRecipeJsons().remove(Identifier.fromNamespaceAndPath("minecraft", "leather"));
    }

    @SubscribeEvent
    public void onCampfirePlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getPlacedBlock().is(BlockTags.CAMPFIRES)
                && event.getLevel() instanceof ServerLevel level) {
            this.trackCampfire(level, event.getPos());
        }
    }

    @SubscribeEvent
    public void onCampfireRelight(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack itemStack = player.getItemInHand(event.getHand());
        BlockState campfireState = event.getLevel().getBlockState(event.getPos());
        if (!itemStack.is(Items.FLINT_AND_STEEL)
                || !CampfireBlock.canLight(campfireState)
                || player.isSpectator()) {
            return;
        }

        event.setCanceled(true);
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockPos pos = event.getPos();
        level.setBlock(pos, campfireState.setValue(CampfireBlock.LIT, true), 11);
        level.playSound(
                null,
                pos,
                SoundEvents.FLINTANDSTEEL_USE,
                SoundSource.BLOCKS,
                1.0F,
                level.getRandom().nextFloat() * 0.4F + 0.8F);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        if (!player.hasInfiniteMaterials()) {
            itemStack.hurtAndBreak(1, player, event.getHand());
        }
    }

    @SubscribeEvent
    public void onLogStripped(BlockEvent.BlockToolModificationEvent event) {
        if (event.isSimulated()
                || event.getItemAbility() != ItemAbilities.AXE_STRIP
                || !event.getState().is(BlockTags.LOGS)
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockPos pos = event.getPos();
        ItemEntity bark = new ItemEntity(
                level,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                new ItemStack(BARK.get()));
        level.addFreshEntity(bark);
    }

    @SubscribeEvent
    public void onWildRootBroken(BreakBlockEvent event) {
        if (!(event.getState().getBlock() instanceof WildPlantBlock plant)
                || !plant.getSpecies().requiresDiggingStick()) {
            return;
        }

        event.setCanceled(true);
        Player player = event.getPlayer();
        player.sendOverlayMessage(Component.translatable(
                player.getMainHandItem().is(DIGGING_STICK.get())
                        ? "message.primitiveisolation.use_digging_stick"
                        : "message.primitiveisolation.need_digging_stick"));
    }

    @SubscribeEvent
    public void onGrassFiberHarvest(BreakBlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getState().is(Blocks.SHORT_GRASS) || event.getState().is(Blocks.TALL_GRASS))) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack knife = player.getMainHandItem();
        if (!knife.is(FLINT_KNIFE.get())) {
            return;
        }

        if (!player.hasInfiniteMaterials()) {
            knife.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
        }

        if (level.getRandom().nextInt(8) != 0) {
            return;
        }

        BlockPos pos = event.getPos();
        ItemEntity fiber = new ItemEntity(
                level,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                new ItemStack(GRASS_FIBER.get()));
        level.addFreshEntity(fiber);
    }

    @SubscribeEvent
    public void onCampfireChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }

        for (BlockPos pos : chunk.getBlockEntities().keySet()) {
            if (chunk.getBlockState(pos).is(BlockTags.CAMPFIRES)) {
                this.trackCampfire(level, pos);
            }
        }
    }

    @SubscribeEvent
    public void onCampfireChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }

        Set<BlockPos> positions = this.trackedCampfires.get(level);
        if (positions == null) {
            return;
        }

        ChunkPos chunkPos = chunk.getPos();
        positions.removeIf(pos -> ChunkPos.containing(pos).equals(chunkPos));
        if (positions.isEmpty()) {
            this.trackedCampfires.remove(level);
        }
    }

    @SubscribeEvent
    public void onCampfireLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            this.trackedCampfires.remove(level);
        }
    }

    @SubscribeEvent
    public void onCampfireLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || level.getGameTime() % 20 != 0) {
            return;
        }

        Set<BlockPos> positions = this.trackedCampfires.get(level);
        if (positions == null) {
            return;
        }

        Iterator<BlockPos> iterator = positions.iterator();
        while (iterator.hasNext()) {
            BlockPos campfirePos = iterator.next();
            var campfireState = level.getBlockState(campfirePos);
            if (!campfireState.is(BlockTags.CAMPFIRES)
                    || !campfireState.hasProperty(CampfireBlock.LIT)) {
                iterator.remove();
                continue;
            }
            if (CampfireBlock.isLitCampfire(campfireState)
                    && level.isRainingAt(campfirePos.above())) {
                level.setBlock(campfirePos, campfireState.setValue(CampfireBlock.LIT, false), 3);
                continue;
            }
            if (!CampfireBlock.isLitCampfire(campfireState)) {
                continue;
            }

            for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -2; z <= 2; z++) {
                        if (x == 0 && y == 0 && z == 0) {
                            continue;
                        }

                        BlockPos woodPos = campfirePos.offset(x, y, z);
                        var woodState = level.getBlockState(woodPos);
                        if (isWoodBlock(woodState) && level.getRandom().nextInt(60) == 0) {
                            igniteBesideWood(level, campfirePos, woodPos, woodState);
                        }
                    }
                }
            }
        }

        if (positions.isEmpty()) {
            this.trackedCampfires.remove(level);
        }
    }

    @SubscribeEvent
    public void onSleepingMatServerTickPre(ServerTickEvent.Pre event) {
        ServerClockManager clockManager = event.getServer().clockManager();
        Set<Holder<WorldClock>> activeClocks = new HashSet<>();
        Set<ServerLevel> activeLevels = Collections.newSetFromMap(new IdentityHashMap<>());

        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!SleepingMatSleep.shouldAccelerate(level, clockManager)) {
                continue;
            }

            Holder<WorldClock> clock = level.dimensionType().defaultClock().orElse(null);
            if (clock == null) {
                continue;
            }
            activeClocks.add(clock);
            activeLevels.add(level);
        }

        for (Holder<WorldClock> clock : activeClocks) {
            if (!this.sleepingMatOriginalClockRates.containsKey(clock)) {
                float originalRate = clockManager.getRate(clock);
                this.sleepingMatOriginalClockRates.put(clock, originalRate);
                clockManager.setRate(clock, originalRate * 10.0F);
            }
        }

        Iterator<Map.Entry<Holder<WorldClock>, Float>> iterator = this.sleepingMatOriginalClockRates.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Holder<WorldClock>, Float> entry = iterator.next();
            if (!activeClocks.contains(entry.getKey())) {
                clockManager.setRate(entry.getKey(), entry.getValue());
                iterator.remove();
            }
        }

        SleepingMatSleep.setAcceleratedLevels(activeLevels);
    }

    @SubscribeEvent
    public void onSleepingMatServerTickPost(ServerTickEvent.Post event) {
        ServerClockManager clockManager = event.getServer().clockManager();
        for (ServerLevel level : SleepingMatSleep.getAcceleratedLevels()) {
            if (!SleepingMatSleep.hasSleepingMatPlayer(level)
                    || !SleepingMatSleep.shouldWakeAtMorning(level, clockManager)) {
                continue;
            }

            if (level.getGameRules().get(GameRules.ADVANCE_WEATHER) && level.isRaining()) {
                level.resetWeatherCycle();
            }
            for (ServerPlayer player : level.getPlayers(ServerPlayer::isSleeping)) {
                player.stopSleepInBed(false, false);
            }
            level.updateSleepingPlayerList();
        }
        SleepingMatSleep.setAcceleratedLevels(Set.of());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        ServerClockManager clockManager = event.getServer().clockManager();
        for (Map.Entry<Holder<WorldClock>, Float> entry : this.sleepingMatOriginalClockRates.entrySet()) {
            clockManager.setRate(entry.getKey(), entry.getValue());
        }
        this.sleepingMatOriginalClockRates.clear();
        SleepingMatSleep.setAcceleratedLevels(Set.of());
        SleepingMatSleep.clearSleepTargets();
    }

    @SubscribeEvent
    public void onItemEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof ItemEntity itemEntity
                && itemEntity.level() instanceof ServerLevel level
                && itemEntity.getItem().getItem() instanceof PerishableFoodItem food) {
            food.tickItemEntity(itemEntity, level);
        }
    }

    @SubscribeEvent
    public void onAnimalDungTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Animal animal
                && animal.level() instanceof ServerLevel level
                && animal.isAlive()
                && animal.getRandom().nextInt(ANIMAL_DUNG_DROP_CHANCE) == 0) {
            animal.spawnAtLocation(level, new ItemStack(DUNG.get()));
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        Inventory inventory = event.getEntity().getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            Item replacement = perishableFoodFor(stack.getItem());
            if (replacement != null) {
                inventory.setItem(slot, new ItemStack(replacement, stack.getCount()));
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            Item replacement = perishableFoodFor(stack.getItem());
            if (replacement != null) {
                itemEntity.setItem(new ItemStack(replacement, stack.getCount()));
            }
        }

        if (event.getEntity() instanceof Spider spider) {
            spider.goalSelector.addGoal(3, new SpiderWebAttackGoal(spider));
        }

        if (event.getEntity() instanceof Squid squid && squid.getType() == EntityType.SQUID) {
            squid.targetSelector.addGoal(
                    1,
                    new NearestAttackableTargetGoal<>(
                            squid,
                            Player.class,
                            false,
                            (entity, level) -> entity instanceof Player player
                                    && (player.isInWater()
                                            || SquidGrabPlayerGoal.isWithinGrabRange(squid, player))));
            squid.goalSelector.addGoal(2, new SquidInkAttackGoal(squid));
            squid.goalSelector.addGoal(3, new SquidGrabPlayerGoal(squid));
        }

        if (!(event.getEntity() instanceof Zombie zombie)) {
            return;
        }
        zombie.goalSelector.addGoal(5, new ZombieEatMeatGoal(zombie));
        zombie.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(zombie, Animal.class, true));
    }

    @SubscribeEvent
    public void onWoodHarvestCheck(PlayerEvent.HarvestCheck event) {
        if (event.getTargetBlock().is(BlockTags.LOGS)) {
            event.setCanHarvest(event.getEntity().getMainHandItem().canPerformAction(ItemAbilities.AXE_STRIP));
        }
    }

    @SubscribeEvent
    public void onAnimalDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof Squid squid && squid.getType() == EntityType.SQUID) {
            event.getDrops().add(new ItemEntity(
                    squid.level(),
                    squid.getX(),
                    squid.getY(),
                    squid.getZ(),
                    new ItemStack(RAW_SQUID.get(), 1 + squid.getRandom().nextInt(2))));
        }

        if (!(event.getEntity() instanceof Animal animal)) {
            return;
        }
        if (replacesLeatherWithHide(animal.getType())) {
            for (ItemEntity drop : event.getDrops()) {
                ItemStack stack = drop.getItem();
                if (stack.is(Items.LEATHER)) {
                    drop.setItem(new ItemStack(RAW_HIDE.get(), stack.getCount()));
                }
            }
        }
        if (animal instanceof MushroomCow mooshroom && mooshroom.getType() == EntityType.MOOSHROOM) {
            if (spawnCorpse(animal, MOOSHROOM_CORPSE.get())) {
                event.getDrops().clear();
            }
        } else if (animal instanceof Cow cow && cow.getType() == EntityType.COW) {
            if (spawnCorpse(animal, COW_CORPSE.get())) {
                event.getDrops().clear();
            }
        } else if (animal instanceof Pig pig && pig.getType() == EntityType.PIG) {
            if (spawnCorpse(animal, PIG_CORPSE.get())) {
                event.getDrops().clear();
            }
        } else if (animal instanceof Chicken chicken && chicken.getType() == EntityType.CHICKEN) {
            if (spawnCorpse(animal, CHICKEN_CORPSE.get())) {
                event.getDrops().clear();
            }
        } else if (animal instanceof Sheep sheep && sheep.getType() == EntityType.SHEEP) {
            if (spawnCorpse(animal, SHEEP_CORPSE.get())) {
                event.getDrops().clear();
            }
        } else if (animal instanceof Rabbit rabbit && rabbit.getType() == EntityType.RABBIT) {
            if (spawnCorpse(animal, RABBIT_CORPSE.get())) {
                event.getDrops().clear();
            }
        } else if (animal instanceof PolarBear bear && bear.getType() == EntityType.POLAR_BEAR) {
            if (spawnCorpse(animal, POLAR_BEAR_CORPSE.get())) {
                event.getDrops().clear();
            }
        } else if (animal instanceof Hoglin hoglin && hoglin.getType() == EntityType.HOGLIN) {
            if (spawnCorpse(animal, HOGLIN_CORPSE.get())) {
                event.getDrops().clear();
            }
        } else if (animal instanceof Deer deer && deer.getType() == DEER.get()) {
            if (spawnCorpse(animal, DEER_CORPSE.get())) {
                event.getDrops().clear();
            }
        } else if (animal instanceof Moose moose && moose.getType() == MOOSE.get()) {
            if (spawnCorpse(animal, MOOSE_CORPSE.get())) {
                event.getDrops().clear();
            }
        }
    }

    private static boolean replacesLeatherWithHide(EntityType<?> entityType) {
        return entityType == EntityType.HORSE
                || entityType == EntityType.DONKEY
                || entityType == EntityType.MULE
                || entityType == EntityType.LLAMA
                || entityType == EntityType.TRADER_LLAMA;
    }

    private boolean spawnCorpse(Animal animal, EntityType<? extends Animal> corpseType) {
        Animal corpse = corpseType.create(animal.level(), EntitySpawnReason.EVENT);
        if (corpse == null) {
            LOGGER.error("Could not create animal corpse for {} at {}", animal.getType(), animal.position());
            return false;
        }

        corpse.setPos(animal.getX(), animal.getY(), animal.getZ());
        corpse.setYRot(animal.getYRot());
        corpse.setXRot(animal.getXRot());
        if (animal instanceof Cow source && corpse instanceof CowCorpse target) {
            target.setVariant(source.getVariant());
        } else if (animal instanceof Pig source && corpse instanceof PigCorpse target) {
            target.setComponent(DataComponents.PIG_VARIANT, source.getVariant());
        } else if (animal instanceof Chicken source && corpse instanceof ChickenCorpse target) {
            target.setVariant(source.getVariant());
        } else if (animal instanceof MushroomCow source && corpse instanceof MooshroomCorpse target) {
            target.setComponent(DataComponents.MOOSHROOM_VARIANT, source.getVariant());
        } else if (animal instanceof Sheep source && corpse instanceof SheepCorpse target) {
            target.setColor(source.getColor());
            target.setSheared(source.isSheared());
        } else if (animal instanceof Rabbit source && corpse instanceof RabbitCorpse target) {
            target.setComponent(DataComponents.RABBIT_VARIANT, source.getVariant());
        }
        if (animal.isBaby()) {
            corpse.setBaby(true);
        }

        if (!animal.level().addFreshEntity(corpse)) {
            LOGGER.error("Could not add animal corpse for {} at {}", animal.getType(), animal.position());
            return false;
        }

        return true;
    }

    @SubscribeEvent
    public void onSpearFishing(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof AbstractFish fish)
                || !fish.isInWater()
                || !(event.getSource().getEntity() instanceof Player player)
                || event.getSource().getDirectEntity() != player
                || !player.getMainHandItem().is(ItemTags.SPEARS)) {
            return;
        }

        event.setNewDamage(fish.getHealth());
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof Squid squid
                && squid.getType() == EntityType.SQUID) {
            for (Player player : squid.level().getEntitiesOfClass(
                    Player.class,
                    squid.getBoundingBox().inflate(3.0),
                    nearbyPlayer -> nearbyPlayer.isAlive()
                            && nearbyPlayer.distanceToSqr(squid) <= 9.0)) {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200), squid);
            }
        }

        if (!(event.getEntity() instanceof Animal animal)) {
            return;
        }
        if (animal instanceof MushroomCow) {
            fleeNearbyAnimals(animal, MushroomCow.class);
        } else if (animal instanceof Cow) {
            fleeNearbyAnimals(animal, Cow.class);
        } else if (animal instanceof Pig) {
            fleeNearbyAnimals(animal, Pig.class);
        } else if (animal instanceof Chicken) {
            fleeNearbyAnimals(animal, Chicken.class);
        } else if (animal instanceof Sheep) {
            fleeNearbyAnimals(animal, Sheep.class);
        } else if (animal instanceof Rabbit) {
            fleeNearbyAnimals(animal, Rabbit.class);
        } else if (animal instanceof PolarBear) {
            fleeNearbyAnimals(animal, PolarBear.class);
        } else if (animal instanceof Hoglin) {
            fleeNearbyAnimals(animal, Hoglin.class);
        }
    }

    private <T extends Animal> void fleeNearbyAnimals(Animal hurtAnimal, Class<T> animalType) {
        for (T nearbyAnimal : hurtAnimal.level().getEntitiesOfClass(
                animalType,
                hurtAnimal.getBoundingBox().inflate(7.0),
                animal -> animal != hurtAnimal && animal.isAlive())) {
            Vec3 fleePosition = DefaultRandomPos.getPosAway(nearbyAnimal, 8, 4, hurtAnimal.position());
            if (fleePosition != null) {
                nearbyAnimal.getNavigation().moveTo(fleePosition.x, fleePosition.y, fleePosition.z, 1.5);
            }
        }
    }

    private void trackCampfire(ServerLevel level, BlockPos pos) {
        this.trackedCampfires.computeIfAbsent(level, ignored -> new HashSet<>()).add(pos.immutable());
    }

    private static boolean isWoodBlock(BlockState state) {
        return state.is(BlockTags.LOGS)
                || state.is(BlockTags.PLANKS)
                || state.is(BlockTags.WOODEN_BUTTONS)
                || state.is(BlockTags.WOODEN_DOORS)
                || state.is(BlockTags.WOODEN_STAIRS)
                || state.is(BlockTags.WOODEN_SLABS)
                || state.is(BlockTags.WOODEN_FENCES)
                || state.is(BlockTags.WOODEN_PRESSURE_PLATES)
                || state.is(BlockTags.WOODEN_SHELVES)
                || state.is(BlockTags.WOODEN_TRAPDOORS)
                || state.is(BlockTags.SIGNS)
                || state.is(BlockTags.ALL_HANGING_SIGNS);
    }

    private static void igniteBesideWood(
            ServerLevel level,
            BlockPos campfirePos,
            BlockPos woodPos,
            BlockState woodState) {
        for (Direction direction : Direction.values()) {
            if (!woodState.isFlammable(level, woodPos, direction)) {
                continue;
            }

            BlockPos firePos = woodPos.relative(direction);
            if (firePos.equals(campfirePos) || !level.getBlockState(firePos).isAir()) {
                continue;
            }

            var fireState = Blocks.FIRE.defaultBlockState();
            if (fireState.canSurvive(level, firePos)) {
                level.setBlock(firePos, fireState, 3);
                return;
            }
        }
    }
}
