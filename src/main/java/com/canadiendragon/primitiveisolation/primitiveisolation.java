package com.canadiendragon.primitiveisolation;

import java.util.List;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
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

    // Define mod id in a common place for everything to reference
    public static final String MODID = "primitiveisolation";
    private static final ToolMaterial FLINT_TOOL_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            ToolMaterial.WOOD.durability() / 2,
            ToolMaterial.STONE.speed(),
            ToolMaterial.STONE.attackDamageBonus(),
            ToolMaterial.STONE.enchantmentValue(),
            ToolMaterial.STONE.repairItems());
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
    public static final DeferredHolder<MenuType<?>, MenuType<KnappingStationMenu>> KNAPPING_STATION_MENU = MENU_TYPES.register(
            "knapping_station",
            () -> IMenuTypeExtension.create(KnappingStationMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<SaltingRackMenu>> SALTING_RACK_MENU = MENU_TYPES.register(
            "salting_rack",
            () -> IMenuTypeExtension.create(SaltingRackMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<PreservingBinMenu>> PRESERVING_BIN_MENU = MENU_TYPES.register(
            "preserving_bin",
            () -> IMenuTypeExtension.create(PreservingBinMenu::new));

    // Creates a new Block with the id "primitiveisolation:example_block", combining the namespace and path
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", p -> p.mapColor(MapColor.STONE));
    // Creates a new BlockItem with the id "primitiveisolation:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);
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
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChunkAnchorBlockEntity>> CHUNK_ANCHOR_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("chunk_anchor", () -> new BlockEntityType<>(
                    ChunkAnchorBlockEntity::new,
                    CHUNK_ANCHOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CharcoalPitBlockEntity>> CHARCOAL_PIT_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("charcoal_pit", () -> new BlockEntityType<>(
                    CharcoalPitBlockEntity::new,
                    CHARCOAL_PIT.get()));

    // Creates a new food item with the id "primitiveisolation:example_id", nutrition 1 and saturation 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", p -> p.food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    static final long FRESH_FOOD_SPOIL_TICKS = 20L * 60 * 20;
    private static final long SALTED_FOOD_SPOIL_TICKS = FRESH_FOOD_SPOIL_TICKS * 3 / 2;
    private static final long SMOKED_FOOD_SPOIL_TICKS = SALTED_FOOD_SPOIL_TICKS * 5;
    public static final DeferredItem<Item> SALT = ITEMS.registerSimpleItem("salt");
    public static final DeferredItem<Item> ICE_SHARD = ITEMS.registerSimpleItem("ice_shard");
    public static final DeferredItem<PerishableFoodItem> RAW_BEEF = ITEMS.registerItem(
            "raw_beef",
            properties -> new PerishableFoodItem(properties.food(foodProperties(3, 0.3F)), FRESH_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> RAW_PORK = ITEMS.registerItem(
            "raw_pork",
            properties -> new PerishableFoodItem(properties.food(foodProperties(3, 0.3F)), FRESH_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> RAW_CHICKEN = ITEMS.registerItem(
            "raw_chicken",
            properties -> new PerishableFoodItem(properties.food(foodProperties(2, 0.3F)), FRESH_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> SALTED_BEEF = ITEMS.registerItem(
            "salted_beef",
            properties -> new PerishableFoodItem(properties.food(foodProperties(3, 0.3F)), SALTED_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> SALTED_PORK = ITEMS.registerItem(
            "salted_pork",
            properties -> new PerishableFoodItem(properties.food(foodProperties(3, 0.3F)), SALTED_FOOD_SPOIL_TICKS));
    public static final DeferredItem<PerishableFoodItem> SALTED_CHICKEN = ITEMS.registerItem(
            "salted_chicken",
            properties -> new PerishableFoodItem(properties.food(foodProperties(2, 0.3F)), SALTED_FOOD_SPOIL_TICKS));
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
    public static final DeferredItem<PerishableFoodItem> RAW_COD = registerPerishableFish(
            "raw_cod", Foods.COD, FRESH_FOOD_SPOIL_TICKS);
    public static final DeferredItem<PerishableFoodItem> RAW_SALMON = registerPerishableFish(
            "raw_salmon", Foods.SALMON, FRESH_FOOD_SPOIL_TICKS);
    public static final DeferredItem<PerishableFoodItem> RAW_TROPICAL_FISH = registerPerishableFish(
            "raw_tropical_fish", Foods.TROPICAL_FISH, FRESH_FOOD_SPOIL_TICKS);
    public static final DeferredItem<PerishableFoodItem> RAW_PUFFERFISH = registerPerishablePufferfish(
            "raw_pufferfish", FRESH_FOOD_SPOIL_TICKS);
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
    public static final DeferredItem<FlintKnifeItem> FLINT_KNIFE = ITEMS.registerItem(
            "flint_knife",
            properties -> new FlintKnifeItem(properties.durability(29)));
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

    // Creates a creative tab with the id "primitiveisolation:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.primitiveisolation")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get());// Add the example item to the tab. For your own tabs, this method is preferred over the event
                output.accept(SALT.get());
                output.accept(ICE_SHARD.get());
                output.accept(SALT_DEPOSIT_ITEM.get());
                output.accept(CHARCOAL_PIT_ITEM.get());
                output.accept(CHUNK_ANCHOR_ITEM.get());
                output.accept(SALTING_RACK_ITEM.get());
                output.accept(PRESERVING_BIN_ITEM.get());
                output.accept(RAW_BEEF.get());
                output.accept(RAW_PORK.get());
                output.accept(RAW_CHICKEN.get());
                output.accept(SALTED_BEEF.get());
                output.accept(SALTED_PORK.get());
                output.accept(SALTED_CHICKEN.get());
                output.accept(SMOKED_BEEF.get());
                output.accept(SMOKED_PORK.get());
                output.accept(SMOKED_CHICKEN.get());
                output.accept(RAW_COD.get());
                output.accept(RAW_SALMON.get());
                output.accept(RAW_TROPICAL_FISH.get());
                output.accept(RAW_PUFFERFISH.get());
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

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
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
        event.put(COW_CORPSE.get(), Animal.createAnimalAttributes().build());
        event.put(PIG_CORPSE.get(), Pig.createAttributes().build());
        event.put(CHICKEN_CORPSE.get(), Chicken.createAttributes().build());
    }

    static List<ItemStack> cowButcheredDrops() {
        return List.of(new ItemStack(RAW_BEEF.get(), 3), new ItemStack(Items.LEATHER), new ItemStack(Items.BONE));
    }

    static List<ItemStack> pigButcheredDrops() {
        return List.of(new ItemStack(RAW_PORK.get(), 3), new ItemStack(Items.LEATHER), new ItemStack(Items.BONE));
    }

    static List<ItemStack> chickenButcheredDrops() {
        return List.of(new ItemStack(RAW_CHICKEN.get(), 2), new ItemStack(Items.FEATHER), new ItemStack(Items.BONE));
    }

    private static FoodProperties foodProperties(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
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

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onCampfirePlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getPlacedBlock().is(BlockTags.CAMPFIRES)
                && event.getLevel() instanceof ServerLevel level) {
            this.trackCampfire(level, event.getPos());
        }
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
            if (!CampfireBlock.isLitCampfire(campfireState)) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos woodPos = campfirePos.relative(direction);
                var woodState = level.getBlockState(woodPos);
                if (isWoodBlock(woodState) && level.getRandom().nextInt(60) == 0) {
                    igniteBesideWood(level, campfirePos, woodPos, woodState);
                }
            }
        }

        if (positions.isEmpty()) {
            this.trackedCampfires.remove(level);
        }
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

        if (!(event.getEntity() instanceof Zombie zombie)) {
            return;
        }
        zombie.goalSelector.addGoal(5, new ZombieEatMeatGoal(zombie));
        zombie.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(
                zombie, Cow.class, true, (cow, level) -> cow.getType() != COW_CORPSE.get()));
        zombie.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(
                zombie, Pig.class, true, (pig, level) -> pig.getType() != PIG_CORPSE.get()));
        zombie.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(
                zombie, Chicken.class, true, (chicken, level) -> chicken.getType() != CHICKEN_CORPSE.get()));
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

        if (!(event.getEntity() instanceof Animal animal)) {
            return;
        }
        if (animal instanceof Cow cow && cow.getType() == EntityType.COW) {
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
        }
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
        if (event.getEntity().level().isClientSide() || !(event.getEntity() instanceof Animal animal)) {
            return;
        }

        if (animal instanceof Cow) {
            fleeNearbyAnimals(animal, Cow.class);
        } else if (animal instanceof Pig) {
            fleeNearbyAnimals(animal, Pig.class);
        } else if (animal instanceof Chicken) {
            fleeNearbyAnimals(animal, Chicken.class);
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
