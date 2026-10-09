package com.canadiendragon.primitiveisolation;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
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
    }

    @SubscribeEvent
    static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(primitiveisolation.KNAPPING_STATION_MENU.get(), KnappingStationScreen::new);
        event.register(primitiveisolation.SALTING_RACK_MENU.get(), SaltingRackScreen::new);
        event.register(primitiveisolation.PRESERVING_BIN_MENU.get(), PreservingBinScreen::new);
    }
}
