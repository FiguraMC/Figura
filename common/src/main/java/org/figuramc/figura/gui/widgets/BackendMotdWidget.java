package org.figuramc.figura.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.Objects;

public class BackendMotdWidget extends AbstractScrollWidget {
    private final Font font;
    private final MultiLineTextWidget multilineWidget;

    public BackendMotdWidget(int i, int j, int k, int l, Component component, Font textRenderer) {
        super(i, j, k, l, component);
        this.font = textRenderer;
        this.multilineWidget = (new MultiLineTextWidget(0, 0, component, textRenderer)).setMaxWidth(this.getWidth() - this.totalInnerPadding());
    }

    public BackendMotdWidget setColor(int i) {
        this.multilineWidget.setColor(i);
        return this;
    }

    public void setWidth(int value) {
        super.setWidth(value);
        this.multilineWidget.setMaxWidth(this.getWidth() - this.totalInnerPadding());
    }

    protected int getInnerHeight() {
        return this.multilineWidget.getHeight();
    }

    protected double scrollRate() {
        Objects.requireNonNull(this.font);
        return 9.0;
    }

    @Override
    protected void renderBorder(GuiGraphics graphics, int x, int y, int width, int height) {
        UIHelper.blitSliced(graphics, this.getX() - this.innerPadding(), this.getY() - this.innerPadding(), this.getWidth() + this.totalInnerPadding(), this.getHeight() + this.totalInnerPadding(), UIHelper.OUTLINE_FILL);
    }

    @Override
    protected void renderBackground(GuiGraphics graphics) {
        UIHelper.blitSliced(graphics, this.getX() - this.innerPadding(), this.getY() - this.innerPadding(), this.getWidth() + this.totalInnerPadding(), this.getHeight() + this.totalInnerPadding(), UIHelper.OUTLINE_FILL);
    }

    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            if (!this.scrollbarVisible()) {
                this.renderBackground(graphics);
                graphics.pose().pushPose();
                graphics.pose().translate((float)this.getX(), (float)this.getY(), 0.0F);
                this.multilineWidget.render(graphics, mouseX, mouseY, delta);
                graphics.pose().popPose();
            } else {
                super.renderWidget(graphics, mouseX, mouseY, delta);
            }

        }
    }

    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.pose().pushPose();
        graphics.pose().translate((float)(this.getX() + this.innerPadding()), (float)(this.getY() + this.innerPadding()), 0.0F);
        this.multilineWidget.render(graphics, mouseX, mouseY, delta);
        graphics.pose().popPose();
    }

    protected void updateWidgetNarration(NarrationElementOutput builder) {
        builder.add(NarratedElementType.TITLE, this.getMessage());
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean shouldRender() {
        return getScrollBarHeight() > 0 && this.height >= 48;
    }
}
