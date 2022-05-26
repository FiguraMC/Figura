package org.moon.figura.gui.actionwheel;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

public class ActionWheel {

    private static boolean enabled = false;
    private static int selected = 0;

    public static void render(PoseStack stack) {
        if (!enabled) return;

        Minecraft minecraft = Minecraft.getInstance();
        getSelected(minecraft, 4);
    }

    public static void getSelected(Minecraft minecraft, int slots) {
        double mouseX = minecraft.mouseHandler.xpos();
        double mouseY = minecraft.mouseHandler.ypos();

        double screenWidth = minecraft.getWindow().getScreenWidth();
        double screenHeight = minecraft.getWindow().getScreenHeight();

        double screenMiddleW = screenWidth / 2;
        double screenMiddleH = screenHeight / 2;

        //get the total mouse distance from the center of screen
        double mouseDistance = Math.sqrt(Math.pow(screenMiddleW - mouseX, 2) + Math.pow(screenMiddleH - mouseY, 2));

        //get the mouse angle in degrees from middle of screen, starting at top, clockwise
        double angle = Math.toDegrees(Math.atan2(mouseY - screenMiddleH, mouseX - screenMiddleW)) + 90;
        if (angle < 0) angle += 360;

        //get left and right slots, right side have preference if odd
        int leftSlots = (int) Math.floor(slots / 2d);
        int rightSlots = (int) Math.ceil(slots / 2d);

        //get the selected slot
        if (angle < 180)
            selected = (int) Math.floor((rightSlots / 180d) * angle);
        else
            selected = (int) Math.floor((leftSlots / 180d) * (angle - 180)) + rightSlots;

        System.out.println("mouseDistance: " + mouseDistance + " selected: " + selected + " angle: " + angle);
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
