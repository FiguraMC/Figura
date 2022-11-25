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
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class ContextMenu extends AbstractContainerElement {

    public static final ResourceLocation BACKGROUND = new FiguraIdentifier("textures/gui/context.png");

    private final List<AbstractWidget> entries = new ArrayList<>();
    public GuiEventListener parent;

    private ContextMenu nestedContext;

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

        //outline
        UIHelper.renderSliced(stack, x, y, width, height, 0f, 0f, 16, 16, 48, 16, BACKGROUND);

        for (int i = 0, y = this.y + 1; i < entries.size(); i++) {
            int height = entries.get(i).getHeight();

            //background
            UIHelper.renderSliced(stack, x + 1, y, width - 2, height, i % 2 == 1 ? 32f : 16f, 0f, 16, 16, 48, 16, BACKGROUND);

            //button
            entries.get(i).render(stack, mouseX, mouseY, delta);

            y += height;
        }

        if (nestedContext != null) {
            nestedContext.render(stack, mouseX, mouseY, delta);
            if (nestedContext.parent instanceof TexturedButton button)
                button.setHovered(true);
        }
    }

    public void addAction(Component name, Button.OnPress action) {
        addElement(new ContextButton(x, y + this.height, name, this, action));
    }

    public void addDivisor() {
        addElement(new ContextDivisor(x, y + this.height));
    }

    public void addTab(Component name, ContextMenu context) {
        //button
        ContextButton button = new TabButton(x, y + this.height, name, this, context);
        addElement(button);

        //context
        context.parent = button;
        this.children.add(context);
    }

    private void addElement(AbstractWidget element) {
        //add element
        children.add(element);
        entries.add(element);

        //update sizes
        this.width = Math.max(element.getWidth(), width);
        this.height += element.getHeight();

        //fix buttons width
        for (AbstractWidget entry : entries)
            entry.setWidth(this.width - 2);
    }

    private void clearNest() {
        if (this.nestedContext != null) {
            this.nestedContext.clearNest();
            this.nestedContext = null;
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        clearNest();
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
                tab.context.setPos(tab.x + tab.getWidth(), tab.y - 1);
        }
    }

    public List<AbstractWidget> getEntries() {
        return entries;
    }

    private static class ContextButton extends TexturedButton {

        protected final ContextMenu parent;

        public ContextButton(int x, int y, Component text, ContextMenu parent, OnPress pressAction) {
            super(x, y, Minecraft.getInstance().font.width(text) + 8, 16, text, null, pressAction);
            this.shouldHaveBackground(false);
            this.parent = parent;
        }

        @Override
        protected void renderText(PoseStack stack) {
            //draw text
            Font font = Minecraft.getInstance().font;
            font.drawShadow(
                    stack, getMessage(),
                    this.x + 3, this.y + this.height / 2 - font.lineHeight / 2,
                    (!this.active ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE).getColor()
            );
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (UIHelper.isMouseOver(x, y, width, height, mouseX, mouseY, true)) {
                UIHelper.setTooltip(this.tooltip);
                parent.clearNest();
                return true;
            }

            return false;
        }
    }

    private static class ContextDivisor extends AbstractWidget {

        public ContextDivisor(int x, int y) {
            super(x, y, 0, 9, Component.empty());
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

    private static class TabButton extends ContextButton {

        private static final Component ARROW = Component.literal(">").setStyle(Style.EMPTY.withFont(UIHelper.UI_FONT));
        private final ContextMenu context;

        public TabButton(int x, int y, Component text, ContextMenu parent, ContextMenu context) {
            super(x, y, text.copy().append(" ").append(ARROW), parent, button -> {});
            this.setMessage(text);
            this.context = context;
            this.context.setVisible(true);
        }

        @Override
        public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
            //super
            super.renderButton(stack, mouseX, mouseY, delta);

            //draw arrow
            Font font = Minecraft.getInstance().font;
            font.drawShadow(
                    stack, ARROW,
                    this.x + this.width - font.width(ARROW) - 3, this.y + this.height / 2 - font.lineHeight / 2,
                    (!this.active ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE).getColor()
            );
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (UIHelper.isMouseOver(x, y, width, height, mouseX, mouseY, true)) {
                UIHelper.setTooltip(this.tooltip);
                parent.nestedContext = context;
                return true;
            }

            return false;
        }
    }
}
