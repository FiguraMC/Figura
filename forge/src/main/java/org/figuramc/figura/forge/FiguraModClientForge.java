package org.figuramc.figura.forge;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.ConfigManager;
import org.figuramc.figura.config.forge.ModConfig;
import org.figuramc.figura.gui.forge.GuiOverlay;
import org.figuramc.figura.gui.forge.GuiUnderlay;
import org.figuramc.figura.utils.forge.FiguraResourceListenerImpl;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = FiguraMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FiguraModClientForge extends FiguraMod {
    // keybinds stored here
    public static List<KeyMapping> KEYBINDS = new ArrayList<>();

    @SubscribeEvent
    public static void onInitializeClient(FMLClientSetupEvent event) {
        onClientInit();
        ModConfig.registerConfigScreen();
        vanillaOverlays.add(ForgeIngameGui.VIGNETTE_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.SPYGLASS_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.HELMET_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.FROSTBITE_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.PORTAL_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.HOTBAR_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.CROSSHAIR_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.BOSS_HEALTH_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.PLAYER_HEALTH_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.ARMOR_LEVEL_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.MOUNT_HEALTH_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.FOOD_LEVEL_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.AIR_LEVEL_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.JUMP_BAR_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.EXPERIENCE_BAR_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.ITEM_NAME_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.SLEEP_FADE_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.HUD_TEXT_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.FPS_GRAPH_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.POTION_ICONS_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.RECORD_OVERLAY_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.SUBTITLES_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.TITLE_TEXT_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.SCOREBOARD_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.CHAT_PANEL_ELEMENT);
        vanillaOverlays.add(ForgeIngameGui.PLAYER_LIST_ELEMENT);
    }

    @SubscribeEvent
    public static void registerResourceListener(RegisterClientReloadListenersEvent event) {
        getResourceListeners().forEach(figuraResourceListener -> event.registerReloadListener((FiguraResourceListenerImpl)figuraResourceListener));
    }

    @SubscribeEvent
    public static void registerOverlays(FMLClientSetupEvent event) {
        OverlayRegistry.registerOverlayTop("figura_overlay", new GuiOverlay());
        OverlayRegistry.registerOverlayBottom("figura_underlay", new GuiUnderlay());
    }

    private static final List<IIngameOverlay> vanillaOverlays = new ArrayList<>();

    public static void cancelVanillaOverlays(RenderGameOverlayEvent.PreLayer event) {
        if (vanillaOverlays.contains(event.getOverlay())) {
            Entity entity = Minecraft.getInstance().getCameraEntity();
            Avatar avatar = entity == null ? null : AvatarManager.getAvatar(entity);
            if (avatar != null && avatar.luaRuntime != null && !avatar.luaRuntime.renderer.renderHUD) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void registerKeyBinding(FMLClientSetupEvent event) {
        // Config has to be initialized here, so that the keybinds exist on time
        ConfigManager.init();
        for (KeyMapping value : KEYBINDS) {
            if(value != null)
                ClientRegistry.registerKeyBinding(value);
        }
    }
}
