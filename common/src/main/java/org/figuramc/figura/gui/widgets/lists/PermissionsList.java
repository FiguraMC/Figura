package org.figuramc.figura.gui.widgets.lists;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.gui.widgets.FiguraWidget;
import org.figuramc.figura.gui.widgets.SliderWidget;
import org.figuramc.figura.gui.widgets.SwitchButton;
import org.figuramc.figura.gui.widgets.TextField;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.permissions.PermissionPack;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.*;
import java.util.function.Predicate;

public class PermissionsList extends AbstractList {

    public boolean precise = false;

    private final Map<Component, List<GuiEventListener>> permissions = new LinkedHashMap<>();

    public PermissionsList(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        // background and scissors
        UIHelper.blitSliced(gui, x, y, width, height, UIHelper.OUTLINE_FILL);
        enableScissors(gui);

        // scrollbar
        Font font = Minecraft.getInstance().font;
        int lineHeight = font.lineHeight;
        int entryHeight = 27 + lineHeight; // 11 (slider) + font height + 16 (padding)
        int titleHeight = 16 + lineHeight;

        int size = 0;
        for (List<GuiEventListener> value : permissions.values())
            size += value.size();
        int totalHeight = size * entryHeight;

        boolean titles = permissions.size() > 1;
        if (titles) totalHeight += permissions.size() * titleHeight;

        scrollBar.setY(y + 4);
        scrollBar.setVisible(totalHeight > height);
        scrollBar.setScrollRatio(entryHeight, totalHeight - height);

        // render
        int xOffset = scrollBar.isVisible() ? 8 : 15;
        int yOffset = scrollBar.isVisible() ? (int) -(Mth.lerp(scrollBar.getScrollProgress(), -16, totalHeight - height)) : 16;

        for (Map.Entry<Component, List<GuiEventListener>> entry : permissions.entrySet()) {
            // titles
            if (titles) {
                gui.drawCenteredString(font, entry.getKey(), x + (width - xOffset) / 2, y + yOffset, 0xFFFFFF);
                yOffset += titleHeight;
            }

            // elements
            for (GuiEventListener widget : entry.getValue()) {
                ((FiguraWidget) widget).setX(x + xOffset);
                ((FiguraWidget) widget).setY(y + yOffset);
                yOffset += entryHeight;
            }
        }

        // render children
        super.render(gui, mouseX, mouseY, delta);

        // reset scissor
        gui.disableScissor();
    }

    public void updateList(PermissionPack container) {
        // clear old widgets
        for (List<GuiEventListener> list : permissions.values())
            list.forEach(children::remove);
        permissions.clear();

        // add new permissions

        // defaults
        permissions.put(FiguraText.of(), generateWidgets(container, Permissions.DEFAULT, FiguraMod.MOD_ID));

        // custom
        for (Map.Entry<String, Collection<Permissions>> entry : PermissionManager.CUSTOM_PERMISSIONS.entrySet())
            permissions.put(Component.translatable(entry.getKey()), generateWidgets(container, entry.getValue(), entry.getKey()));
    }

    private List<GuiEventListener> generateWidgets(PermissionPack container, Collection<Permissions> coll, String id) {
        List<GuiEventListener> list = new ArrayList<>();

        int x = getX();
        int y = getWidth();
        int width = getWidth();

        for (Permissions permissions : coll) {
            int lineHeight = Minecraft.getInstance().font.lineHeight;

            GuiEventListener widget;
            String text = id + ".permissions.value." + permissions.name.toLowerCase(Locale.US);
            if (!permissions.isToggle) {
                if (!precise)
                    widget = new PermissionSlider(x + 8, y, width - 30, 11 + lineHeight, container, permissions, this, id, text);
                else
                    widget = new PermissionField(x + 8, y, width - 30, 11 + lineHeight, container, permissions, this, id, text);
            } else {
                widget = new PermissionSwitch(x + 8, y, width - 30, 20 + lineHeight, container, permissions, this, id, text);
            }

            list.add(widget);
            children.add(widget);
        }

        return list;
    }

    private static class PermissionSlider extends SliderWidget {

        private static final Component INFINITY = FiguraText.of("permissions.infinity");

