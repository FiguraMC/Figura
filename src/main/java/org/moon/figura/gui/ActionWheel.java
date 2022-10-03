package org.moon.figura.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.config.Config;
import org.moon.figura.lua.api.action_wheel.Action;
import org.moon.figura.lua.api.action_wheel.Page;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.util.List;

public class ActionWheel {

    private static final ResourceLocation TEXTURE = new FiguraIdentifier("textures/gui/action_wheel.png");
    private static final ResourceLocation ICONS = new FiguraIdentifier("textures/gui/action_wheel_icons.png");

    private static boolean enabled = false;
    private static int selected = -1;

    //rendering data
    private static Minecraft minecraft;
    private static int slots, leftSlots, rightSlots;
    private static float scale;
    private static int x, y;
    private static double mouseX, mouseY;

    public static void render(PoseStack stack) {
        if (!isEnabled()) return;

        minecraft = Minecraft.getInstance();
        x = (int) (minecraft.getWindow().getGuiScaledWidth() / 2d);
        y = (int) (minecraft.getWindow().getGuiScaledHeight() / 2d);

        //rendering
        stack.pushPose();
        stack.translate(x, y, 0d);

        scale = Config.ACTION_WHEEL_SCALE.asFloat();
        stack.scale(scale, scale, scale);

        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        Page currentPage;
        if (avatar == null || avatar.luaRuntime == null || (currentPage = avatar.luaRuntime.action_wheel.currentPage) == null) {
            //this also pops the stack
            renderEmpty(stack, avatar == null);
            return;
        }

        //get left and right slots, right side have preference when the slots its odd
        slots = currentPage.getSize();
        leftSlots = (int) Math.floor(slots / 2d);
        rightSlots = (int) Math.ceil(slots / 2d);

        mouseX = minecraft.mouseHandler.xpos();
        mouseY = minecraft.mouseHandler.ypos();

        //calculate selected slot
        calculateSelected();

        //render overlays
        renderTextures(stack, currentPage);

        //render items
        renderItems(currentPage);

        stack.popPose();

        //render title
        Action action = selected == -1 ? null : currentPage.actions[selected];
        renderTitle(stack, action == null ? null : action.getTitle());
    }

    // -- render helpers -- //

    private static double getAngle(int i) {
        double angle;
        if (i < rightSlots)
            angle = 180d / rightSlots * (i - ((rightSlots - 1) * 0.5));
        else
            angle = 180d / leftSlots * (i - rightSlots - ((leftSlots - 1) * 0.5f) + leftSlots);

        return Math.toRadians(angle);
    }

    private static void renderEmpty(PoseStack stack, boolean avatar) {
        //render empty wheel
        TextureData data = OverlayTexture.values()[0].data[0];
        data.render(stack, null, false);
        data.render(stack, null, true);

        stack.popPose(); //previous stack

        //warning text
        Component component = FiguraText.of("gui.error." + (avatar ? "no_avatar" : "no_wheel_page")).withStyle(ChatFormatting.YELLOW);
        Font font = minecraft.font;

        UIHelper.renderOutlineText(stack, font, component, x - font.width(component) / 2, y - font.lineHeight / 2, 0xFFFFFF, 0);
    }

