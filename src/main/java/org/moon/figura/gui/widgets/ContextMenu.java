package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class ContextMenu extends AbstractContainerElement {

    public static final ResourceLocation BACKGROUND = new FiguraIdentifier("textures/gui/context.png");

    private final List<AbstractWidget> entries = new ArrayList<>();
    public final GuiEventListener parent;

    public ContextMenu(GuiEventListener parent, int minWidth) {
        super(0, 0, minWidth, 2);
        this.parent = parent;
        this.setVisible(false);
    }

    public ContextMenu(GuiEventListener parent) {
        this(parent, 0);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!isVisible()) return;

        //render
        stack.pushPose();
        stack.translate(0f, 0f, 500f);

        UIHelper.renderSliced(stack, x, y, width, height, BACKGROUND);

        int y = this.y + 1;
        boolean stripe = false;
        for (AbstractWidget entry : entries) {
            if (entry instanceof ContextDivisor)
                stripe = false;

            if (stripe) UIHelper.fill(stack, x + 1, y, x + width - 1, y + entry.getHeight(), 0x22FFFFFF);
            y += entry.getHeight();
            stripe = !stripe;

            entry.render(stack, mouseX, mouseY, delta);
        }

        stack.popPose();
    }

    public void addAction(Component name, Button.OnPress action) {
        ContextButton button = new ContextButton(x, y + this.height, name, action);
        button.shouldHaveBackground(false);

        addElement(button);
    }

    public void addDivisor(Component name) {
        addElement(new ContextDivisor(x, y + this.height, name));
    }

    private void addElement(AbstractWidget element) {
        //add element
        children.add(element);
        entries.add(element);

        //update sizes
        this.width = Math.max(Minecraft.getInstance().font.width(element.getMessage()) + 8, width);
        this.height += element.getHeight();

        //fix buttons width
        for (AbstractWidget entry : entries)
            entry.setWidth(this.width - 2);
    }

    public void setPos(int x, int y) {
        //fix out of screen
        int realWidth = x + width;
        int clientWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        if (realWidth > clientWidth)
            x -= (realWidth - clientWidth);

        int realHeight = y + height;
        int clientHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        if (realHeight > clientHeight)
            y -= (realHeight - clientHeight);

        //apply changes
        this.x = x;
        this.y = y;

        int heigth = y + 1;
        for (AbstractWidget button : entries) {
            button.x = x + 1;
            button.y = heigth;
            heigth += button.getHeight();
        }
    }

    public List<AbstractWidget> getEntries() {
        return entries;
    }

    public static class ContextButton extends TexturedButton {

        public ContextButton(int x, int y, Component text, OnPress pressAction) {
            super(x, y, 0, 16, text, null, pressAction);
        }

        @Override
        protected void renderText(PoseStack stack) {
            //draw text
            Font font = Minecraft.getInstance().font;
            font.drawShadow(
                    stack, getMessage(),
                    this.x + 3, this.y + this.height / 2f - font.lineHeight / 2f,
                    (!this.active ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE).getColor()
            );
        }
    }

    public static class ContextDivisor extends AbstractWidget {

        public ContextDivisor(int x, int y, Component message) {
            super(x, y, 0, 24, message);
        }

        @Override
        public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
            Font font = Minecraft.getInstance().font;

            //draw lines
            int y = this.y + this.height / 2 + font.lineHeight / 2 + 2;
            fill(stack, this.x + 4, y, this.x + this.width - 8, y + 1, 0xFF000000 + ChatFormatting.DARK_GRAY.getColor());

            //draw text
            font.drawShadow(
                    stack, getMessage(),
                    this.x + this.width / 2f - font.width(getMessage()) / 2f, this.y + this.height / 2f - font.lineHeight / 2f - 1,
                    0xFFFFFF
            );
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return isMouseOver(mouseX, mouseY);
        }

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {
        }
    }
}
