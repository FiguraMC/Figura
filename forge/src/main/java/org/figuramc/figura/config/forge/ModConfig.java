package org.figuramc.figura.config.forge;

import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.gui.screens.ConfigScreen;

public class ModConfig {
    public static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () ->
                (client, parent) -> new ConfigScreen(parent, FiguraMod.debugModeEnabled()));
    }
}
