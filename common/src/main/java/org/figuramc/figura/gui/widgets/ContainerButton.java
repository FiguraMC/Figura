package org.figuramc.figura.gui.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.gui.widgets.lists.AbstractList;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class ContainerButton extends SwitchButton {

    private final AbstractList parent;

    public ContainerButton(AbstractList parent, int x, int y, int width, int height, Component text, Component tooltip, OnPress pressAction) {
        super(x, y, width, height, text, tooltip, pressAction);
        this.parent = parent;
    }

    @Override
    protected void renderText(GuiGraphics gui, float delta) {
        // variables
        Font font = Minecraft.getInstance().font;
        int color = getTextColor();
        Component arrow = this.toggled ? UIHelper.DOWN_ARROW : UIHelper.UP_ARROW;
        int arrowWidth = font.width(arrow);
        Component message = TextUtils.trimToWidthEllipsis(font, getMessage(), this.getWidth() - arrowWidth - 6, TextUtils.ELLIPSIS.copy().withStyle(getMessage().getStyle()));

        // draw text
        gui.drawString(
                font, message,
                this.getX() + arrowWidth + 6, (int) (this.getY() + this.getHeight() / 2f - font.lineHeight / 2f),
                color
        );

        // draw arrow
        gui.drawString(
                font, arrow,
                this.getX() + 3, (int) (this.getY() + this.getHeight() / 2f - font.lineHeight / 2f),
                color
        );

        // tooltip
        if (message != getMessage())
            this.setTooltip(getMessage());
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
    }

    @Override
    protected int getTextColor() {
        return !this.isToggled() ? ChatFormatting.DARK_GRAY.getColor() : super.getTextColor();
    }
}
