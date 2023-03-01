package org.moon.figura.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.moon.figura.FiguraMod;

public class ConfigKeyBind extends KeyMapping {

    private final ConfigType.KeybindConfig config;

    public ConfigKeyBind(String translationKey, InputConstants.Key key, ConfigType.KeybindConfig config) {
        super(translationKey, key.getType(), key.getValue(), FiguraMod.MOD_ID);
        this.config = config;

        if (FiguraMod.DEBUG_MODE || !config.disabled)
            KeyBindingHelper.registerKeyBinding(this);
    }

    @Override
    public void setKey(InputConstants.Key boundKey) {
        super.setKey(boundKey);

        config.value = this.saveString();
        ConfigManager.saveConfig();

        Options options = Minecraft.getInstance().options;
        if (options != null) options.save();
        KeyMapping.resetMapping();
    }
}