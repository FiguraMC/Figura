package org.figuramc.figura.config.neoforge;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.gui.screens.ConfigScreen;

public class ModConfig {
    public static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(
                (client, parent) -> new ConfigScreen(parent, FiguraMod.debugModeEnabled())));
    }
}
