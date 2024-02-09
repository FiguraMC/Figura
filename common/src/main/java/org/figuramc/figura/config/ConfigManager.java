package org.figuramc.figura.config;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.utils.PlatformUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ConfigManager {

    private static final File FILE = new File(PlatformUtils.getConfigDir().resolve(FiguraMod.MOD_ID + ".json").toString());
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    public static final List<ConfigType<?>> REGISTRY = new ArrayList<>();
    public static final Map<String, ConfigType.Category> CATEGORIES_REGISTRY = new LinkedHashMap<>();

    private static boolean initializing = false;

    public static void init() {
        Configs.init();
        loadConfig();
        saveConfig();
    }
    private static JsonParser parser = new JsonParser();
    public static void loadConfig() {
        try {
            if (FILE.exists()) {
                initializing = true;
                BufferedReader br = new BufferedReader(new FileReader(FILE));
                JsonObject json = parser.parse(br).getAsJsonObject();

                JsonElement version = json.get("CONFIG_VERSION");
                if (version != null && version.getAsInt() != Configs.CONFIG_VERSION) {
                    update(json, version.getAsInt());
                } else {
                    for (ConfigType<?> config : REGISTRY) {
                        JsonElement object = json.get(config.id.toLowerCase());
                        if (object == null)
                            continue;
                        if (config instanceof ConfigType.SerializableConfig) {
                            ConfigType.SerializableConfig s = (ConfigType.SerializableConfig) config;
                            s.deserialize(object);
                            continue;
                        }
                        String obj = object.getAsString();
                        if (config instanceof ConfigType.KeybindConfig) {
                            ConfigType.KeybindConfig keybind = (ConfigType.KeybindConfig) config;
                            keybind.keyBind.setKey(InputConstants.getKey(obj));
                        } else if (config instanceof ConfigType.InputConfig<?>) {
                            ConfigType.InputConfig<?> input = (ConfigType.InputConfig<?>) config;
                            if (input.inputType.validator.test(obj))
                                config.setValue(obj);
                        } else {
                            config.setValue(obj);
                        }
                    }
                }

                br.close();
                FiguraMod.debug("Successfully loaded config file");
            }
        } catch (Exception e) {
            FiguraMod.LOGGER.warn("Failed to load config file! Resetting all settings...", e);
            setDefaults();
        }

        initializing = false;
    }

    public static void saveConfig() {
        if (initializing)
            return;

        try {
            JsonObject configJson = new JsonObject();
            configJson.addProperty("CONFIG_VERSION", Configs.CONFIG_VERSION);

            for (ConfigType<?> config : REGISTRY) {
                if (config.isDefault())
                    continue;

                String id = config.id;
                if (config instanceof ConfigType.SerializableConfig) {
                    ConfigType.SerializableConfig s = (ConfigType.SerializableConfig) config;
                    configJson.add(id, s.serialize());
                } else if (config.value instanceof Number) {
                    Number n = (Number) config.value;
                    configJson.addProperty(id, n);
                } else if (config.value instanceof Character) {
                    Character c = (Character) config.value;
                    configJson.addProperty(id, c);
                } else if (config.value instanceof Boolean) {
                    Boolean b = (Boolean) config.value;
                    configJson.addProperty(id, b);
                } else if (config.value != null)
                    configJson.addProperty(id, String.valueOf(config.value));
            }

            String jsonString = GSON.toJson(configJson);
            FileWriter fileWriter = new FileWriter(FILE);
            fileWriter.write(jsonString);
            fileWriter.close();

            FiguraMod.debug("Successfully saved config file");
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to save config file!", e);
        }
    }

    public static void applyConfig() {
        for (ConfigType<?> config : REGISTRY)
            config.setValue(String.valueOf(config.tempValue));
    }

    public static void discardConfig() {
        for (ConfigType<?> config : REGISTRY)
            config.discardConfig();
    }

    public static void setDefaults() {
        for (ConfigType<?> config : REGISTRY)
            config.setDefault();
    }

    public static void update(JsonObject json, int version) {
        Map<ConfigType<?>, String> versionMap = Configs.CONFIG_UPDATES.get(version);
        if (versionMap == null)
            return;

        for (Map.Entry<ConfigType<?>, String> config : versionMap.entrySet()) {
            JsonElement object = json.get(config.getValue());

            if (object == null)
                continue;

            String jsonValue = object.getAsString();
            config.getKey().setValue(jsonValue);
        }

        FiguraMod.debug("Config updated from version " + version);
    }
}