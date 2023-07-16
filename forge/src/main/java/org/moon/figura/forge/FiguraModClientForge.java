package org.moon.figura.forge;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.Configs;
import org.moon.figura.config.forge.ModConfig;
import org.moon.figura.gui.forge.GuiOverlay;
import org.moon.figura.gui.forge.GuiUnderlay;
import org.moon.figura.utils.forge.FiguraResourceListenerImpl;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = FiguraMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FiguraModClientForge extends FiguraMod {
    //keybinds stored here
    public static List<KeyMapping> KEYBINDS = new ArrayList<>();

    @SubscribeEvent
    public static void onInitializeClient(FMLClientSetupEvent event) {
        onClientInit();
        ModConfig.registerConfigScreen();
    }

    @SubscribeEvent
    public static void registerResourceListener(RegisterClientReloadListenersEvent event) {
        getResourceListeners().forEach(figuraResourceListener -> event.registerReloadListener((FiguraResourceListenerImpl)figuraResourceListener));
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("figura_overlay", new GuiOverlay());
        event.registerBelowAll("figura_underlay", new GuiUnderlay());
    }

    @SubscribeEvent
    public static void registerKeyBinding(RegisterKeyMappingsEvent event) {
        for (KeyMapping value : KEYBINDS) {
            if(value != null)
                event.register(value);
        }
    }
}
