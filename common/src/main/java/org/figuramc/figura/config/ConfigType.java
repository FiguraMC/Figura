package org.figuramc.figura.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.gui.screens.NetworkFilterScreen;
import org.figuramc.figura.lua.api.net.NetworkingAPI;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraText;

import java.util.ArrayList;
import java.util.List;

public abstract class ConfigType<T> {

    public final String id;
    public final boolean hidden;

    // display
    public Component name;
    public Component tooltip;

    // values
    public T value;
    public T tempValue; // settings screen "undo"
    public final T defaultValue;
    public boolean disabled;

    public ConfigType(String name, T value) {
        this(name, value, false);
    }
    public ConfigType(String name, T value, boolean hidden) {
        this.id = name;
        this.hidden = hidden;

        // generate names
        name = "config." + name;
        this.name = FiguraText.of(name);
        this.tooltip = FiguraText.of(name + ".tooltip");

        // values
        this.value = this.defaultValue = this.tempValue = value;
    }

    public abstract T parseValue(String newVal);

    public void setValue(String newVal) {
        boolean change = !value.equals(tempValue);

        try {
            value = parseValue(newVal);
        } catch (Exception e) {
            FiguraMod.LOGGER.warn("Failed to set this config (" + id + ") value \"" + value + "\", restoring it to default", e);
            value = defaultValue;
            change = true;
        }

        tempValue = value;
        if (change) {
            try {
                onChange();
            } catch (Exception e) {
                FiguraMod.LOGGER.warn("Failed to run onChange for config \"" + id + "\"", e);
            }
        }
    }

    public void setTempValue(String newVal) {
        this.tempValue = parseValue(newVal);
    }

    public void discardConfig() {
        tempValue = value;
    }

    public void setDefault() {
        value = defaultValue;
    }

    public void resetTemp() {
        tempValue = defaultValue;
    }

    public boolean isDefault() {
        return defaultValue == null || defaultValue.equals(tempValue);
    }

    public void onChange() {}

    // -- json serializable -- //

    public interface SerializableConfig {
        JsonElement serialize();
        void deserialize(JsonElement element);
    }


    // -- category -- // 


    public static class Category extends ConfigType<Void> {
        public final List<ConfigType<?>> children = new ArrayList<>();

        public Category(String name) {
            super(name, null);
            ConfigManager.CATEGORIES_REGISTRY.put(name, this);
        }

        @Override
        public Void parseValue(String newVal) {
            return null;
        }

        @Override
        public void setValue(String newVal) {}
    }

    public abstract static class ParentedConfig<T> extends ConfigType<T> {
        public final Category parent;

        public ParentedConfig(String name, Category category, T value, boolean hidden) {
            super(name, value, hidden);
            this.parent = category;

            category.children.add(this);
            ConfigManager.REGISTRY.add(this);
        }

        public ParentedConfig(String name, Category category, T value) {
            this(name, category, value, false);
        }
    }


    // -- boolean -- // 


    public static class BoolConfig extends ParentedConfig<Boolean> {
        public BoolConfig(String name, Category category, Boolean defaultValue, boolean hidden) {
            super(name, category, defaultValue, hidden);
        }

        public BoolConfig(String name, Category category, Boolean defaultValue) {
            this(name, category, defaultValue, false);
        }

        @Override
        public Boolean parseValue(String newVal) {
            return Boolean.valueOf(newVal);
        }
    }


    // -- enum -- // 


    public static class EnumConfig extends ParentedConfig<Integer> {
        public List<Component> enumList, enumTooltip;

        public EnumConfig(String name, Category category, int defaultValue, int length) {
            super(name, category, defaultValue);

            name = "config." + name;

            // generate enum list
            ArrayList<Component> enumList = new ArrayList<>();
            ArrayList<Component> enumTooltip = new ArrayList<>();

            for (int i = 1; i <= length; i++) {
                enumList.add(FiguraText.of(name + "." + i));
                enumTooltip.add(FiguraText.of(name + "." + i + ".tooltip"));
            }

            this.enumList = enumList;
            this.enumTooltip = enumTooltip;
        }

        @Override
        public Integer parseValue(String newVal) {
            return Math.floorMod(Integer.parseInt(newVal), enumList.size());
        }
    }


    // -- input -- // 


    public abstract static class InputConfig<T> extends ParentedConfig<T> {
        public final InputType inputType;

        public InputConfig(String name, Category category, T defaultValue, InputType inputType) {
            super(name, category, defaultValue);
            this.inputType = inputType;
        }
    }

    public static class StringConfig extends InputConfig<String> {
        public StringConfig(String name, Category category, String defaultValue) {
            super(name, category, defaultValue, InputType.ANY);
        }

        @Override
        public String parseValue(String newVal) {
            return newVal;
        }
    }

