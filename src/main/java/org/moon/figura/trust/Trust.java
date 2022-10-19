package org.moon.figura.trust;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.moon.figura.lua.FiguraAPI;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Trust {

    //default trusts
    public static final Trust
            INIT_INST = new Trust("INIT_INST", 0, 32767, 0, 4096, 16384, Integer.MAX_VALUE, Integer.MAX_VALUE),
            WORLD_TICK_INST = new Trust("WORLD_TICK_INST", 0, 32767, 0, 128, 256, Integer.MAX_VALUE, Integer.MAX_VALUE),
            TICK_INST = new Trust("TICK_INST", 0, 32767, 0, 4096, 16384, Integer.MAX_VALUE, Integer.MAX_VALUE),
            WORLD_RENDER_INST = new Trust("WORLD_RENDER_INST", 0, 32767, 0, 32, 64, Integer.MAX_VALUE, Integer.MAX_VALUE),
            RENDER_INST = new Trust("RENDER_INST", 0, 32767, 0, 4096, 16384, Integer.MAX_VALUE, Integer.MAX_VALUE),
            COMPLEXITY = new Trust("COMPLEXITY", 0, 8191, 0, 512, 2048, Integer.MAX_VALUE, Integer.MAX_VALUE),
            PARTICLES = new Trust("PARTICLES", 0, 63, 0, 4, 32, Integer.MAX_VALUE, Integer.MAX_VALUE),
            SOUNDS = new Trust("SOUNDS", 0, 63, 0, 4, 32, Integer.MAX_VALUE, Integer.MAX_VALUE),
            VOLUME = new Trust("VOLUME", 0, 99, 0, 100, 100, 100, 100),
            BB_ANIMATIONS = new Trust("BB_ANIMATIONS", 0, 255, 0, 32, 128, Integer.MAX_VALUE, Integer.MAX_VALUE),
            VANILLA_MODEL_EDIT = new Trust("VANILLA_MODEL_EDIT", 0, 0, 1, 1, 1),
            NAMEPLATE_EDIT = new Trust("NAMEPLATE_EDIT", 0, 0, 1, 1, 1),
            OFFSCREEN_RENDERING = new Trust("OFFSCREEN_RENDERING", 0, 0, 1, 1, 1),
            //CUSTOM_RENDER_LAYER = new Trust("CUSTOM_RENDER_LAYER", List.of(0, 0, 1, 1, 1)),
            CUSTOM_SOUNDS = new Trust("CUSTOM_SOUNDS", 0, 0, 1, 1, 1),
            CUSTOM_HEADS = new Trust("CUSTOM_HEADS", 0, 0, 1, 1, 1);

    public static final HashMap<String, Collection<Trust>> CUSTOM_TRUST = new HashMap<>();

    public static final List<Trust> DEFAULT = List.of(
            INIT_INST,
            WORLD_TICK_INST,
            TICK_INST,
            WORLD_RENDER_INST,
            RENDER_INST,
            COMPLEXITY,
            PARTICLES,
            SOUNDS,
            VOLUME,
            BB_ANIMATIONS,
            VANILLA_MODEL_EDIT,
            NAMEPLATE_EDIT,
            OFFSCREEN_RENDERING,
            CUSTOM_SOUNDS,
            CUSTOM_HEADS
    );


    // -- trust stuff -- //


    //stuff
    public final String name;
    private final List<Integer> defaults;

    //toggle check
    public final boolean isToggle;

    //used only for sliders
    public final Integer min;
    public final Integer max;
    public final Integer stepSize;

    //toggle constructor
    public Trust(String name, int blocked, int untrusted, int trusted, int friend, int local) {
        this(name, null, null, blocked, untrusted, trusted, friend, local);
    }

    //slider constructor
    public Trust(String name, Integer min, Integer max, int blocked, int untrusted, int trusted, int friend, int local) {
        this(name, min, max, 1, blocked, untrusted, trusted, friend, local);
    }
    public Trust(String name, Integer min, Integer max, Integer stepSize, int blocked, int untrusted, int trusted, int friend, int local) {
        this.name = name;
        this.isToggle = min == null || max == null;
        this.min = min;
        this.max = max;
        this.stepSize = stepSize;
        this.defaults = List.of(blocked, untrusted, trusted, friend, local);
    }

    //infinity check :p
    public boolean checkInfinity(int value) {
        return max != null && value > max && this != VOLUME;
    }

    //transform to boolean
    public boolean asBoolean(int value) {
        return value >= 1;
    }

    public int getDefault(Group group) {
        if (group.index >= 0 && group.index < defaults.size())
            return defaults.get(group.index);
        return -1;
    }

    public static void register(FiguraAPI api) {
        Collection<Trust> c = api.customTrust();
        if (c != null) CUSTOM_TRUST.put(api.getName(), c);
    }

    public enum Group {
        BLOCKED(0, ChatFormatting.RED.getColor()),
        UNTRUSTED(1, ChatFormatting.WHITE.getColor()),
        TRUSTED(2, ChatFormatting.GREEN.getColor()),
        FRIEND(3, ColorUtils.Colors.FRAN_PINK.hex),
        LOCAL(4, ChatFormatting.AQUA.getColor());

        public final int index;
        public final int color;
        public final MutableComponent text;

        Group(int index, int color) {
            this.index = index;
            this.color = color;
            text = FiguraText.of("trust.group." + name().toLowerCase()).withStyle(Style.EMPTY.withColor(color));
        }

        public static Group indexOf(int i) {
            for (Group value : values()) {
                if (value.index == i)
                    return value;
            }

            return null;
        }
    }
}
