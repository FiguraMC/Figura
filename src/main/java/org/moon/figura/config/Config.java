package org.moon.figura.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.backend2.NetworkStuff;
import org.moon.figura.lua.FiguraLuaPrinter;
import org.moon.figura.trust.Trust;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;

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

    Nameplate,
    SELF_NAMEPLATE(false),
    PREVIEW_NAMEPLATE(true),
    CHAT_NAMEPLATE(2, 3) {{
        String path = "config.nameplate_level";
        this.enumList = List.of(
                FiguraText.of(path + ".1"),
                FiguraText.of(path + ".2"),
                FiguraText.of(path + ".3")
        );
    }},
    ENTITY_NAMEPLATE(2, 3) {{
        String path = "config.nameplate_level";
        this.enumList = List.of(
                FiguraText.of(path + ".1"),
                FiguraText.of(path + ".2"),
                FiguraText.of(path + ".3")
        );
    }},
    LIST_NAMEPLATE(2, 3) {{
        String path = "config.nameplate_level";
        this.enumList = List.of(
                FiguraText.of(path + ".1"),
                FiguraText.of(path + ".2"),
                FiguraText.of(path + ".3")
        );
    }},

    Script,
    LOG_LOCATION(0, 2),
    ALLOW_FP_HANDS(false),
    LOG_NUMBER_LENGTH(5, InputType.POSITIVE_INT) {
        @Override
        public void onChange() {
            super.onChange();
            FiguraLuaPrinter.updateDecimalFormatting();
        }
    },
    FORMAT_SCRIPT(1, 3) {{
      String tooltip = "config.format_script.tooltip.";
      this.tooltip = FiguraText.of(tooltip + "1")
              .append("\n")
              .append(FiguraText.of(tooltip + "2").withStyle(ChatFormatting.RED));
    }
        @Override
        public void onChange() {
            if (!AvatarManager.localUploaded)
                AvatarManager.reloadAvatar(FiguraMod.getLocalPlayerUUID());
        }
    },

    ActionWheel,
    ACTION_WHEEL_BUTTON("key.keyboard.b"),
    ACTION_WHEEL_MODE(0, 4),
    ACTION_WHEEL_SCALE(1f, InputType.FLOAT),
    ACTION_WHEEL_TITLE(0, 5),
    ACTION_WHEEL_DECORATIONS(true),

    UI,
    BACKGROUND_SCROLL_SPEED(1f, InputType.FLOAT),
    POPUP_SCALE(1f, InputType.FLOAT),
    POPUP_MIN_SIZE(1f, InputType.FLOAT),
    POPUP_MAX_SIZE(6f, InputType.FLOAT),
    AVATAR_PORTRAITS(false) {{
        this.disabled = true;
    }},
    FIGURA_INVENTORY(true),
    TOAST_TIME(5f, InputType.FLOAT),
    TOAST_TITLE_TIME(2f, InputType.FLOAT),
    WARDROBE_FILE_NAMES(false),

    Paperdoll,
    HAS_PAPERDOLL(false),
    PAPERDOLL_ALWAYS_ON(false),
    FIRST_PERSON_PAPERDOLL(true),
    PAPERDOLL_SCALE(1f, InputType.FLOAT),
    PAPERDOLL_X(0f, InputType.FLOAT),
    PAPERDOLL_Y(0f, InputType.FLOAT),
    PAPERDOLL_PITCH(0f, InputType.FLOAT),
    PAPERDOLL_YAW(20f, InputType.FLOAT),

    Misc,
    POPUP_BUTTON("key.keyboard.r"),
    RELOAD_BUTTON("key.keyboard.unknown"),
    PANIC_BUTTON("key.keyboard.unknown"),
    WARDROBE_BUTTON("key.keyboard.unknown"),
    BUTTON_LOCATION(0, 5),
    UPDATE_CHANNEL(1, 3) {
        @Override
        public void onChange() {
            super.onChange();
            NetworkStuff.checkVersion();
        }
    },
    DEFAULT_TRUST(1, Trust.Group.values().length - 1) {{
        List<Component> list = new ArrayList<>();
        Trust.Group[] groups = Trust.Group.values();
        for (int i = 0; i < groups.length - 1; i++)
            list.add(groups[i].text.copy());
        this.enumList = list;
    }
        @Override
        public void onChange() {
            super.onChange();
            TrustManager.saveToDisk();
        }
    },
    CHAT_EMOJIS(false),
    EASTER_EGGS(true),

    Dev {{this.name = this.name.copy().withStyle(ChatFormatting.RED);}},
    CONNECTION_TOASTS(true),
    RENDER_DEBUG_PARTS_PIVOT(1, 3) {{
        String tooltip = "config.render_debug_parts_pivot.tooltip";
        this.tooltip = FiguraText.of(tooltip,
                FiguraText.of(tooltip + ".cubes").setStyle(ColorUtils.Colors.FRAN_PINK.style),
                FiguraText.of(tooltip + ".groups").setStyle(ColorUtils.Colors.MAYA_BLUE.style));
    }},
    FIRST_PERSON_MATRICES(true),
    LOG_OTHERS(false),
    LOG_PINGS(0, 3),
    SYNC_PINGS(false) {{
        String tooltip = "config.sync_pings.tooltip.";
        this.tooltip = FiguraText.of(tooltip + "1")
                .append("\n")
                .append(FiguraText.of(tooltip + "2").withStyle(ChatFormatting.RED));
    }},
    CHAT_MESSAGES(false) {{
        this.name = this.name.copy().withStyle(ChatFormatting.RED);
        String tooltip = "config.chat_messages.tooltip.";
        this.tooltip = FiguraText.of(tooltip + "1")
                .append("\n\n")
                .append(FiguraText.of(tooltip + "2").withStyle(ChatFormatting.RED))
                .append("\n\n")
                .append(FiguraText.of(tooltip + "3").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
    }},
    MAIN_DIR("", InputType.FOLDER_PATH),
    SERVER_IP("figura.moonlight-devs.org:25565", InputType.IP) {
        @Override
        public void onChange() {
            super.onChange();
            NetworkStuff.reAuth();
        }
    };


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
     * why not ? lol
     * *stabs*
     */


    //values
    public Object value;
    public Object tempValue; //settings screen "undo"
    public final Object defaultValue;

    //metadata
    public Component name;
    public Component tooltip;
    public final ConfigType type;

    //special properties
    public List<Component> enumList;
    public ConfigKeyBind keyBind;
    public final InputType inputType;
    public boolean disabled;

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
    Config(String key) {
        this(ConfigType.KEYBIND, key, null, null, null);
        this.keyBind = new ConfigKeyBind(this.name.getString(), InputConstants.getKey(key), this);
    }

    //global constructor
    Config(ConfigType type, Object value, Integer length, ConfigKeyBind keyBind, InputType inputType) {
        //set values
        this.type = type;
        this.value = value;
        this.defaultValue = value;
        this.tempValue = value;
        this.keyBind = keyBind;
        this.inputType = inputType;

        //generate names
        String name = "config." + this.name().toLowerCase();
        this.name = FiguraText.of(name);
        this.tooltip = FiguraText.of(name + ".tooltip");

        //generate enum list
        if (length != null) {
            ArrayList<Component> enumList = new ArrayList<>();
            for (int i = 1; i <= length; i++)
                enumList.add(FiguraText.of(name + "." + i));
            this.enumList = enumList;
        }
    }

    public void setValue(String text) {
        boolean change = !value.equals(tempValue);

        try {
            if (enumList != null)
                value = Math.floorMod(Integer.parseInt(text), enumList.size());
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
            else
                value = text;
        } catch (Exception e) {
            FiguraMod.LOGGER.warn("Failed to set this config (" + this.name() + ") value \"" + value + "\", restoring it to default", e);
            value = defaultValue;
            change = true;
        }

        tempValue = value;
        if (change) {
            try {
                onChange();
            } catch (Exception e) {
                FiguraMod.LOGGER.warn("Failed to run onChange for this config (" + this.name() + ")", e);
            }
        }
    }

    public void onChange() {}

    public enum ConfigType {
        CATEGORY,
        BOOLEAN,
        ENUM,
        INPUT,
        KEYBIND
    }

    public float asFloat() {
        return (float) value;
    }

    public int asInt() {
        return (int) value;
    }

    public boolean asBool() {
        return (boolean) value;
    }

    public String asString() {
        return (String) value;
    }

    public enum InputType {
        ANY(s -> true),
        INT(s -> {
            try {
                Integer.parseInt(s);
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }),
        POSITIVE_INT(s -> {
            try {
                Integer i = Integer.parseInt(s);
                return i >= 0;
            } catch (Exception ignored) {
                return false;
            }
        }),
        FLOAT(s -> {
            try {
                Float f = Float.parseFloat(s);
                return !f.isInfinite();
            } catch (Exception ignored) {
                return false;
            }
        }),
        HEX_COLOR(s -> ColorUtils.userInputHex(s, null) != null),
        FOLDER_PATH(s -> {
            try {
                return s.isBlank() || Path.of(s.trim()).toFile().isDirectory();
            } catch (Exception ignored) {
                return false;
            }
        }),
        IP(ServerAddress::isValidAddress);

        public final Predicate<String> validator;
        public final Component hint;
        InputType(Predicate<String> predicate) {
            this.validator = predicate;
            this.hint = FiguraText.of("config.input." + this.name().toLowerCase());
        }
    }

    public static class ConfigKeyBind extends KeyMapping {
        private final Config config;

        public ConfigKeyBind(String translationKey, InputConstants.Key key, Config config) {
            super(translationKey, key.getType(), key.getValue(), FiguraMod.MOD_ID);
            this.config = config;

            if (FiguraMod.DEBUG_MODE || !config.disabled)
                KeyBindingRegistryImpl.registerKeyBinding(this);
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