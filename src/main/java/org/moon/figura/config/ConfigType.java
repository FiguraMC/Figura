package org.moon.figura.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import org.moon.figura.FiguraMod;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;

import java.util.ArrayList;
import java.util.List;

public abstract class ConfigType<T> {

    public final String id;

    //display
    public Component name;
    public Component tooltip;

    //values
    public T value;
    public T tempValue; //settings screen "undo"
    public final T defaultValue;
    public boolean disabled;

    public ConfigType(String name, T value) {
        this.id = name;

        //generate names
        name = "config." + name;
        this.name = new FiguraText(name);
        this.tooltip = new FiguraText(name + ".tooltip");

        //values
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
        return tempValue == defaultValue;
    }

    public void onChange() {}


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

        public ParentedConfig(String name, Category category, T value) {
            super(name, value);
            this.parent = category;

            category.children.add(this);
            ConfigManager.REGISTRY.add(this);
        }
    }


    // -- boolean -- //


    public static class BoolConfig extends ParentedConfig<Boolean> {
        public BoolConfig(String name, Category category, Boolean defaultValue) {
            super(name, category, defaultValue);
        }

        @Override
        public Boolean parseValue(String newVal) {
            return Boolean.valueOf(newVal);
        }
    }


    // -- enum -- //


    public static class EnumConfig extends ParentedConfig<Integer> {
        public Component enumTooltip;
        public List<Component> enumList;

        public EnumConfig(String name, Category category, int defaultValue, int length) {
            super(name, category, defaultValue);

            name = "config." + name;

            //tooltip
            this.enumTooltip = new FiguraText(name + ".enum");

            //generate enum list
            ArrayList<Component> enumList = new ArrayList<>();
            for (int i = 1; i <= length; i++)
                enumList.add(new FiguraText(name + "." + i));
            this.enumList = enumList;
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
}
