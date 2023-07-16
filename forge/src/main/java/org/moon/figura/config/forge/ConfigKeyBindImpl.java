package org.moon.figura.config.forge;

import org.moon.figura.config.ConfigKeyBind;
import org.moon.figura.forge.FiguraModClientForge;

public class ConfigKeyBindImpl {
    public static void addKeyBind(ConfigKeyBind keyBind) {
        FiguraModClientForge.KEYBINDS.add(keyBind);
    }
}