    private static void calculateSelected() {
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

    private static void renderTextures(PoseStack stack, Page page) {
        for (int i = 0; i < slots; i++) {
            Action action = page.actions[i];
            boolean left = i >= rightSlots;
            int type = left ? leftSlots : rightSlots;
            int relativeIndex = left ? i - rightSlots : i;

            //get color
            FiguraVec3 color = action == null ? null : action.getColor(selected == i);

            //render background texture
            OverlayTexture.values()[type - 1].data[relativeIndex].render(stack, color, left);

            //no icon for null action
            if (action == null)
                continue;

            //convert angle to x and y coordinates
            double angle = getAngle(i);
            double x = Math.cos(angle) * 15 - 4;
            double y = Math.sin(angle) * 15 - 4;

            //render icon
            UIHelper.setupTexture(ICONS);

            if (color != null)
                RenderSystem.setShaderColor((float) color.x, (float) color.y, (float) color.z, 1f);
            UIHelper.blit(stack,
                    (int) Math.round(x), (int) Math.round(y),
                    8, 8,
                    action.scroll != null ? 24f : action.toggle != null ? action.isToggled() ? 16f : 8f : 0f, color == null ? 0f : 8f,
                    8, 8,
                    32, 16
            );
        }
    }

    private static void renderItems(Page page) {
        double distance = 41 * scale;

        for (int i = 0; i < slots; i++) {
            Action action = page.actions[i];
            if (action == null)
                continue;

            ItemStack item = action.getItem(selected == i);

            //no item, no render
            if (item == null || item.isEmpty())
                continue;

            //convert angle to x and y coordinates
            double angle = getAngle(i);
            double xOff = x + Math.cos(angle) * distance;
            double yOff = y + Math.sin(angle) * distance;

            //render
            PoseStack stack = RenderSystem.getModelViewStack();
            stack.pushPose();
            stack.translate(xOff, yOff, 0);
            stack.scale(scale, scale, scale);

            minecraft.getItemRenderer().renderGuiItem(item, -8, -8);
            if (Config.ACTION_WHEEL_DECORATIONS.asBool())
                minecraft.getItemRenderer().renderGuiItemDecorations(minecraft.font, item, -8, -8);

            stack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    private static void renderTitle(PoseStack stack, String title) {
        if (title == null)
            return;

        //vars
        Component text = TextUtils.tryParseJson(title);
        List<Component> list = TextUtils.splitText(text, "\n");
        Font font = minecraft.font;
        int height = font.lineHeight * list.size();

        //pos
        double yOff;
        int config = Config.ACTION_WHEEL_TITLE.asInt();
        switch (config) {
            case 2 -> yOff = Math.max(y - 64 * scale - 4 - height, 4); // top
            case 3 -> yOff = y - height / 2f; // middle
            case 4 -> yOff = Math.min(y + 64 * scale + 4 + height, y * 2 - 4) - height; // bottom
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
            font.drawShadow(stack, component, x - font.width(component) / 2, (int) (yOff + font.lineHeight * i), 0xFFFFFF);
        }

        stack.popPose();
    }

    // -- functions -- //

    public static void execute(int index, boolean left) {
        Avatar avatar;
        if (!isEnabled() || (avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID())) == null || avatar.luaRuntime == null) {
            selected = -1;
            return;
        }

        //wheel click action
        avatar.luaRuntime.action_wheel.execute(avatar, left);

        //execute action
        Page currentPage;
        if (index < 0 || index > 7 || avatar.luaRuntime == null || (currentPage = avatar.luaRuntime.action_wheel.currentPage) == null) {
            selected = -1;
            return;
        }

        Action action = currentPage.actions[index];
        if (action != null) action.execute(avatar, left);

        selected = -1;
    }

    public static void scroll(double delta) {
        Avatar avatar;
        if (!isEnabled() || (avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID())) == null || avatar.luaRuntime == null)
            return;

        //wheel scroll action
        avatar.luaRuntime.action_wheel.mouseScroll(avatar, delta);

        //scroll
        Page currentPage;
        if (selected < 0 || selected > 7 || (currentPage = avatar.luaRuntime.action_wheel.currentPage) == null)
            return;

        Action action = currentPage.actions[selected];
        if (action != null) action.mouseScroll(avatar, delta);
    }

    public static void setEnabled(boolean enabled) {
        ActionWheel.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static int getSelected() {
        return selected;
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

        private final int y, h, rh;
        private final float u, v;
        private final int rotation;

        public TextureData(boolean large, int y, float u, float v, int rotation) {
            this.y = y;
            this.h = large ? 128 : 64;
            this.u = u;
            this.v = v;
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

        public void render(PoseStack stack, FiguraVec3 color, boolean left) {
            stack.pushPose();
            stack.mulPose(Vector3f.ZP.rotationDegrees(rotation + (left ? 180 : 0)));

            UIHelper.setupTexture(TEXTURE);
            if (color != null)
                RenderSystem.setShaderColor((float) color.x, (float) color.y, (float) color.z, 1f);
            UIHelper.blit(stack, 0, y, 64, h, u, color == null ? v : v + 128, 64, rh, 256, 256);

            stack.popPose();
        }
    }
}
