package org.figuramc.figura.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.lua.api.action_wheel.Action;
import org.figuramc.figura.lua.api.action_wheel.Page;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.List;
import java.util.function.Function;

public class ActionWheel {

    private static final ResourceLocation TEXTURE = new FiguraIdentifier("textures/gui/action_wheel.png");
    private static final ResourceLocation ICONS = new FiguraIdentifier("textures/gui/action_wheel_icons.png");
    private static final double DISTANCE = 41;
    private static final double DEADZONE = 19;

    private static boolean enabled = false;
    private static int selected = -1;

    // rendering data
    private static Minecraft minecraft;
    private static int slots, leftSlots, rightSlots;
    private static float scale;
    private static int x, y;
    private static double mouseX, mouseY;

    public static void render(GuiGraphics gui) {
        if (!isEnabled()) return;

        minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        x = (int) (window.getGuiScaledWidth() / 2d);
        y = (int) (window.getGuiScaledHeight() / 2d);

        // rendering
        PoseStack pose = gui.pose();
        pose.pushPose();
        pose.translate(x, y, 0d);

        scale = Configs.ACTION_WHEEL_SCALE.value;
        pose.scale(scale, scale, scale);

        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        Page currentPage;
        if (avatar == null || avatar.luaRuntime == null || (currentPage = avatar.luaRuntime.action_wheel.currentPage) == null) {
            // this also pops the stack
            renderEmpty(gui, avatar == null);
            return;
        }

        // get left and right slots, right side have preference when the slots its odd
        slots = currentPage.getSize();
        leftSlots = (int) Math.floor(slots / 2d);
        rightSlots = (int) Math.ceil(slots / 2d);

        mouseX = minecraft.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
        mouseY = minecraft.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();

        // calculate selected slot
        FiguraMod.pushProfiler("selectedSlot");
        calculateSelected();

        // render overlays
        FiguraMod.popPushProfiler("wheel");
        renderTextures(gui, currentPage);

        // reset colours
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // render items
        FiguraMod.popPushProfiler("items");
        renderItemsAndIcons(gui, currentPage);

        pose.popPose();

        // render title
        FiguraMod.popPushProfiler("texts");
        renderTexts(gui, currentPage);

        FiguraMod.popProfiler();
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

    private static void renderEmpty(GuiGraphics gui, boolean avatar) {
        // render empty wheel
        TextureData data = OverlayTexture.values()[0].data[0];
        data.render(gui, null, false);
        data.render(gui, null, true);

        gui.pose().popPose(); // previous stack

        // warning text
        Component component = FiguraText.of("gui.error." + (avatar ? "no_avatar" : "no_wheel_page")).withStyle(ChatFormatting.YELLOW);
        Font font = minecraft.font;

        UIHelper.renderOutlineText(gui, font, component, x - font.width(component) / 2, y - font.lineHeight / 2, 0xFFFFFF, 0);
    }

    private static void calculateSelected() {
        // get the total mouse distance from the center of the wheel
        double mouseDistance = Math.sqrt(Math.pow(x - mouseX, 2) + Math.pow(y - mouseY, 2));

        // no need to sum left side because if the right side is 0, the left side will also be 0
        if (rightSlots == 0 || mouseDistance < (DEADZONE * scale)) {
            selected = -1;
            return;
        }

        // get the mouse angle in degrees from middle of the wheel, starting at top, clockwise
        double angle = Math.toDegrees(Math.atan2(mouseY - y, mouseX - x)) + 90;
        if (angle < 0) angle += 360;

        // get the selected slot
        if (angle < 180)
            selected = (int) Math.floor((rightSlots / 180d) * angle);
        else
            selected = (int) Math.floor((leftSlots / 180d) * (angle - 180)) + rightSlots;
    }

    private static void renderTextures(GuiGraphics gui, Page page) {
        for (int i = 0; i < slots; i++) {
            Action action = page.slots()[i];
            boolean left = i >= rightSlots;
            int type = left ? leftSlots : rightSlots;
            int relativeIndex = left ? i - rightSlots : i;

            // get color
            FiguraVec3 color = action == null ? null : action.getColor(selected == i);

            // render background texture
            OverlayTexture.values()[type - 1].data[relativeIndex].render(gui, color, left);

            // no icon for null action
            if (action == null)
                continue;

            // convert angle to x and y coordinates
            double angle = getAngle(i);
            double x = Math.cos(angle) * 15 - 4;
            double y = Math.sin(angle) * 15 - 4;

            // render icon
            UIHelper.enableBlend();

            if (color != null)
                RenderSystem.setShaderColor((float) color.x, (float) color.y, (float) color.z, 1f);
            gui.blit(ICONS,
                    (int) Math.round(x), (int) Math.round(y),
                    8, 8,
                    action.scroll != null ? 24f : action.toggle != null ? action.isToggled() ? 16f : 8f : 0f, color == null ? 0f : 8f,
                    8, 8,
                    32, 16
            );
        }
    }

    private static void renderItemsAndIcons(GuiGraphics gui, Page page) {
        for (int i = 0; i < slots; i++) {
            Action action = page.slots()[i];
            if (action == null)
                continue;

            boolean isSelected = selected == i;

            // convert angle to x and y coordinates
            double angle = getAngle(i);
            double xOff = Math.cos(angle) * DISTANCE;
            double yOff = Math.sin(angle) * DISTANCE;

            // texture
            Action.TextureData texture = action.getTexture(isSelected);
            if (texture != null) {
                UIHelper.enableBlend();
                gui.blit(texture.texture.getLocation(),
                        (int) Math.round(xOff - texture.width * texture.scale / 2d),
                        (int) Math.round(yOff - texture.height * texture.scale / 2d),
                        (int) Math.round(texture.width * texture.scale), (int) Math.round(texture.height * texture.scale),
                        (float) texture.u, (float) texture.v,
                        texture.width, texture.height,
                        texture.texture.getWidth(), texture.texture.getHeight());
            }

            // no item, no render
            ItemStack item = action.getItem(isSelected);
            if (item == null || item.isEmpty())
                continue;

            // render
            gui.renderItem(item, (int) Math.round(xOff - 8), (int) Math.round(yOff - 8));
            if (Configs.ACTION_WHEEL_DECORATIONS.value)
                gui.renderItemDecorations(minecraft.font, item, (int) Math.round(xOff - 8), (int) Math.round(yOff - 8));
        }
    }

    private static void renderTexts(GuiGraphics gui, Page page) {
        Font font = minecraft.font;
        int titlePosition = Configs.ACTION_WHEEL_TITLE.value;
        int indicatorPosition = Configs.ACTION_WHEEL_SLOTS_INDICATOR.value;
        PoseStack pose = gui.pose();

        Action selectedTitleAction = selected == -1 ? null : page.slots()[selected];
        String selectedTitle = selectedTitleAction == null ? null : selectedTitleAction.getTitle();

        // page indicator
        int groupCount = page.getGroupCount();
        if (groupCount > 1 && (selectedTitle == null || indicatorPosition != titlePosition - 2)) {
            pose.pushPose();
            pose.translate(0d, 0d, 999d);
            int index = page.getSlotsShift();
            int greatest = page.getGreatestSlot() + 1;

            MutableComponent indicator = Component.empty();
            int extraWidth = 0;

            // down arrow
            if (index > 1) {
                Component arrow = UIHelper.UP_ARROW.copy().append(" ");
                indicator.append(arrow);
                extraWidth -= font.width(arrow);
            }
            // text
            indicator.append(FiguraText.of("gui.action_wheel.slots_indicator",
                    Component.literal(String.valueOf((index - 1) * 8 + 1)).withStyle(FiguraMod.getAccentColor()),
                    Component.literal(String.valueOf(Math.min(index * 8, greatest))).withStyle(FiguraMod.getAccentColor()),
                    Component.literal(String.valueOf(greatest)).withStyle(FiguraMod.getAccentColor())
            ));
            // up arrow
            if (index < groupCount) {
                Component arrow = Component.literal(" ").append(UIHelper.DOWN_ARROW);
                indicator.append(arrow);
                extraWidth += font.width(arrow);
            }

            // draw
            gui.drawString(font, indicator, x - (int) ((font.width(indicator) - extraWidth) / 2f), (int) Position.index(indicatorPosition).apply(font.lineHeight), 0xFFFFFF);
            pose.popPose();
        }

        // all titles
        if (titlePosition >= 5) {
            boolean internal = titlePosition == 5;
            double distance = (internal ? DISTANCE : 66) * scale;
            pose.pushPose();
            pose.translate(0f, 0f, 999f);
            for (int i = 0; i < slots; i++) {
                Action action = page.slots()[i];
                if (action == null)
                    continue;

                String title = action.getTitle();
                if (title == null)
                    continue;

                // convert angle to x and y coordinates
                double angle = getAngle(i);
                double xOff = Math.cos(angle) * distance;
                double yOff = Math.sin(angle) * distance;

                // render text
                int textX = x + (int) (Math.round(xOff));
                int textY = y + (int) (Math.round(yOff + (internal ? 9 * scale : -font.lineHeight / 2f)));

                Component text = TextUtils.replaceInText(Emojis.applyEmojis(TextUtils.tryParseJson(title)), "\n|\\\\n", " ");
                int textWidth = font.width(text);

                if (internal) {
                    textX -= textWidth / 2f;
                    if (i >= rightSlots)
                        textX = Math.min(textX, x - textWidth - 1);
                    else
                        textX = Math.max(textX, x + 1);
                } else if (i >= rightSlots) {
                    textX -= textWidth;
                }

                gui.drawString(font, text, textX, textY, 0xFFFFFF);
            }
            pose.popPose();
            return;
        }

        // title
        if (selectedTitle == null)
            return;

        // vars
        Component text = Emojis.applyEmojis(TextUtils.tryParseJson(selectedTitle));
        List<Component> list = TextUtils.splitText(text, "\n");
        int height = font.lineHeight * list.size();

        // render
        if (titlePosition < 2) { // tooltip
            UIHelper.renderTooltip(gui, text, (int) mouseX, (int) mouseY, titlePosition == 0);
        } else { // anchored
            pose.pushPose();
            pose.translate(0d, 0d, 999d);

            int y = (int) Position.index(titlePosition - 2).apply(height);
            for (int i = 0; i < list.size(); i++) {
                Component component = list.get(i);
                gui.drawString(font, component, x - (int) (font.width(component) / 2f), y + font.lineHeight * i, 0xFFFFFF);
            }

            pose.popPose();
        }
    }

    // -- functions -- // 

    public static void execute(int index, boolean left) {
        Avatar avatar;
        if (!isEnabled() || (avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID())) == null || avatar.luaRuntime == null) {
            selected = -1;
            return;
        }

        // wheel click action
        if (avatar.luaRuntime.action_wheel.execute(avatar, left))
            return;

        // execute action
        Page currentPage;
        if (index < 0 || index > 7 || avatar.luaRuntime == null || (currentPage = avatar.luaRuntime.action_wheel.currentPage) == null) {
            selected = -1;
            return;
        }

        Action action = currentPage.slots()[index];
        if (action != null) action.execute(avatar, left);

        selected = -1;
    }

