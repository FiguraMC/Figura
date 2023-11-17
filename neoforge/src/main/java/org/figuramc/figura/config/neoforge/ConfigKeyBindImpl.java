package org.figuramc.figura.config.neoforge;

import org.figuramc.figura.config.ConfigKeyBind;
import org.figuramc.figura.neoforge.FiguraModClientNeoForge;

public class ConfigKeyBindImpl {
    public static void addKeyBind(ConfigKeyBind keyBind) {
        FiguraModClientNeoForge.KEYBINDS.add(keyBind);
    }
}
