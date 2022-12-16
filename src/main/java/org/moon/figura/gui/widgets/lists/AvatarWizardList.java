package org.moon.figura.gui.widgets.lists;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import org.moon.figura.FiguraMod;
import org.moon.figura.gui.widgets.SwitchButton;
import org.moon.figura.gui.widgets.TextField;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.ui.UIHelper;
import org.moon.figura.wizards.AvatarWizard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AvatarWizardList extends AbstractList {

    private final AvatarWizard wizard;
    private final Map<Component, List<GuiEventListener>> map = new LinkedHashMap<>();

    public AvatarWizardList(int x, int y, int width, int height, AvatarWizard wizard) {
        super(x, y, width, height);
        this.wizard = wizard;
        generate();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //background and scissors
        UIHelper.renderSliced(stack, x, y, width, height, UIHelper.OUTLINE);
        UIHelper.setupScissor(x + scissorsX, y + scissorsY, width + scissorsWidth, height + scissorsHeight);

        //scrollbar
        Font font = Minecraft.getInstance().font;
        int lineHeight = font.lineHeight + 8;
        int entryHeight = 24;

        int size = 0;
        for (List<GuiEventListener> list : map.values()) {
            for (GuiEventListener widget : list) {
                if (widget instanceof WizardInputBox ib) {
                    ib.setVisible(wizard.checkDependency(ib.entry));
                    if (ib.isVisible()) size++;
                } else if (widget instanceof WizardToggleButton tb) {
                    tb.visible = wizard.checkDependency(tb.entry);
                    if (tb.visible) size++;
                }
            }
        }
        int totalHeight = entryHeight * size + lineHeight * map.size();

        scrollBar.visible = totalHeight > height;
        scrollBar.setScrollRatio(entryHeight, totalHeight - height);

        //render list
        int yOffset = scrollBar.visible ? (int) -(Mth.lerp(scrollBar.getScrollProgress(), -4, totalHeight - height)) : 4;
        for (Map.Entry<Component, List<GuiEventListener>> entry : map.entrySet()) {
            List<GuiEventListener> value = entry.getValue();
            if (value.isEmpty())
                continue;

            int newY = yOffset + lineHeight;
            //elements
            for (GuiEventListener widget : entry.getValue()) {
                if (widget instanceof AbstractWidget w) {
                    if (w.visible) {
                        w.setY(y + newY);
                        newY += entryHeight;
                    }
                } else if (widget instanceof TextField t) {
                    if (t.isVisible()) {
                        t.setPos(t.x, y + newY);
                        newY += entryHeight;
                    }
                }
            }

            if (newY == yOffset + lineHeight)
                continue;

            //category
            UIHelper.drawCenteredString(stack, font, entry.getKey(), x + width / 2, y + yOffset + 4, 0xFFFFFF);
            yOffset = newY;
        }

        //children
        super.render(stack, mouseX, mouseY, delta);

        //reset scissor
        RenderSystem.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //fix mojang focusing for text fields
        for (GuiEventListener widget : children()) {
            if (widget instanceof TextField field)
                field.getField().setFocus(field.isEnabled() && field.isMouseOver(mouseX, mouseY));
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void generate() {
        for (List<GuiEventListener> value : map.values())
            children.removeAll(value);
        map.clear();

        int x = this.x + width / 2 + 4;
        int width = this.width / 2 - 20;

        Component lastName = Component.empty();
        List<GuiEventListener> lastList = new ArrayList<>();

        for (AvatarWizard.WizardEntry value : AvatarWizard.WizardEntry.values()) {
            switch (value.getType()) {
                case CATEGORY -> {
                    if (!lastList.isEmpty()) {
                        map.put(lastName, lastList);
                        children.addAll(lastList);
                    }

                    lastName = FiguraText.of("gui.avatar_wizard." + value.name().toLowerCase());
                    lastList = new ArrayList<>();
                }
                case TEXT -> lastList.add(new WizardInputBox(x, width, this, value));
                case TOGGLE -> lastList.add(new WizardToggleButton(x, width, this, value));
            }
        }

        map.put(lastName, lastList);
        children.addAll(lastList);
    }

    private static class WizardInputBox extends TextField {

        private final AvatarWizardList parent;
        private final AvatarWizard.WizardEntry entry;
        private final Component name;

        public WizardInputBox(int x, int width, AvatarWizardList parent, AvatarWizard.WizardEntry entry) {
            super(x, 0, width, 20, HintType.ANY, s -> parent.wizard.changeEntry(entry, s));
            this.parent = parent;
            this.entry = entry;
            this.name = FiguraText.of("gui.avatar_wizard." + entry.name().toLowerCase());
        }

        @Override
        public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
            if (!isVisible()) return;
            super.render(stack, mouseX, mouseY, delta);

            Font font = Minecraft.getInstance().font;
            MutableComponent name = this.name.copy();
            if (!this.getField().getValue().isBlank())
                name.setStyle(FiguraMod.getAccentColor());
            font.draw(stack, name, x - width - 8, y + (height - font.lineHeight) / 2, 0xFFFFFF);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
        }
    }

    private static class WizardToggleButton extends SwitchButton {

        private final AvatarWizardList parent;
        private final AvatarWizard.WizardEntry entry;
        private final Component name;

        public WizardToggleButton(int x, int width, AvatarWizardList parent, AvatarWizard.WizardEntry entry) {
            super(x, 0, width, 20, false);
            this.parent = parent;
            this.entry = entry;
            this.name = FiguraText.of("gui.avatar_wizard." + entry.name().toLowerCase());
        }

        @Override
        public void onPress() {
            super.onPress();
            parent.wizard.changeEntry(entry, this.isToggled());
        }

        @Override
        protected void renderTexture(PoseStack stack, float delta) {
            //button
            stack.pushPose();
            stack.translate(width - textureWidth, 0, 0);
            super.renderTexture(stack, delta);
            stack.popPose();

            //name
            Font font = Minecraft.getInstance().font;
            MutableComponent name = this.name.copy();
            if (this.isToggled())
                name.withStyle(FiguraMod.getAccentColor());
            font.draw(stack, name, getX() - width - 8, getY() + (height - font.lineHeight) / 2, 0xFFFFFF);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
        }
    }
}
