package org.figuramc.figura.config.forge;

import org.figuramc.figura.config.ConfigKeyBind;
import org.figuramc.figura.forge.FiguraModClientForge;

public class ConfigKeyBindImpl {
    public static void addKeyBind(ConfigKeyBind keyBind) {
        FiguraModClientForge.KEYBINDS.add(keyBind);
    }
}
