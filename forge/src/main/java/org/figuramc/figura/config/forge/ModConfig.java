package org.figuramc.figura.config.forge;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import org.figuramc.figura.gui.screens.ConfigScreen;
import org.figuramc.figura.FiguraMod;

public class ModConfig {
    public static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(
                (client, parent) -> new ConfigScreen(parent, FiguraMod.DEBUG_MODE)));
    }
}
