package org.figuramc.figura.config.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import org.figuramc.figura.gui.screens.ConfigScreen;
import org.figuramc.figura.FiguraMod;

public class ModMenuConfig implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> new ConfigScreen(parentScreen, FiguraMod.DEBUG_MODE);
    }
}