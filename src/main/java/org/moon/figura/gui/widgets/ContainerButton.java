package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.gui.widgets.lists.AbstractList;
import org.moon.figura.utils.TextUtils;

public class ContainerButton extends SwitchButton {

    private final AbstractList parent;

    public ContainerButton(AbstractList parent, int x, int y, int width, int height, Component text, Component tooltip, OnPress pressAction) {
        super(x, y, width, height, text, tooltip, pressAction);
        this.parent = parent;
    }

    @Override
    protected void renderText(PoseStack stack) {
        //variables
        Font font = Minecraft.getInstance().font;
        int color = (!this.active || !this.isToggled() ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE).getColor();
        Component arrow = new TextComponent(this.toggled ? "V" : "^").setStyle(Style.EMPTY.withFont(TextUtils.FIGURA_FONT));
        int arrowWidth = font.width(arrow);
        Component message = TextUtils.trimToWidthEllipsis(font, getMessage(), this.width - arrowWidth - 6);

        //draw text
        font.drawShadow(
                stack, message,
                this.x + 3, this.y + this.height / 2f - font.lineHeight / 2f,
                color
        );

        //draw arrow
        font.drawShadow(
                stack, arrow,
                this.x + this.width - arrowWidth - 3, this.y + this.height / 2f - font.lineHeight / 2f,
                color
        );

        //tooltip
        if (message != getMessage())
            this.setTooltip(getMessage());
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
    }
}
