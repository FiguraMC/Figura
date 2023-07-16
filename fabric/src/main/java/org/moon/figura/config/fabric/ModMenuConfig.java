package org.moon.figura.config.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import org.moon.figura.FiguraMod;
import org.moon.figura.gui.screens.ConfigScreen;

public class ModMenuConfig implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> new ConfigScreen(parentScreen, FiguraMod.DEBUG_MODE);
    }
}