        private final PermissionPack container;
        private final Permissions permissions;
        private final PermissionsList parent;
        private final String text;
        private Component value;
        private boolean changed;

        public PermissionSlider(int x, int y, int width, int height, PermissionPack container, Permissions permissions, PermissionsList parent, String id, String text) {
            super(x, y, width, height, Mth.clamp(container.get(permissions) / (permissions.max + 1d), 0d, 1d), permissions.max / permissions.stepSize + 1, permissions.showSteps());
            this.container = container;
            this.permissions = permissions;
            this.parent = parent;
            this.text = text;
            this.value = container.get(permissions) == Integer.MAX_VALUE ? INFINITY : Component.literal(String.valueOf(container.get(permissions)));
            this.changed = container.isChanged(permissions);

            setAction(slider -> {
                // update permission
                int value = this.showSteps ? ((SliderWidget) slider).getIntValue() * permissions.stepSize : (int) ((permissions.max + 1d) * slider.getScrollProgress());
                boolean infinity = permissions.checkInfinity(value);

                container.insert(permissions, infinity ? Integer.MAX_VALUE : value, id);
                changed = container.isChanged(permissions);

                // update text
                this.value = infinity ? INFINITY : Component.literal(String.valueOf(value));
            });
        }

        @Override
        public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float delta) {
            PoseStack pose = gui.pose();
            Font font = Minecraft.getInstance().font;

            // button
            pose.pushPose();
            pose.translate(0f, font.lineHeight, 0f);
            super.renderWidget(gui, mouseX, mouseY, delta);
            pose.popPose();

            // texts
            MutableComponent name = Component.translatable(this.text);
            if (changed) name = Component.literal("*").setStyle(FiguraMod.getAccentColor()).append(name).append("*");
            int valueX = getX() + getWidth() - font.width(value) - 1;

            int x = getX() + 1;
            int y = getY() + 1;
            int width = valueX - getX() - 2;

            UIHelper.renderScrollingText(gui, name, x, y, width, 0xFFFFFF);
            gui.drawString(font, value.copy().setStyle(FiguraMod.getAccentColor()), valueX, getY() + 1, 0xFFFFFF);

            if (parent.isInsideScissors(mouseX, mouseY) && UIHelper.isMouseOver(x, y, width, font.lineHeight, mouseX, mouseY))
                UIHelper.setTooltip(Component.translatable(this.text + ".tooltip"));
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.isActive() || !this.isHoveredOrFocused() || !this.isMouseOver(mouseX, mouseY))
                return false;

