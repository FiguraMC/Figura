package org.moon.figura.gui.actionwheel;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.config.Config;
import org.moon.figura.lua.types.LuaFunction;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.ui.UIHelper;

public class ActionWheel {

    private static final ResourceLocation WHEEL_TEXTURE = new FiguraIdentifier("textures/gui/action_wheel.png");

    private static boolean enabled = false;
    private static int selected = -1;

    public static void render(PoseStack stack) {
        if (!isEnabled()) return;

        Minecraft minecraft = Minecraft.getInstance();
        double x = minecraft.getWindow().getGuiScaledWidth() / 2d;
        double y = minecraft.getWindow().getGuiScaledHeight() / 2d;

        //rendering
        stack.pushPose();
        stack.translate(x, y, 0d);

        float scale = (float) Config.ACTION_WHEEL_SCALE.value;
        stack.scale(scale, scale, scale);

        //render wheel
        UIHelper.renderTexture(stack, -64, -64, 128, 128, WHEEL_TEXTURE);

        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        Page currentPage;
        if (avatar == null || avatar.luaState == null || (currentPage = avatar.luaState.actionWheel.currentPage) == null) {
            stack.popPose();
            return;
        }

        //get left and right slots, right side have preference when the slots its odd
        int slots = currentPage.getSize();
        int leftSlots = (int) Math.floor(slots / 2d);
        int rightSlots = (int) Math.ceil(slots / 2d);

        double mouseX = minecraft.mouseHandler.xpos();
        double mouseY = minecraft.mouseHandler.ypos();

        //calculate selected slot
        getSelected(mouseX, mouseY, minecraft, leftSlots, rightSlots);

        //render selected overlay
        Action action = selected == -1 ? null : currentPage.actions[selected];
        renderSelected(stack, leftSlots, rightSlots, action == null ? null : action.color);

        //render items
        renderItems(stack, leftSlots, rightSlots);

        stack.popPose();

        //render title
        renderTitle(stack, mouseX, mouseY, minecraft, action == null ? null : action.title);
    }

    // -- render helpers -- //

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

    private static void renderSelected(PoseStack stack, int leftSlots, int rightSlots, FiguraVec3 color) {
        if (selected == -1) return;

        boolean left = selected >= rightSlots;
        int type = left ? leftSlots : rightSlots;
        int relativeSel = left ? selected - rightSlots : selected;
        TextureData data = OverlayTexture.values()[type - 1].data[relativeSel];

        //render
        stack.pushPose();
        stack.mulPose(Vector3f.ZP.rotationDegrees(data.rotation + (left ? 180 : 0)));

        UIHelper.setupTexture(TextureData.TEXTURE);
        if (color != null)
            RenderSystem.setShaderColor((float) color.x, (float) color.y, (float) color.z, 1f);
        UIHelper.blit(stack, data.x, data.y, data.w, data.h, data.u, data.v, data.rw, data.rh, TextureData.TW, TextureData.TH);

        stack.popPose();
    }

    private static void renderItems(PoseStack stack, int leftSlots, int rightSlots) {

    }

    private static void renderTitle(PoseStack stack, double mouseX, double mouseY, Minecraft minecraft, String title) {
        if (title == null) return;

    }

    // -- functions -- //

    public static void execute(boolean left) {
        Avatar avatar;
        Page currentPage;
        if (!isEnabled() || selected == -1 || (avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID())) == null || avatar.luaState == null || (currentPage = avatar.luaState.actionWheel.currentPage) == null) {
            selected = -1;
            return;
        }

        //get action
        Action action = currentPage.actions[selected];
        LuaFunction function = action == null ? null : (left ? action.leftAction : action.rightAction);

        //execute
        if (function != null)
            avatar.tryCall(function, -1);

        selected = -1;
    }

    public static void setEnabled(boolean enabled) {
        ActionWheel.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    // -- overlay texture data -- //

    private enum OverlayTexture {
        ONE(
                new TextureData(true, -64, 0, 0, 0)
        ),
        TWO(
                new TextureData(64, 0),
                new TextureData(64, 0, 90)
        ),
        THREE(
                new TextureData(128, 0),
                new TextureData(-32, 128, 64, 0),
                new TextureData(0, 64, 64, 0)
        ),
        FOUR(
                new TextureData(192, 0),
                new TextureData(192, 64),
                new TextureData(192, 0, 90),
                new TextureData(192, 64, 90)
        );

        private final TextureData[] data;

        OverlayTexture(TextureData... data) {
            this.data = data;
        }
    }

    private static class TextureData {

        private static final ResourceLocation TEXTURE = new FiguraIdentifier("textures/gui/action_wheel_selected.png");
        private static final int TW = 256;
        private static final int TH = 128;

        private final int x, y, w, h, u, v, rw, rh;
        private final int rotation;

        public TextureData(boolean large, int y, int u, int v, int rotation) {
            this.x = 0;
            this.y = y;
            this.w = 64;
            this.h = large ? 128 : 64;
            this.u = u;
            this.v = v;
            this.rw = 64;
            this.rh = large ? 128 : 64;
            this.rotation = rotation;
        }

        public TextureData(int y, int u, int v, int rotation) {
            this(false, y, u, v, rotation);
        }
        public TextureData(int u, int v, int rotation) {
            this(-64, u, v, rotation);
        }
        public TextureData(int u, int v) {
            this(u, v, 0);
        }
    }
}
