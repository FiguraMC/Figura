package org.moon.figura.gui.widgets.lists;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import org.moon.figura.gui.widgets.SliderWidget;
import org.moon.figura.gui.widgets.SwitchButton;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class TrustList extends AbstractList {

    private final List<TrustSlider> sliders = new ArrayList<>();
    private final List<TrustSwitch> switches = new ArrayList<>();

    public TrustList(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //background and scissors
        UIHelper.renderSliced(stack, x, y, width, height, UIHelper.OUTLINE);
        UIHelper.setupScissor(x + scissorsX, y + scissorsY, width + scissorsWidth, height + scissorsHeight);

        //scrollbar
        int lineHeight = Minecraft.getInstance().font.lineHeight;
        int entryHeight = 27 + lineHeight; //11 (slider) + font height + 16 (padding)
        int totalHeight = (sliders.size() + switches.size()) * entryHeight;
        scrollBar.y = y + 4;
        scrollBar.visible = totalHeight > height;
        scrollBar.setScrollRatio(entryHeight, totalHeight - height);

        //render sliders
        int xOffset = scrollBar.visible ? 8 : 15;
        int yOffset = scrollBar.visible ? (int) -(Mth.lerp(scrollBar.getScrollProgress(), -16, totalHeight - height)) : 16;
        for (TrustSlider slider : sliders) {
            slider.x = x + xOffset;
            slider.y = y + yOffset;
            yOffset += 27 + lineHeight;
        }

        //render switches
        for (TrustSwitch trustSwitch : switches) {
            trustSwitch.x = x + xOffset;
            trustSwitch.y = y + yOffset;
            yOffset += 27 + lineHeight;
        }

        //render children
        super.render(stack, mouseX, mouseY, delta);

        //reset scissor
        RenderSystem.disableScissor();
    }

    public void updateList(TrustContainer container) {
        //clear old widgets
        sliders.forEach(children::remove);
        sliders.clear();

        //clear old switches
        switches.forEach(children::remove);
        switches.clear();

        //add new sliders
        for (TrustContainer.Trust trust : TrustContainer.Trust.values()) {
            int lineHeight = Minecraft.getInstance().font.lineHeight;
            if (!trust.isToggle) {
                TrustSlider slider = new TrustSlider(x + 8, y, width - 30, 11 + lineHeight, container, trust, this);
                sliders.add(slider);
                children.add(slider);
            } else {
                TrustSwitch trustSwitch = new TrustSwitch(x + 8, y, width - 30, 20 + lineHeight, container, trust, this);
                switches.add(trustSwitch);
                children.add(trustSwitch);
            }
        }
    }

    private static class TrustSlider extends SliderWidget {

        private static final Component INFINITY = new FiguraText("trust.infinity");

        private final TrustContainer.Trust trust;
        private final TrustList parent;
        private Component value;
        private boolean changed;

        public TrustSlider(int x, int y, int width, int height, TrustContainer container, TrustContainer.Trust trust, TrustList parent) {
            super(x, y, width, height, Mth.clamp(container.get(trust) / (trust.max + 1f), 0f, 1f));
            this.trust = trust;
            this.parent = parent;
            this.value = trust.checkInfinity(container.get(trust)) ? INFINITY : new TextComponent(String.valueOf(container.get(trust)));
            this.changed = container.getSettings().containsKey(trust);

            setAction(slider -> {
                //update trust
                int value = (int) ((trust.max + 1f) * slider.getScrollProgress());
                boolean infinity = trust.checkInfinity(value);

                container.getSettings().put(trust, infinity ? Integer.MAX_VALUE - 100 : value);
                changed = true;

                //update text
                this.value = infinity ? INFINITY : new TextComponent(String.valueOf(value));
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
            MutableComponent name = new FiguraText("trust." + trust.name().toLowerCase());
            if (changed) name = new TextComponent("*").setStyle(ColorUtils.Colors.FRAN_PINK.style).append(name).append("*");

            font.draw(stack, name, x + 1, y + 1, 0xFFFFFF);
            font.draw(stack, value, x + width - font.width(value) - 1, y + 1, ChatFormatting.AQUA.getColor());
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
        }
    }

    private static class TrustSwitch extends SwitchButton {

        private final TrustContainer container;
        private final TrustContainer.Trust trust;
        private final TrustList parent;
        private Component value;
        private boolean changed;

        public TrustSwitch(int x, int y, int width, int height, TrustContainer container, TrustContainer.Trust trust, TrustList parent) {
            super(x, y, width, height, trust.asBoolean(container.get(trust)));
            this.container = container;
            this.trust = trust;
            this.parent = parent;
            this.changed = container.getSettings().containsKey(trust);
            this.value = new FiguraText("trust." + (toggled ? "enabled" : "disabled"));
        }

        @Override
        public void onPress() {
            //update trust
            boolean value = !this.isToggled();

            this.container.getSettings().put(trust, value ? 1 : 0);
            this.changed = true;

            //update text
            this.value = new FiguraText("trust." + (value ? "enabled" : "disabled"));

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
            MutableComponent name = new FiguraText("trust." + trust.name().toLowerCase());
            if (changed) name = new TextComponent("*").setStyle(ColorUtils.Colors.FRAN_PINK.style).append(name).append("*");

            font.draw(stack, name, x + 1, y + 1, 0xFFFFFF);
            font.draw(stack, value, x + width - font.width(value) - 1, y + 1, ChatFormatting.AQUA.getColor());
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
        }
    }
}
