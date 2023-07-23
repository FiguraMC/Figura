package org.figuramc.figura.gui.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class ContextMenu extends AbstractContainerElement {

    public static final ResourceLocation BACKGROUND = new FiguraIdentifier("textures/gui/context.png");

    private final int minWidth;
    private final List<ContextButton> entries = new ArrayList<>();

    public GuiEventListener parent;

    private ContextMenu nestedContext;

    public ContextMenu(GuiEventListener parent, int minWidth) {
        super(0, 0, minWidth, 2);
        this.minWidth = minWidth;
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
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        if (!isVisible()) return;

        // outline
        UIHelper.blitSliced(gui, getX(), getY(), getWidth(), getHeight(), 0f, 0f, 16, 16, 48, 16, BACKGROUND);

        for (int i = 0, y = getY() + 1; i < entries.size(); i++) {
            int height = entries.get(i).getHeight();

            // background
            UIHelper.blitSliced(gui, getX() + 1, y, getWidth() - 2, height, i % 2 == 1 ? 32f : 16f, 0f, 16, 16, 48, 16, BACKGROUND);

            // button
            entries.get(i).render(gui, mouseX, mouseY, delta);

            y += height;
        }

        if (nestedContext != null) {
            nestedContext.render(gui, mouseX, mouseY, delta);
            if (nestedContext.parent instanceof Button button)
                button.setHovered(true);
        }
    }

    public void addAction(Component name, Component tooltip, Button.OnPress action) {
        addElement(new ContextButton(getX(), getY() + getHeight(), name, tooltip, this, action));
    }

    public void addDivisor() {
        addElement(new ContextDivisor(getX(), getY() + getHeight()));
    }

    public void addTab(Component name, Component tooltip, ContextMenu context) {
        // button
        ContextButton button = new TabButton(getX(), getY() + getHeight(), name, tooltip, this, context);
        addElement(button);

        // context
        context.parent = button;
        this.children.add(context);
    }

    private void addElement(ContextButton element) {
        // add element
        children.add(element);
        entries.add(element);

        // update size
        updateDimensions();
    }

    private void clearNest() {
        if (this.nestedContext != null) {
            this.nestedContext.clearNest();
            this.nestedContext = null;
        }
    }

    public void updateDimensions() {
        this.setWidth(minWidth);
        this.setHeight(2);

        for (ContextButton entry : entries) {
            this.setWidth(Math.max(entry.getTrueWidth() + 8, getWidth()));
            this.setHeight(getHeight() + entry.getHeight());
        }

        // fix buttons width
        for (ContextButton entry : entries)
            entry.setWidth(getWidth() - 2);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        clearNest();
    }

    @Override
    public void setX(int x) {
        // fix out of screen
        int realWidth = x + getWidth();
        int clientWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        if (realWidth > clientWidth)
            x -= (realWidth - clientWidth);

        // apply changes
        super.setX(x);

        // children
        for (ContextButton button : entries) {
            button.setX(x + 1);
            if (button instanceof TabButton tab)
                tab.context.setX(tab.getX() + tab.getWidth());
        }
    }

    @Override
    public void setY(int y) {
        // fix out of screen
        int realHeight = y + getHeight();
        int clientHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        if (realHeight > clientHeight)
            y -= (realHeight - clientHeight);

        // apply changes
        super.setY(y);

        // children
        int heigth = y + 1;
        for (ContextButton button : entries) {
            button.setY(heigth);
            heigth += button.getHeight();

            if (button instanceof TabButton tab)
                tab.context.setY(tab.getY() - 1);
        }
    }

    public List<? extends AbstractWidget> getEntries() {
        return entries;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        setFocused(null);
        return result;
    }

    private static class ContextButton extends Button {

        protected final ContextMenu parent;

        public ContextButton(int x, int y, Component text, Component tooltip, ContextMenu parent, OnPress pressAction) {
            super(x, y, 0, 16, text, tooltip, pressAction);
            this.shouldHaveBackground(false);
            this.parent = parent;
        }

        protected ContextButton(int x, int y, int height) {
            super(x, y, 0, height, Component.empty(), null, button -> {});
            this.shouldHaveBackground(false);
            this.parent = null;
        }

        @Override
        protected void renderText(GuiGraphics gui, float delta) {
            // draw text
            Font font = Minecraft.getInstance().font;
            gui.drawString(
                    font, getMessage(),
                    this.getX() + 3, (int) (this.getY() + this.getHeight() / 2f - font.lineHeight / 2f),
                    getTextColor()
            );
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (UIHelper.isMouseOver(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY, true)) {
                UIHelper.setTooltip(this.tooltip);
                parent.clearNest();
                return true;
            }

            return false;
        }

        public int getTrueWidth() {
            return Minecraft.getInstance().font.width(getMessage());
        }
    }

    private static class ContextDivisor extends ContextButton {

        public ContextDivisor(int x, int y) {
            super(x, y, 9);
        }

        @Override
        public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float delta) {
            // draw line
            gui.fill(this.getX() + 4, getY() + 4, this.getX() + this.getWidth() - 4, getY() + 5, 0xFF000000 + ChatFormatting.DARK_GRAY.getColor());
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }
    }

    private static class TabButton extends ContextButton {

        private static final Component ARROW = Component.literal(">").setStyle(Style.EMPTY.withFont(UIHelper.UI_FONT));
        private final ContextMenu context;

        public TabButton(int x, int y, Component text, Component tooltip, ContextMenu parent, ContextMenu context) {
            super(x, y, text, tooltip, parent, button -> {});
            this.context = context;
            this.context.setVisible(true);
        }

        @Override
        public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float delta) {
            // super
            super.renderWidget(gui, mouseX, mouseY, delta);

            // draw arrow
            Font font = Minecraft.getInstance().font;
            gui.drawString(
                    font, ARROW,
                    this.getX() + this.getWidth() - font.width(ARROW) - 3, (int) (this.getY() + this.getHeight() / 2f - font.lineHeight / 2f),
                    getTextColor()
            );
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            boolean mouseOver = UIHelper.isMouseOver(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY, true);
            if (mouseOver || parent.nestedContext == context) {
                UIHelper.setTooltip(this.tooltip);
                parent.nestedContext = context;
                if (mouseOver) context.nestedContext = null;
                return true;
            }

            return false;
        }

        @Override
        public int getTrueWidth() {
            return super.getTrueWidth() + Minecraft.getInstance().font.width(Component.literal(" ").append(ARROW));
        }
    }
}
