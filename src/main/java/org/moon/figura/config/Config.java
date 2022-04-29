package org.moon.figura.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.moon.figura.FiguraMod;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public enum Config {

    /**
     * Config Here!!!1!
     * (order matters)
     */


    Script,
    LOG_LOCATION(0, 2),

    ActionWheel,
    ACTION_WHEEL_BUTTON("key.keyboard.b", FiguraMod.MOD_ID),

    Dev {{this.name = new TranslatableComponent("figura.config.dev").withStyle(ChatFormatting.RED);}},
    PANIC_BUTTON("key.keyboard.unknown", FiguraMod.MOD_ID),
    LOG_PINGS(0, 3);


    /**
     * Static Properties
     */


    //mod config version
    //only change this if you rename old configs
    public static final int CONFIG_VERSION = 1;

    //config update hashmap; <version number, <actual config, old config name>>
    public static final HashMap<Integer, HashMap<Config, String>> CONFIG_UPDATES = new HashMap<>();


    /**
     * do not edit below this line :p
     */


    //values
    public Object value;
    public Object configValue;
    public final Object defaultValue;

    //metadata
    public Component name;
    public Component tooltip;
    public final ConfigType type;

    //special properties
    public List<Component> enumList;
    public ConfigKeyBind keyBind;
    public final InputType inputType;

    //type constructors
    Config() {
        this(ConfigType.CATEGORY, null, null, null, null);
    }
    Config(boolean defaultValue) {
        this(ConfigType.BOOLEAN, defaultValue, null, null, null);
    }
    Config(int defaultValue, Integer length) {
        this(ConfigType.ENUM, defaultValue, length, null, null);
    }
    Config(Object defaultValue, InputType inputType) {
        this(ConfigType.INPUT, defaultValue, null, null, inputType);
    }
    Config(String key, String category) {
        this(ConfigType.KEYBIND, key, null, null, null);
        this.keyBind = new ConfigKeyBind(this.name.getString(), InputConstants.getKey(key), category, this);
    }

    //global constructor
    Config(ConfigType type, Object value, Integer length, ConfigKeyBind keyBind, InputType inputType) {
        //set values
        this.type = type;
        this.value = value;
        this.defaultValue = value;
        this.configValue = value;
        this.keyBind = keyBind;
        this.inputType = inputType;

        //generate names
        String name = FiguraMod.MOD_ID + ".config." + this.name().toLowerCase();
        this.name = new TranslatableComponent(name);
        this.tooltip = new TranslatableComponent(name + ".tooltip");

        //generate enum list
        if (length != null) {
            ArrayList<Component> enumList = new ArrayList<>();
            for (int i = 1; i <= length; i++)
                enumList.add(new TranslatableComponent(name + "." + i));
            this.enumList = enumList;
        }
    }

    public void setValue(String text) {
        boolean change = value.equals(configValue);

        try {
            if (value instanceof String)
                value = text;
            else if (value instanceof Boolean)
                value = Boolean.valueOf(text);
            else if (value instanceof Integer)
                value = Integer.valueOf(text);
            else if (value instanceof Float)
                value = Float.valueOf(text);
            else if (value instanceof Long)
                value = Long.valueOf(text);
            else if (value instanceof Double)
                value = Double.valueOf(text);
            else if (value instanceof Byte)
                value = Byte.valueOf(text);
            else if (value instanceof Short)
                value = Short.valueOf(text);

            if (enumList != null) {
                int length = enumList.size();
                value = ((Integer.parseInt(text) % length) + length) % length;
            }
        } catch (Exception e) {
            value = defaultValue;
        }

        configValue = value;
        if (change) runOnChange();
    }

    public void runOnChange() {}

    public enum ConfigType {
        CATEGORY,
        BOOLEAN,
        ENUM,
        INPUT,
        KEYBIND
    }

    public enum InputType {
        ANY(s -> true),
        INT(s -> s.matches("^[\\-+]?[0-9]*$")),
        FLOAT(s -> s.matches("[\\-+]?[0-9]*(\\.[0-9]+)?") || s.endsWith(".") || s.isEmpty()),
        HEX_COLOR(s -> s.matches("^[#]?[0-9A-Fa-f]{0,6}$")),
        FOLDER_PATH(s -> {
            if (!s.isBlank()) {
                try {
                    return Path.of(s.trim()).toFile().isDirectory();
                } catch (Exception ignored) {
                    return false;
                }
            }

            return true;
        });

        public final Predicate<String> validator;
        public final Component hint;
        InputType(Predicate<String> predicate) {
            this.validator = predicate;
            this.hint = new TranslatableComponent(FiguraMod.MOD_ID + ".config.input." + this.name().toLowerCase());
        }
    }

    public static class ConfigKeyBind extends KeyMapping {
        private final Config config;

        public ConfigKeyBind(String translationKey, InputConstants.Key key, String category, Config config) {
            super(translationKey, key.getType(), key.getValue(), category);
            this.config = config;
            KeyMappingRegistry.registerKeyMapping(this);
        }

        @Override
        public void setKey(InputConstants.Key boundKey) {
            super.setKey(boundKey);

            config.setValue(this.saveString());
            ConfigManager.saveConfig();

            Options options = Minecraft.getInstance().options;
            if (options != null) options.save();
            KeyMapping.resetMapping();
        }
    }
}