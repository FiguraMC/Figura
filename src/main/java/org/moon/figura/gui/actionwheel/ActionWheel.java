package org.moon.figura.gui.actionwheel;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.config.Config;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.ui.UIHelper;

public class ActionWheel {

    private static final ResourceLocation WHEEL_TEXTURE = new FiguraIdentifier("textures/gui/action_wheel.png");

    private static boolean enabled = false;
    private static int selected = 0;

    public static void render(PoseStack stack) {
        if (!enabled) return;

        //mouse and window variables
        Minecraft minecraft = Minecraft.getInstance();

        double mouseX = minecraft.mouseHandler.xpos();
        double mouseY = minecraft.mouseHandler.ypos();

        //get left and right slots, right side have preference if odd
        int slots = 4;
        int leftSlots = (int) Math.floor(slots / 2d);
        int rightSlots = (int) Math.ceil(slots / 2d);

        //calculate selected slot
        getSelected(mouseX, mouseY, minecraft, leftSlots, rightSlots);

        //rendering
        stack.pushPose();
        stack.translate(minecraft.getWindow().getGuiScaledWidth() / 2d,  minecraft.getWindow().getGuiScaledHeight() / 2d, 0d);

        float scale = (float) Config.ACTION_WHEEL_SCALE.value;
        stack.scale(scale, scale, scale);

        //render wheel
        UIHelper.renderTexture(stack, -64, -64, 128, 128, WHEEL_TEXTURE);

        stack.popPose();
    }

    private static void getSelected(double mouseX, double mouseY, Minecraft minecraft, int leftSlots, int rightSlots) {
        //window specific variables
        double screenMiddleW = minecraft.getWindow().getScreenWidth() / 2d;
        double screenMiddleH = minecraft.getWindow().getScreenHeight() / 2d;
        double guiScale = minecraft.getWindow().getGuiScale();

        //get the total mouse distance from the center of screen
        double mouseDistance = Math.sqrt(Math.pow(screenMiddleW - mouseX, 2) + Math.pow(screenMiddleH - mouseY, 2));

        //no need to sum left side because if the right side is 0, the left side will also be 0
        if (rightSlots == 0 || mouseDistance < (19 * guiScale * (float) Config.ACTION_WHEEL_SCALE.value)) {
            selected = -1;
            return;
        }

        //get the mouse angle in degrees from middle of screen, starting at top, clockwise
        double angle = Math.toDegrees(Math.atan2(mouseY - screenMiddleH, mouseX - screenMiddleW)) + 90;
        if (angle < 0) angle += 360;

        //get the selected slot
        if (angle < 180)
            selected = (int) Math.floor((rightSlots / 180d) * angle);
        else
            selected = (int) Math.floor((leftSlots / 180d) * (angle - 180)) + rightSlots;
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
