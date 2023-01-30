package org.moon.figura.gui.widgets.lists;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import org.moon.figura.FiguraMod;
import org.moon.figura.gui.widgets.SliderWidget;
import org.moon.figura.gui.widgets.SwitchButton;
import org.moon.figura.gui.widgets.TextField;
import org.moon.figura.permissions.Permissions;
import org.moon.figura.permissions.PermissionPack;
import org.moon.figura.permissions.PermissionManager;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.ui.UIHelper;

import java.util.*;
import java.util.function.Predicate;

public class PermissionsList extends AbstractList {

    public boolean precise = false;

    private final Map<Component, List<GuiEventListener>> permissions = new LinkedHashMap<>();

    public PermissionsList(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //background and scissors
        UIHelper.renderSliced(stack, x, y, width, height, UIHelper.OUTLINE);
        UIHelper.setupScissor(x + scissorsX, y + scissorsY, width + scissorsWidth, height + scissorsHeight);

        //scrollbar
        Font font = Minecraft.getInstance().font;
        int lineHeight = font.lineHeight;
        int entryHeight = 27 + lineHeight; //11 (slider) + font height + 16 (padding)
        int titleHeight = 16 + lineHeight;

        int size = 0;
        for (List<GuiEventListener> value : permissions.values())
            size += value.size();
        int totalHeight = size * entryHeight;

        boolean titles = permissions.size() > 1;
        if (titles) totalHeight += permissions.size() * titleHeight;

        scrollBar.setY(y + 4);
        scrollBar.visible = totalHeight > height;
        scrollBar.setScrollRatio(entryHeight, totalHeight - height);

        //render
        int xOffset = scrollBar.visible ? 8 : 15;
        int yOffset = scrollBar.visible ? (int) -(Mth.lerp(scrollBar.getScrollProgress(), -16, totalHeight - height)) : 16;

        for (Map.Entry<Component, List<GuiEventListener>> entry : permissions.entrySet()) {
            //titles
            if (titles) {
                UIHelper.drawCenteredString(stack, font, entry.getKey(), x + (width - xOffset) / 2, y + yOffset, 0xFFFFFF);
                yOffset += titleHeight;
            }

            //elements
            for (GuiEventListener widget : entry.getValue()) {
                if (widget instanceof AbstractWidget w) {
                    w.setX(x + xOffset);
                    w.setY(y + yOffset);
                } else if (widget instanceof TextField t) {
                    t.setPos(x + xOffset, y + yOffset);
                }
                yOffset += entryHeight;
            }
        }

        //render children
        super.render(stack, mouseX, mouseY, delta);

        //reset scissor
        RenderSystem.disableScissor();
    }

    public void updateList(PermissionPack container) {
        //clear old widgets
        for (List<GuiEventListener> list : permissions.values())
            list.forEach(children::remove);
        permissions.clear();

        //add new permissions

        //defaults
        permissions.put(FiguraText.of(), generateWidgets(container, Permissions.DEFAULT, FiguraMod.MOD_ID));

        //custom
        for (Map.Entry<String, Collection<Permissions>> entry : PermissionManager.CUSTOM_PERMISSIONS.entrySet())
            permissions.put(Component.translatable(entry.getKey()), generateWidgets(container, entry.getValue(), entry.getKey()));
    }

