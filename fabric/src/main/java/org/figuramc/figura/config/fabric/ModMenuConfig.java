package org.figuramc.figura.config.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
<<<<<<< HEAD:src/main/java/org/moon/figura/config/ModMenuConfig.java
import org.moon.figura.FiguraMod;
import org.moon.figura.gui.screens.ConfigScreen;
=======
import org.figuramc.figura.gui.screens.ConfigScreen;
import org.figuramc.figura.FiguraMod;
>>>>>>> 8b2c2606120aac4f05d8dd5820ea17317422dc93:fabric/src/main/java/org/figuramc/figura/config/fabric/ModMenuConfig.java

public class ModMenuConfig implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> new ConfigScreen(parentScreen, FiguraMod.DEBUG_MODE);
    }
}