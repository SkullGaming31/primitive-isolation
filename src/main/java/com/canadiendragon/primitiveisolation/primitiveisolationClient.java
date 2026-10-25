package com.canadiendragon.primitiveisolation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = primitiveisolation.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = primitiveisolation.MODID, value = Dist.CLIENT)
public class primitiveisolationClient {
    public primitiveisolationClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    public static void openFieldGuide(ItemStack guide) {
        Minecraft.getInstance().setScreen(new FieldGuideScreen(guide));
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        primitiveisolation.LOGGER.info("HELLO FROM CLIENT SETUP");
        primitiveisolation.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(primitiveisolation.COW_CORPSE.get(), CowCorpseRenderer::new);
        event.registerEntityRenderer(primitiveisolation.PIG_CORPSE.get(), PigCorpseRenderer::new);
        event.registerEntityRenderer(primitiveisolation.CHICKEN_CORPSE.get(), ChickenCorpseRenderer::new);
        event.registerEntityRenderer(primitiveisolation.MOOSHROOM_CORPSE.get(), MooshroomCorpseRenderer::new);
        event.registerEntityRenderer(primitiveisolation.SHEEP_CORPSE.get(), SheepCorpseRenderer::new);
        event.registerEntityRenderer(primitiveisolation.RABBIT_CORPSE.get(), RabbitCorpseRenderer::new);
        event.registerEntityRenderer(primitiveisolation.POLAR_BEAR_CORPSE.get(), PolarBearCorpseRenderer::new);
        event.registerEntityRenderer(primitiveisolation.HOGLIN_CORPSE.get(), HoglinCorpseRenderer::new);
        event.registerEntityRenderer(primitiveisolation.SPIDER_WEB_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(primitiveisolation.SQUID_INK_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(primitiveisolation.DEER.get(), DeerRenderer::new);
        event.registerEntityRenderer(primitiveisolation.MOOSE.get(), MooseRenderer::new);
        event.registerEntityRenderer(
                primitiveisolation.PIRANHA.get(),
                context -> new WaterPredatorRenderer<Piranha>(
                        context,
                        WaterPredatorModel.PIRANHA_LAYER,
                        Identifier.fromNamespaceAndPath(primitiveisolation.MODID, "textures/entity/piranha.png"),
                        0.25F,
                        1.0F));
        event.registerEntityRenderer(
                primitiveisolation.SHARK.get(),
                context -> new WaterPredatorRenderer<Shark>(
                        context,
                        WaterPredatorModel.SHARK_LAYER,
                        Identifier.fromNamespaceAndPath(primitiveisolation.MODID, "textures/entity/shark.png"),
                        0.75F,
                        1.4F));
        event.registerEntityRenderer(primitiveisolation.DEER_CORPSE.get(), DeerCorpseRenderer::new);
        event.registerEntityRenderer(primitiveisolation.MOOSE_CORPSE.get(), MooseCorpseRenderer::new);
        event.registerBlockEntityRenderer(
                primitiveisolation.BUTCHERY_TABLE_BLOCK_ENTITY.get(),
                ButcheryTableRenderer::new);
    }

    @SubscribeEvent
    static void registerWildlifeLayers(RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WildlifeModel.DEER_LAYER, WildlifeModel::createDeerLayer);
        event.registerLayerDefinition(WildlifeModel.MOOSE_LAYER, WildlifeModel::createMooseLayer);
        event.registerLayerDefinition(WaterPredatorModel.PIRANHA_LAYER, WaterPredatorModel::createPiranhaLayer);
        event.registerLayerDefinition(WaterPredatorModel.SHARK_LAYER, WaterPredatorModel::createSharkLayer);
    }

    @SubscribeEvent
    static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(primitiveisolation.KNAPPING_STATION_MENU.get(), KnappingStationScreen::new);
        event.register(primitiveisolation.SALTING_RACK_MENU.get(), SaltingRackScreen::new);
        event.register(primitiveisolation.DRYING_RACK_MENU.get(), DryingRackScreen::new);
        event.register(primitiveisolation.PLANT_DRYER_MENU.get(), PlantDryerScreen::new);
        event.register(primitiveisolation.PRESERVING_BIN_MENU.get(), PreservingBinScreen::new);
        event.register(primitiveisolation.BASKET_MENU.get(), BasketScreen::new);
    }
}