    private List<GuiEventListener> generateWidgets(PermissionPack container, Collection<Permissions> coll, String id) {
        List<GuiEventListener> list = new ArrayList<>();

        for (Permissions permissions : coll) {
            int lineHeight = Minecraft.getInstance().font.lineHeight;

            GuiEventListener widget;
            if (!permissions.isToggle) {
                if (!precise)
                    widget = new PermissionSlider(x + 8, y, width - 30, 11 + lineHeight, container, permissions, this, id);
                else
                    widget = new PermissionField(x + 8, y, width - 30, 11 + lineHeight, container, permissions, this, id);
            } else {
                widget = new PermissionSwitch(x + 8, y, width - 30, 20 + lineHeight, container, permissions, this, id);
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
        private final String id;
        private Component value;
        private boolean changed;

        public PermissionSlider(int x, int y, int width, int height, PermissionPack container, Permissions permissions, PermissionsList parent, String id) {
            super(x, y, width, height, Mth.clamp(container.get(permissions) / (permissions.max + 1d), 0d, 1d), permissions.max / permissions.stepSize + 1, permissions.showSteps());
            this.container = container;
            this.permissions = permissions;
            this.parent = parent;
            this.id = id;
            this.value = container.get(permissions) == Integer.MAX_VALUE ? INFINITY : Component.literal(String.valueOf(container.get(permissions)));
            this.changed = container.isChanged(permissions);

            setAction(slider -> {
                //update permission
                int value = this.showSteps ? ((SliderWidget) slider).getIntValue() * permissions.stepSize : (int) ((permissions.max + 1d) * slider.getScrollProgress());
                boolean infinity = permissions.checkInfinity(value);

                container.insert(permissions, infinity ? Integer.MAX_VALUE : value, id);
                changed = container.isChanged(permissions);

                //update text
                this.value = infinity ? INFINITY : Component.literal(String.valueOf(value));
            });
        }

        @Override
        public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
            Font font = Minecraft.getInstance().font;

            //button
            stack.pushPose();
            stack.translate(0f, font.lineHeight, 0f);
            super.renderButton(stack, mouseX, mouseY, delta);
            stack.popPose();

            //texts
            MutableComponent name = Component.translatable(id + ".permissions.value." + permissions.name.toLowerCase());
            if (changed) name = Component.literal("*").setStyle(FiguraMod.getAccentColor()).append(name).append("*");

            font.draw(stack, name, getX() + 1, getY() + 1, 0xFFFFFF);
            font.draw(stack, value.copy().setStyle(FiguraMod.getAccentColor()), getX() + width - font.width(value) - 1, getY() + 1, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.active || !this.isHoveredOrFocused() || !this.isMouseOver(mouseX, mouseY))
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
        private Component value;
        private boolean changed;

        public PermissionSwitch(int x, int y, int width, int height, PermissionPack container, Permissions permissions, PermissionsList parent, String id) {
            super(x, y, width, height, permissions.asBoolean(container.get(permissions)));
            this.container = container;
            this.permissions = permissions;
            this.parent = parent;
            this.id = id;
            this.changed = container.isChanged(permissions);
            this.value = FiguraText.of("permissions." + (toggled ? "enabled" : "disabled"));
        }

        @Override
        public void onPress() {
            //update permission
            boolean value = !this.isToggled();

            container.insert(permissions, value ? 1 : 0, id);
            this.changed = container.isChanged(permissions);

            //update text
            this.value = FiguraText.of("permissions." + (value ? "enabled" : "disabled"));

            super.onPress();
        }

        @Override
        protected void renderTexture(PoseStack stack, float delta) {
            Font font = Minecraft.getInstance().font;

            //button
            stack.pushPose();
            stack.translate(0f, font.lineHeight, 0f);
            super.renderTexture(stack, delta);
            stack.popPose();

            //texts
            MutableComponent name = Component.translatable(id + ".permissions.value." + permissions.name.toLowerCase());
            if (changed) name = Component.literal("*").setStyle(FiguraMod.getAccentColor()).append(name).append("*");

            font.draw(stack, name, getX() + 1, getY() + 1, 0xFFFFFF);
            font.draw(stack, value.copy().setStyle(FiguraMod.getAccentColor()), getX() + width - font.width(value) - 1, getY() + 1, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.active || !this.isHoveredOrFocused() || !this.isMouseOver(mouseX, mouseY))
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
        private final String id;
        private Component value;
        private boolean changed;

        public PermissionField(int x, int y, int width, int height, PermissionPack container, Permissions permissions, PermissionsList parent, String id) {
            super(x, y, width, height, null, null);

            this.container = container;
            this.permissions = permissions;
            this.parent = parent;
            this.id = id;
            String val = String.valueOf(container.get(permissions));
            this.value = Component.literal(val);
            this.changed = container.isChanged(permissions);

            this.getField().setValue(val);
            this.getField().setResponder(text -> {
                if (!validator.test(text))
                    return;

                int value = Integer.parseInt(text);

                container.insert(permissions, value, id);
                changed = container.isChanged(permissions);

                //update text
                this.value = Component.literal(String.valueOf(value));
            });
        }

        @Override
        public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
            Font font = Minecraft.getInstance().font;

            //text colour
            int color = 0xFFFFFF;

            //invalid value
            String text = getField().getValue();
            if (!validator.test(text)) {
                color = 0xFF5555;
            }
            //changed value
            else if (changed) {
                TextColor textColor = FiguraMod.getAccentColor().getColor();
                color = textColor == null ? ColorUtils.Colors.FRAN_PINK.hex : textColor.getValue();
            }

            //set text colour
            setColor(color);
            setBorderColour(0xFF000000 + color);

            //field
            stack.pushPose();
            //stack.translate(0f, font.lineHeight, 0f);
            super.render(stack, mouseX, mouseY, delta);
            stack.popPose();

            //texts
            MutableComponent name = Component.translatable(id + ".permissions.value." + permissions.name.toLowerCase());
            if (changed) name = Component.literal("*").setStyle(FiguraMod.getAccentColor()).append(name).append("*");

            font.draw(stack, name, x + 1, y + 1 - font.lineHeight, 0xFFFFFF);
            font.draw(stack, value.copy().setStyle(FiguraMod.getAccentColor()), x + width - font.width(value) - 1, y + 1 - font.lineHeight, 0xFFFFFF);
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
