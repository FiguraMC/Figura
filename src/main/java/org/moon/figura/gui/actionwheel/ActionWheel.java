package org.moon.figura.gui.actionwheel;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.config.Config;
import org.moon.figura.lua.types.LuaFunction;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.util.List;

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
        getSelected(mouseX, mouseY, minecraft, leftSlots, rightSlots, scale);

        //render selected overlay
        Action action = selected == -1 ? null : currentPage.actions[selected];
        renderSelected(stack, leftSlots, rightSlots, action == null ? null : action.color);

        //render items
        renderItems(x, y, leftSlots, rightSlots, scale, minecraft, currentPage);

        stack.popPose();

        //render title
        renderTitle(stack, x, y, mouseX, mouseY, scale, minecraft, action == null ? null : action.title);
    }

    // -- render helpers -- //

    private static void getSelected(double mouseX, double mouseY, Minecraft minecraft, int leftSlots, int rightSlots, float scale) {
        //window specific variables
        double screenMiddleW = minecraft.getWindow().getScreenWidth() / 2d;
        double screenMiddleH = minecraft.getWindow().getScreenHeight() / 2d;
        double guiScale = minecraft.getWindow().getGuiScale();

        //get the total mouse distance from the center of screen
        double mouseDistance = Math.sqrt(Math.pow(screenMiddleW - mouseX, 2) + Math.pow(screenMiddleH - mouseY, 2));

        //no need to sum left side because if the right side is 0, the left side will also be 0
        if (rightSlots == 0 || mouseDistance < (19 * guiScale * scale)) {
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

    private static void renderItems(double screenX, double screenY, int leftSlots, int rightSlots, float scale, Minecraft minecraft, Page page) {
        double distance = 41 * scale;
        int slots = leftSlots + rightSlots;

        for (int i = 0; i < slots; i++) {
            Action action = page.actions[i];
            if (action == null)
                continue;

            double angle;
            if (i < rightSlots)
                angle = 180d / rightSlots * (i - ((rightSlots - 1) * 0.5));
            else
                angle = 180d / leftSlots * (i - rightSlots - ((leftSlots - 1) * 0.5f) + leftSlots);

            //convert angle to x and y coordinates
            double x = screenX + Math.cos(Math.toRadians(angle)) * distance;
            double y = screenY + Math.sin(Math.toRadians(angle)) * distance;

            //render
            PoseStack stack = RenderSystem.getModelViewStack();
            stack.pushPose();
            stack.translate(x, y, 0);
            stack.scale(scale, scale, scale);

            minecraft.getItemRenderer().renderGuiItem(action.item, -8, -8);
            if ((boolean) Config.ACTION_WHEEL_DECORATIONS.value)
                minecraft.getItemRenderer().renderGuiItemDecorations(minecraft.font, action.item, -8, -8);

            stack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    private static void renderTitle(PoseStack stack, double screenX, double screenY, double mouseX, double mouseY, float scale, Minecraft minecraft, String title) {
        if (title == null)
            return;

        //vars
        Component text = TextUtils.tryParseJson(title);
        List<Component> list = TextUtils.splitText(text, "\n");
        Font font = minecraft.font;
        int height = font.lineHeight * list.size();

        //pos
        double y;
        int config = (int) Config.ACTION_WHEEL_TITLE.value;
        switch (config) {
            case 2 -> y = Math.max(screenY - 64 * scale - 4 - height, 4); // top
            case 3 -> y = screenY - height / 2f; // middle
            case 4 -> y = Math.min(screenY + 64 * scale + 4 + height, screenY * 2 - 4) - height; // bottom
            default -> { // tooltip
                double guiScale = minecraft.getWindow().getGuiScale();
                UIHelper.renderTooltip(stack, text, (int) (mouseX / guiScale), (int) (mouseY / guiScale), config == 0);
                return;
            }
        }

        //render (when not tooltip)
        stack.pushPose();
        stack.translate(0d, 0d, 400d);

        for (int i = 0; i < list.size(); i++) {
            Component component = list.get(i);
            font.drawShadow(stack, component, (float) screenX - font.width(component) / 2f, (float) y + font.lineHeight * i, 0xFFFFFF);
        }

        stack.popPose();
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

        private final int x, y, w, h, rw, rh;
        private final float u, v;
        private final int rotation;

        public TextureData(boolean large, int y, float u, float v, int rotation) {
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

        public TextureData(int y, float u, float v, int rotation) {
            this(false, y, u, v, rotation);
        }
        public TextureData(float u, float v, int rotation) {
            this(-64, u, v, rotation);
        }
        public TextureData(float u, float v) {
            this(u, v, 0);
        }
    }
}
