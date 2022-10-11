package org.moon.figura.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.loader.api.FabricLoader;
import org.moon.figura.FiguraMod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ConfigManager {

    private static final File FILE = new File(FabricLoader.getInstance().getConfigDir().resolve(FiguraMod.MOD_ID + ".json").toString());
    private static final List<Config> CONFIG_ENTRIES = new ArrayList<>() {{
        for (Config value : Config.values()) {
            if (value.type != Config.ConfigType.CATEGORY)
                this.add(value);
        }
    }};

    public static void init() {
        loadConfig();
        saveConfig();
    }

    public static void loadConfig() {
        try {
            if (FILE.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(FILE));
                JsonObject json = JsonParser.parseReader(br).getAsJsonObject();

                JsonElement version = json.get("CONFIG_VERSION");
                if (version != null && version.getAsInt() != Config.CONFIG_VERSION) {
                    update(json, version.getAsInt());
                } else {
                    for (Config config : CONFIG_ENTRIES) {
                        JsonElement object = json.get(config.name().toLowerCase());
                        if (object == null)
                            continue;

                        String obj = object.getAsString();
                        switch (config.type) {
                            case KEYBIND -> config.keyBind.setKey(InputConstants.getKey(obj));
                            case INPUT -> {
                                if (config.inputType.validator.test(obj))
                                    config.setValue(obj);
                            }
                            default -> config.setValue(obj);
                        }
                    }
                }

                br.close();
            }
            FiguraMod.debug("Successfully loaded config file");
        } catch (Exception e) {
            FiguraMod.LOGGER.warn("Failed to load config file! Generating a new one...", e);
            setDefaults();
        }
    }

    public static void saveConfig() {
        try {
            JsonObject configJson = new JsonObject();
            configJson.addProperty("CONFIG_VERSION", Config.CONFIG_VERSION);

            for (Config config : CONFIG_ENTRIES) {
                String name = config.name().toLowerCase();
                if (config.value instanceof Number n)
                    configJson.addProperty(name, n);
                else if (config.value instanceof Character c)
                    configJson.addProperty(name, c);
                else if (config.value instanceof Boolean b)
                    configJson.addProperty(name, b);
                else
                    configJson.addProperty(name, String.valueOf(config.value));
            }

            String jsonString = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(configJson);
            FileWriter fileWriter = new FileWriter(FILE);
            fileWriter.write(jsonString);
            fileWriter.close();

            FiguraMod.debug("Successfully saved config file");
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to save config file!", e);
        }
    }

    public static void applyConfig() {
        for (Config config : CONFIG_ENTRIES)
            config.setValue(String.valueOf(config.tempValue));
    }

    public static void discardConfig() {
        for (Config config : CONFIG_ENTRIES)
            config.tempValue = config.value;
    }

    public static void setDefaults() {
        for (Config config : CONFIG_ENTRIES)
            config.value = config.defaultValue;
    }

    public static void update(JsonObject json, int version) {
        Map<Config, String> versionMap = Config.CONFIG_UPDATES.get(version);
        if (versionMap == null)
            return;

        for (Map.Entry<Config, String> config : versionMap.entrySet()) {
            JsonElement object = json.get(config.getValue());

            if (object == null)
                continue;

            String jsonValue = object.getAsString();
            Config.valueOf(config.getKey().toString()).setValue(jsonValue);
        }

        FiguraMod.debug("Config updated from version " + version);
    }

    //returns true if modmenu shifts other buttons on the game menu screen
    public static boolean modmenuShift() {
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            String buttonStyle = com.terraformersmc.modmenu.config.ModMenuConfig.MODS_BUTTON_STYLE.getValue().toString();
            return !buttonStyle.equals("SHRINK") && !buttonStyle.equals("ICON");
        }

        return false;
    }
}