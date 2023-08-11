package org.figuramc.figura.config;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.figuramc.figura.FiguraMod;

public class ConfigKeyBind extends KeyMapping {

    private final ConfigType.KeybindConfig config;

    public ConfigKeyBind(String translationKey, InputConstants.Key key, ConfigType.KeybindConfig config) {
        super(translationKey, key.getType(), key.getValue(), FiguraMod.MOD_ID);
        this.config = config;

        if (FiguraMod.debugModeEnabled() || !config.disabled)
           addKeyBind(this);
    }

    @Override
    public void setKey(InputConstants.Key boundKey) {
        super.setKey(boundKey);

        config.value = config.tempValue = this.saveString();
        ConfigManager.saveConfig();

        Options options = Minecraft.getInstance().options;
        if (options != null) options.save();

        KeyMapping.resetMapping();
    }

    @ExpectPlatform
    public static void addKeyBind(ConfigKeyBind keyBind) {}
}