            if (button == 1) {
                container.reset(permissions);
                this.parent.updateList(container);
                playDownSound(Minecraft.getInstance().getSoundManager());
                return true;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
        }
    }

    private static class PermissionSwitch extends SwitchButton {

        private final PermissionPack container;
        private final Permissions permissions;
        private final PermissionsList parent;
        private final String id;
        private final String text;
        private Component value;
        private boolean changed;

        public PermissionSwitch(int x, int y, int width, int height, PermissionPack container, Permissions permissions, PermissionsList parent, String id, String text) {
            super(x, y, width, height, Component.translatable(text), permissions.asBoolean(container.get(permissions)));
            this.container = container;
            this.permissions = permissions;
            this.parent = parent;
            this.id = id;
            this.text = text;
            this.changed = container.isChanged(permissions);
            this.value = FiguraText.of("permissions." + (toggled ? "enabled" : "disabled"));
        }

        @Override
        public void onPress() {
            // update permission
            boolean value = !this.isToggled();

            container.insert(permissions, value ? 1 : 0, id);
            this.changed = container.isChanged(permissions);

            // update text
            this.value = FiguraText.of("permissions." + (value ? "enabled" : "disabled"));

            super.onPress();
        }

        @Override
        public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float delta) {
            super.renderWidget(gui, mouseX, mouseY, delta);
            if (parent.isInsideScissors(mouseX, mouseY) && UIHelper.isMouseOver(getX() + 1, getY() + 1, getWidth() - 2, Minecraft.getInstance().font.lineHeight, mouseX, mouseY))
                UIHelper.setTooltip(Component.translatable(this.text + ".tooltip"));
        }

        @Override
        protected void renderDefaultTexture(GuiGraphics gui, float delta) {
            PoseStack pose = gui.pose();
            Font font = Minecraft.getInstance().font;

            // button
            pose.pushPose();
            pose.translate(0f, font.lineHeight, 0f);
            super.renderDefaultTexture(gui, delta);
            pose.popPose();
        }

        @Override
        protected void renderText(GuiGraphics gui, float delta) {
            Font font = Minecraft.getInstance().font;

            // texts
            MutableComponent name = getMessage().copy();
            if (changed) name = Component.literal("*").setStyle(FiguraMod.getAccentColor()).append(name).append("*");
            int valueX = getX() + getWidth() - font.width(value) - 1;
            int valueY = getY() + font.lineHeight + 11 - font.lineHeight / 2;

            UIHelper.renderScrollingText(gui, name, getX() + 1, getY() + 1, getWidth() - 2, 0xFFFFFF);
            gui.drawString(font, value.copy().setStyle(FiguraMod.getAccentColor()), valueX, valueY, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.isActive() || !this.isHoveredOrFocused() || !this.isMouseOver(mouseX, mouseY))
                return false;

            if (button == 1) {
                container.reset(permissions);
                this.parent.updateList(container);
                playDownSound(Minecraft.getInstance().getSoundManager());
                return true;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
        }
    }

    private static class PermissionField extends TextField {

        private final static Predicate<String> validator = s -> {
            try {
                Integer i = Integer.parseInt(s);
                return i >= 0;
            } catch (Exception ignored) {
                return false;
            }
        };

        private final PermissionPack container;
        private final Permissions permissions;
        private final PermissionsList parent;
        private final String text;
        private Component value;
        private boolean changed;

        public PermissionField(int x, int y, int width, int height, PermissionPack container, Permissions permissions, PermissionsList parent, String id, String text) {
            super(x, y, width, height, null, null);

            this.container = container;
            this.permissions = permissions;
            this.parent = parent;
            this.text = text;
            String val = String.valueOf(container.get(permissions));
            this.value = Component.literal(val);
            this.changed = container.isChanged(permissions);

            this.getField().setValue(val);
            this.getField().setResponder(txt -> {
                if (!validator.test(txt))
                    return;

                int value = Integer.parseInt(txt);

                container.insert(permissions, value, id);
                changed = container.isChanged(permissions);

                // update text
                this.value = Component.literal(String.valueOf(value));
            });
        }

        @Override
        public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
            Font font = Minecraft.getInstance().font;

            // text colour
            int color = 0xFFFFFF;

            // invalid value
            String text = getField().getValue();
            if (!validator.test(text)) {
                color = 0xFF5555;
            }
            // changed value
            else if (changed) {
                TextColor textColor = FiguraMod.getAccentColor().getColor();
                color = textColor == null ? ColorUtils.Colors.AWESOME_BLUE.hex : textColor.getValue();
            }

            // set text colour
            setColor(color);
            setBorderColour(0xFF000000 + color);

            // field
            super.render(gui, mouseX, mouseY, delta);

            // texts
            MutableComponent name = Component.translatable(this.text);
            if (changed) name = Component.literal("*").setStyle(FiguraMod.getAccentColor()).append(name).append("*");
            int valueX = getX() + getWidth() - font.width(value) - 1;

            int x = getX() + 1;
            int y = getY() + 1 - font.lineHeight;
            int width = valueX - getX() - 2;

            UIHelper.renderScrollingText(gui, name, x, y, width, 0xFFFFFF);
            gui.drawString(font, value.copy().setStyle(FiguraMod.getAccentColor()), valueX, getY() + 1 - font.lineHeight, 0xFFFFFF);

            if (parent.isInsideScissors(mouseX, mouseY) && UIHelper.isMouseOver(x, y, width, font.lineHeight, mouseX, mouseY))
                UIHelper.setTooltip(Component.translatable(this.text + ".tooltip"));
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.isEnabled() || !this.isMouseOver(mouseX, mouseY))
                return false;

            if (button == 1) {
                container.reset(permissions);
                this.parent.updateList(container);
                this.getField().playDownSound(Minecraft.getInstance().getSoundManager());
                return true;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
        }
    }
}
