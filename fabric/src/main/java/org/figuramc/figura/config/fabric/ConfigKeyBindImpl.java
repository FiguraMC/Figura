package org.figuramc.figura.config.fabric;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import org.figuramc.figura.config.ConfigKeyBind;

public class ConfigKeyBindImpl {
    public static void addKeyBind(ConfigKeyBind keyBind) {
        KeyMappingHelper.registerKeyMapping(keyBind);
    }
}
