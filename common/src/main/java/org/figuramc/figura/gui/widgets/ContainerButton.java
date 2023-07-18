package org.figuramc.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
<<<<<<< HEAD:src/main/java/org/moon/figura/gui/widgets/ContainerButton.java
import org.moon.figura.gui.widgets.lists.AbstractList;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;
=======
import org.figuramc.figura.gui.widgets.lists.AbstractList;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;
>>>>>>> 8b2c2606120aac4f05d8dd5820ea17317422dc93:common/src/main/java/org/figuramc/figura/gui/widgets/ContainerButton.java

public class ContainerButton extends SwitchButton {

    private final AbstractList parent;

    public ContainerButton(AbstractList parent, int x, int y, int width, int height, Component text, Component tooltip, OnPress pressAction) {
        super(x, y, width, height, text, tooltip, pressAction);
        this.parent = parent;
    }

    @Override
    protected void renderText(PoseStack stack, float delta) {
        //variables
        Font font = Minecraft.getInstance().font;
        int color = getTextColor();
        Component arrow = this.toggled ? UIHelper.DOWN_ARROW : UIHelper.UP_ARROW;
        int arrowWidth = font.width(arrow);
        Component message = TextUtils.trimToWidthEllipsis(font, getMessage(), this.getWidth() - arrowWidth - 6, TextUtils.ELLIPSIS.copy().withStyle(getMessage().getStyle()));

        //draw text
        font.drawShadow(
                stack, message,
                this.getX() + arrowWidth + 6, (int) (this.getY() + this.getHeight() / 2f - font.lineHeight / 2f),
                color
        );

        //draw arrow
        font.drawShadow(
                stack, arrow,
                this.getX() + 3, (int) (this.getY() + this.getHeight() / 2f - font.lineHeight / 2f),
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

    @Override
    protected int getTextColor() {
        return !this.isToggled() ? ChatFormatting.DARK_GRAY.getColor() : super.getTextColor();
    }
}
