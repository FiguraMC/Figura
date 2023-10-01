package org.figuramc.figura.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod("figura")
public class FiguraModForge {
    // dummy empty mod class, we are client only
    public FiguraModForge() {
        MinecraftForge.EVENT_BUS.addListener(FiguraModClientForge::cancelVanillaOverlays);
    }
}
