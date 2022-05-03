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
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class ContextMenu extends AbstractContainerElement {

    public static final ResourceLocation BACKGROUND = new FiguraIdentifier("textures/gui/context.png");

    private final List<AbstractWidget> entries = new ArrayList<>();
    public GuiEventListener parent;

    public ContextMenu(GuiEventListener parent, int minWidth) {
        super(0, 0, minWidth, 2);
        this.parent = parent;
        this.setVisible(false);
    }

    public ContextMenu(GuiEventListener parent) {
        this(parent, 0);
    }

    public ContextMenu() {
        this(null);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!isVisible()) return;

        //render
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

            if (entry instanceof TabButton tab) {
                tab.context.setVisible(tab.isHoveredOrFocused() || (tab.context.isVisible() && tab.context.isMouseOver(mouseX, mouseY)));
                if (tab.context.isVisible()) {
                    tab.setHovered(true);
                    tab.context.render(stack, mouseX, mouseY, delta);
                }
            }
        }
    }

    public void addAction(Component name, Button.OnPress action) {
        addElement(new ContextButton(x, y + this.height, name, action));
    }

    public void addDivisor() {
        addElement(new ContextDivisor(x, y + this.height));
    }

    public void addTab(Component name, ContextMenu context) {
        //context
        this.children.add(context);

        //button
        ContextButton button = new TabButton(x, y + this.height, name, context);
        addElement(button);
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

            if (button instanceof TabButton tab)
                tab.context.setPos(tab.x + tab.getWidth(), tab.y);
        }
    }

    public List<AbstractWidget> getEntries() {
        return entries;
    }

    public static class ContextButton extends TexturedButton {

        public ContextButton(int x, int y, Component text, OnPress pressAction) {
            super(x, y, 0, 16, text, null, pressAction);
            this.shouldHaveBackground(false);
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

        public ContextDivisor(int x, int y) {
            super(x, y, 0, 9, TextComponent.EMPTY);
        }

        @Override
        public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
            //draw line
            fill(stack, this.x + 4, y + 4, this.x + this.width - 4, y + 5, 0xFF000000 + ChatFormatting.DARK_GRAY.getColor());
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {
        }
    }

    public static class TabButton extends ContextButton {

        private final ContextMenu context;

        public TabButton(int x, int y, Component text, ContextMenu context) {
            super(x, y, text, button -> {});
            this.context = context;
        }

        @Override
        public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
            //super
            super.renderButton(stack, mouseX, mouseY, delta);

            //draw arrow
            Font font = Minecraft.getInstance().font;
            Component arrow = new TextComponent(">").setStyle(Style.EMPTY.withFont(TextUtils.FIGURA_FONT));
            font.drawShadow(
                    stack, arrow,
                    this.x + this.width - font.width(arrow) - 3, this.y + this.height / 2f - font.lineHeight / 2f,
                    (!this.active ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE).getColor()
            );
        }

        //suppress events
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return false;
        }
    }
}
