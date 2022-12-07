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
import org.moon.figura.trust.Trust;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.ui.UIHelper;

import java.util.*;
import java.util.function.Predicate;

public class TrustList extends AbstractList {

    public boolean precise = false;

    private final Map<String, List<GuiEventListener>> trusts = new LinkedHashMap<>();

    public TrustList(int x, int y, int width, int height) {
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
        for (List<GuiEventListener> value : trusts.values())
            size += value.size();
        int totalHeight = size * entryHeight;

        boolean titles = trusts.size() > 1;
        if (titles) totalHeight += trusts.size() * titleHeight;

        scrollBar.setY(y + 4);
        scrollBar.visible = totalHeight > height;
        scrollBar.setScrollRatio(entryHeight, totalHeight - height);

        //render
        int xOffset = scrollBar.visible ? 8 : 15;
        int yOffset = scrollBar.visible ? (int) -(Mth.lerp(scrollBar.getScrollProgress(), -16, totalHeight - height)) : 16;

        for (Map.Entry<String, List<GuiEventListener>> entry : trusts.entrySet()) {
            //titles
            if (titles) {
                UIHelper.drawCenteredString(stack, font, Component.translatable(entry.getKey()), x + (width - xOffset) / 2, y + yOffset, 0xFFFFFF);
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

    public void updateList(TrustContainer container) {
        //clear old widgets
        for (List<GuiEventListener> list : trusts.values())
            list.forEach(children::remove);
        trusts.clear();

        //add new trusts

        //defaults
        trusts.put(FiguraMod.MOD_ID, generateWidgets(container, Trust.DEFAULT, FiguraMod.MOD_ID));

        //custom
        for (Map.Entry<String, Collection<Trust>> entry : TrustManager.CUSTOM_TRUST.entrySet())
            trusts.put(entry.getKey(), generateWidgets(container, entry.getValue(), entry.getKey()));
    }

    private List<GuiEventListener> generateWidgets(TrustContainer container, Collection<Trust> coll, String id) {
        List<GuiEventListener> list = new ArrayList<>();

        for (Trust trust : coll) {
            int lineHeight = Minecraft.getInstance().font.lineHeight;

            GuiEventListener widget;
            if (!trust.isToggle) {
                if (!precise)
                    widget = new TrustSlider(x + 8, y, width - 30, 11 + lineHeight, container, trust, this, id);
                else
                    widget = new TrustField(x + 8, y, width - 30, 11 + lineHeight, container, trust, this, id);
            } else {
                widget = new TrustSwitch(x + 8, y, width - 30, 20 + lineHeight, container, trust, this, id);
            }

            list.add(widget);
            children.add(widget);
        }

        return list;
    }

    private static class TrustSlider extends SliderWidget {

        private static final Component INFINITY = FiguraText.of("trust.infinity");

        private final TrustContainer container;
        private final Trust trust;
        private final TrustList parent;
        private final String id;
        private Component value;
        private boolean changed;

        public TrustSlider(int x, int y, int width, int height, TrustContainer container, Trust trust, TrustList parent, String id) {
            super(x, y, width, height, Mth.clamp(container.get(trust) / (trust.max + 1d), 0d, 1d), trust.max / trust.stepSize + 1, trust.showSteps());
            this.container = container;
            this.trust = trust;
            this.parent = parent;
            this.id = id;
            this.value = container.get(trust) == Integer.MAX_VALUE ? INFINITY : Component.literal(String.valueOf(container.get(trust)));
            this.changed = container.isChanged(trust);

            setAction(slider -> {
                //update trust
                int value = this.showSteps ? ((SliderWidget) slider).getIntValue() * trust.stepSize : (int) ((trust.max + 1d) * slider.getScrollProgress());
                boolean infinity = trust.checkInfinity(value);

                container.insert(trust, infinity ? Integer.MAX_VALUE : value, id);
                changed = container.isChanged(trust);

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
            MutableComponent name = Component.translatable(id + ".trust.value." + trust.name.toLowerCase());
            if (changed) name = Component.literal("*").setStyle(FiguraMod.getAccentColor()).append(name).append("*");

            font.draw(stack, name, getX() + 1, getY() + 1, 0xFFFFFF);
            font.draw(stack, value.copy().setStyle(FiguraMod.getAccentColor()), getX() + width - font.width(value) - 1, getY() + 1, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.active || !this.isHoveredOrFocused() || !this.isMouseOver(mouseX, mouseY))
                return false;

            if (button == 1) {
                container.reset(trust);
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

    private static class TrustSwitch extends SwitchButton {

        private final TrustContainer container;
        private final Trust trust;
        private final TrustList parent;
        private final String id;
        private Component value;
        private boolean changed;

        public TrustSwitch(int x, int y, int width, int height, TrustContainer container, Trust trust, TrustList parent, String id) {
            super(x, y, width, height, trust.asBoolean(container.get(trust)));
            this.container = container;
            this.trust = trust;
            this.parent = parent;
            this.id = id;
            this.changed = container.isChanged(trust);
            this.value = FiguraText.of("trust." + (toggled ? "enabled" : "disabled"));
        }

        @Override
        public void onPress() {
            //update trust
            boolean value = !this.isToggled();

            container.insert(trust, value ? 1 : 0, id);
            this.changed = container.isChanged(trust);

            //update text
            this.value = FiguraText.of("trust." + (value ? "enabled" : "disabled"));

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
            MutableComponent name = Component.translatable(id + ".trust.value." + trust.name.toLowerCase());
            if (changed) name = Component.literal("*").setStyle(FiguraMod.getAccentColor()).append(name).append("*");

            font.draw(stack, name, getX() + 1, getY() + 1, 0xFFFFFF);
            font.draw(stack, value.copy().setStyle(FiguraMod.getAccentColor()), getX() + width - font.width(value) - 1, getY() + 1, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.active || !this.isHoveredOrFocused() || !this.isMouseOver(mouseX, mouseY))
                return false;

            if (button == 1) {
                container.reset(trust);
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

    private static class TrustField extends TextField {

        private final static Predicate<String> validator = s -> {
            try {
                Integer i = Integer.parseInt(s);
                return i >= 0;
            } catch (Exception ignored) {
                return false;
            }
        };

        private final TrustContainer container;
        private final Trust trust;
        private final TrustList parent;
        private final String id;
        private Component value;
        private boolean changed;

        public TrustField(int x, int y, int width, int height, TrustContainer container, Trust trust, TrustList parent, String id) {
            super(x, y, width, height, null, null);

            this.container = container;
            this.trust = trust;
            this.parent = parent;
            this.id = id;
            String val = String.valueOf(container.get(trust));
            this.value = Component.literal(val);
            this.changed = container.isChanged(trust);

            this.getField().setValue(val);
            this.getField().setResponder(text -> {
                if (!validator.test(text))
                    return;

                int value = Integer.parseInt(text);

                container.insert(trust, value, id);
                changed = container.isChanged(trust);

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
            MutableComponent name = Component.translatable(id + ".trust.value." + trust.name.toLowerCase());
            if (changed) name = Component.literal("*").setStyle(FiguraMod.getAccentColor()).append(name).append("*");

            font.draw(stack, name, x + 1, y + 1 - font.lineHeight, 0xFFFFFF);
            font.draw(stack, value.copy().setStyle(FiguraMod.getAccentColor()), x + width - font.width(value) - 1, y + 1 - font.lineHeight, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.isEnabled() || !this.isMouseOver(mouseX, mouseY))
                return false;

            if (button == 1) {
                container.reset(trust);
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