    public static class IntConfig extends InputConfig<Integer> {
        public IntConfig(String name, Category category, int defaultValue) {
            super(name, category, defaultValue, InputType.INT);
        }

        @Override
        public Integer parseValue(String newVal) {
            return Integer.parseInt(newVal);
        }
    }

    public static class FloatConfig extends InputConfig<Float> {
        public FloatConfig(String name, Category category, float defaultValue) {
            super(name, category, defaultValue, InputType.FLOAT);
        }

        @Override
        public Float parseValue(String newVal) {
            return Float.parseFloat(newVal);
        }
    }

    public static class PositiveIntConfig extends InputConfig<Integer> {
        public PositiveIntConfig(String name, Category category, int defaultValue) {
            super(name, category, defaultValue, InputType.POSITIVE_INT);
        }

        @Override
        public Integer parseValue(String newVal) {
            return Integer.parseInt(newVal);
        }
    }

    public static class PositiveFloatConfig extends InputConfig<Float> {
        public PositiveFloatConfig(String name, Category category, float defaultValue) {
            super(name, category, defaultValue, InputType.POSITIVE_FLOAT);
        }

        @Override
        public Float parseValue(String newVal) {
            return Float.parseFloat(newVal);
        }
    }

    public static class ColorConfig extends InputConfig<Integer> {
        public ColorConfig(String name, Category category, int defaultValue) {
            super(name, category, defaultValue, InputType.HEX_COLOR);
        }

        @Override
        public Integer parseValue(String newVal) {
            return Integer.parseInt(newVal);
        }

        @Override
        public void setTempValue(String newVal) {
            this.tempValue = ColorUtils.rgbToInt(ColorUtils.userInputHex(newVal, FiguraVec3.of()));
        }
    }

    public static class FolderConfig extends InputConfig<String> {
        public FolderConfig(String name, Category category, String defaultValue) {
            super(name, category, defaultValue, InputType.FOLDER_PATH);
        }

        @Override
        public String parseValue(String newVal) {
            return newVal;
        }
    }

    public static class IPConfig extends InputConfig<String> {
        public IPConfig(String name, Category category, String defaultValue) {
            super(name, category, defaultValue, InputType.IP);
        }

        @Override
        public String parseValue(String newVal) {
            return newVal;
        }
    }


    // -- keybind -- // 


    public static class KeybindConfig extends ParentedConfig<String> {
        public ConfigKeyBind keyBind;

        public KeybindConfig(String name, Category category, String defaultValue) {
            super(name, category, defaultValue);
            this.keyBind = new ConfigKeyBind(this.name.getString(), InputConstants.getKey(defaultValue), this);
        }

        @Override
        public String parseValue(String newVal) {
            return newVal;
        }
    }


    // -- button -- // 


    public static class ButtonConfig extends ParentedConfig<Void> {
        public final Runnable toRun;

        public ButtonConfig(String name, Category category, Runnable toRun) {
            super(name, category, null);
            this.toRun = toRun;
        }

        @Override
        public Void parseValue(String newVal) {
            return null;
        }

        @Override
        public void setValue(String newVal) {}
    }

    // -- network filter -- //
    public static class NetworkFilterConfig extends ButtonConfig implements SerializableConfig {
        private final ArrayList<NetworkingAPI.Filter> filters = new ArrayList<>();
        public NetworkFilterConfig(String name, Category category) {
            super(name, category, () -> {
                Minecraft mc = Minecraft.getInstance();
                mc.setScreen(new NetworkFilterScreen(mc.screen));
            });
        }
        public ArrayList<NetworkingAPI.Filter> getFilters() {
            return filters;
        }

        @Override
        public JsonElement serialize() {
            JsonArray array = new JsonArray();
            for (NetworkingAPI.Filter filter :
                    filters) {
                JsonObject o = new JsonObject();
                o.addProperty("source", filter.getSource());
                array.add(o);
            }
            return array;
        }

        @Override
        public void deserialize(JsonElement element) {
            filters.clear();
            if (!element.isJsonArray()) return;
            JsonArray array = element.getAsJsonArray();
            for (JsonElement e :
                    array) {
                if (!e.isJsonObject()) continue;
                JsonObject o = e.getAsJsonObject();
                JsonElement s = o.get("source");
                JsonElement m = o.get("mode");
                if (s == null || m == null || !s.isJsonPrimitive() || !m.isJsonPrimitive()) continue;
                JsonPrimitive source = s.getAsJsonPrimitive();
                JsonPrimitive mode = m.getAsJsonPrimitive();
                if (!source.isString() || !mode.isNumber()) continue;
                filters.add(new NetworkingAPI.Filter(source.getAsString()));
            }
        }

        @Override
        public void setDefault() {
            filters.clear();
        }

        @Override
        public boolean isDefault() {
            return filters.size() == 0;
        }
    }
}
