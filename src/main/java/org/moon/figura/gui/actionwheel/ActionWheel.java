package org.moon.figura.gui.actionwheel;

import com.mojang.blaze3d.vertex.PoseStack;

public class ActionWheel {

    private static boolean enabled = false;

    public static void render(PoseStack stack) {
        if (!enabled) return;


    }

    public static void execute(boolean left) {

    }

    public static void setEnabled(boolean enabled) {
        ActionWheel.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
