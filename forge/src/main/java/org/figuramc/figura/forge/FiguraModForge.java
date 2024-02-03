package org.figuramc.figura.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.figuramc.figura.FiguraMod;

@Mod("figura")
public class FiguraModForge {
    // dummy empty mod class, we are client only
    public FiguraModForge() {
        MinecraftForge.EVENT_BUS.addListener(FiguraModClientForge::cancelVanillaOverlays);
        MinecraftForge.EVENT_BUS.addListener(FiguraModClientForge::renderOverlay);
        MinecraftForge.EVENT_BUS.addListener(FiguraModClientForge::renderUnderlay);
        if (FMLEnvironment.dist == Dist.CLIENT)
            FiguraModClientForge.registerResourceListeners();
    }
}
