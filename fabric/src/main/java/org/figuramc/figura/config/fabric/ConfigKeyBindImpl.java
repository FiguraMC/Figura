package org.figuramc.figura.config.fabric;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.figuramc.figura.config.ConfigKeyBind;

public class ConfigKeyBindImpl {
    public static void addKeyBind(ConfigKeyBind keyBind) {
        KeyBindingHelper.registerKeyBinding(keyBind);
    }
}
