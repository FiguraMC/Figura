package org.moon.figura.config;

import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
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


    NameTag,

    PREVIEW_NAMEPLATE(false),
    NAMEPLATE_MODIFICATIONS(true),
    CHAT_MODIFICATIONS(true),
    PLAYERLIST_MODIFICATIONS(true),
    BADGES(true),

    Misc,

    FIGURA_BUTTON_LOCATION(4, 5),
    SCRIPT_LOG_LOCATION(0, 3) {{
        String path = "figura.config.log_location.";
        this.enumList = List.of(
                new TranslatableText(path + "1"),
                new TranslatableText(path + "2"),
                new TranslatableText(path + "3")
        );
    }},
    PLAYER_POPUP_BUTTON("key.keyboard.r", "figura"),
    ACCENT_COLOR(0x55FFFF, InputType.HEX_COLOR),
    RELEASE_CHANNEL(0, 3),
    CUSTOM_PLAYER_HEADS(true),
    FIX_HANDS(true),
    EASTER_EGGS(true),
    MAX_UI_AVATARS(50, InputType.INT),

    ActionWheel,

    ACTION_WHEEL_BUTTON("key.keyboard.b", "figura"),
    ACTION_WHEEL_TITLE_POS(0, 4),
    ACTION_WHEEL_EXECUTE_ON_CLOSE(true),
    //NEW_ACTION_WHEEL(false),

    Dev {{this.name = new TranslatableText("figura.config.dev").formatted(Formatting.RED);}},

    USE_LOCAL_SERVER(false),
    FORMAT_SCRIPT_ON_UPLOAD(true),
    LOG_OTHERS_SCRIPT(false),
    RENDER_DEBUG_PARTS_PIVOT(true) {{
        String tooltip = "figura.config.render_debug_parts_pivot.tooltip";
        this.tooltip = new TranslatableText(tooltip,
                new TranslatableText(tooltip + ".cubes").setStyle(Style.EMPTY.withColor(0xff72b7)),
                new TranslatableText(tooltip + ".groups").setStyle(Style.EMPTY.withColor(0xaff2ff)));
    }},
    RENDER_OWN_NAMEPLATE(false),
    MODEL_FOLDER_PATH("", InputType.FOLDER_PATH),
    PANIC_BUTTON("key.keyboard.unknown", "figura"),
    PINGS_LOG_LOCATION(3, 4) {{
        String path = "figura.config.log_location.";
        this.enumList = List.of(
                new TranslatableText(path + "1"),
                new TranslatableText(path + "2"),
                new TranslatableText(path + "3"),
                new TranslatableText(path + "4")
        );
    }},
    BACKEND_PATH("figura.f24.im", InputType.ANY);


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
    public Text name;
    public Text tooltip;
    public final ConfigType type;

    //special properties
    public List<Text> enumList;
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
        this.keyBind = new ConfigKeyBind(this.name.getString(), InputUtil.fromTranslationKey(key), category, this);
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
        this.name = new TranslatableText(name);
        this.tooltip = new TranslatableText(name + ".tooltip");

        //generate enum list
        if (length != null) {
            ArrayList<Text> enumList = new ArrayList<>();
            for (int i = 1; i <= length; i++)
                enumList.add(new TranslatableText(name + "." + i));
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
        public final Text hint;
        InputType(Predicate<String> predicate) {
            this.validator = predicate;
            this.hint = new TranslatableText(FiguraMod.MOD_ID + ".config.input." + this.name().toLowerCase());
        }
    }

    public static class ConfigKeyBind extends KeyBinding {
        private final Config config;

        public ConfigKeyBind(String translationKey, InputUtil.Key key, String category, Config config) {
            super(translationKey, key.getCategory(), key.getCode(), category);
            this.config = config;
            KeyBindingRegistryImpl.registerKeyBinding(this);
        }

        @Override
        public void setBoundKey(InputUtil.Key boundKey) {
            super.setBoundKey(boundKey);

            config.setValue(this.getBoundKeyTranslationKey());
            ConfigManager.saveConfig();

            GameOptions options = MinecraftClient.getInstance().options;
            if (options != null) options.write();
            KeyBinding.updateKeysByCode();
        }
    }
}