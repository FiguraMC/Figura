package org.figuramc.figura.config.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.screens.ConfigScreen;

public class ModMenuConfig implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> new ConfigScreen(parentScreen, FiguraMod.debugModeEnabled());
    }
}