    public static void hotbarKeyPressed(int i) {
        execute(i, true);
    }

    public static void scroll(double delta) {
        Avatar avatar;
        if (!isEnabled() || (avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID())) == null || avatar.luaRuntime == null)
            return;

        // wheel scroll action
        if (avatar.luaRuntime.action_wheel.mouseScroll(avatar, delta))
            return;

        // scroll action
        Page currentPage;
        if (avatar.luaRuntime == null || (currentPage = avatar.luaRuntime.action_wheel.currentPage) == null)
            return;

        if (selected >= 0 && selected <= 7) {
            Action action = currentPage.slots()[selected];
            if (action != null && action.scroll != null) {
                action.mouseScroll(avatar, delta);
                return;
            }
        }

        // page scroll
        currentPage.setSlotsShift(currentPage.getSlotsShift() - (int) Math.signum(delta));
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

        public void render(GuiGraphics gui, FiguraVec3 color, boolean left) {
            PoseStack pose = gui.pose();
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(rotation + (left ? 180 : 0)));

            UIHelper.enableBlend();
            if (color != null)
                gui.setColor((float) color.x, (float) color.y, (float) color.z, 1f);
            gui.blit(TEXTURE, 0, y, 64, h, u, color == null ? v : v + 128, 64, rh, 256, 256);

            pose.popPose();
        }
    }

    // -- text position -- // 

    private enum Position {
        TOP(height -> Math.max(y - 64 * scale - 4 - height, 4)),
        MID(height -> y - height / 2f),
        BOT(height -> Math.min(y + 64 * scale + 4 + height, y * 2 - 4) - height);

        private final Function<Double, Double> function;

        Position(Function<Double, Double> function) {
            this.function = function;
        }

        public static Position index(int i) {
            return values()[i];
        }

        public double apply(double d) {
            return function.apply(d);
        }
    }
}
