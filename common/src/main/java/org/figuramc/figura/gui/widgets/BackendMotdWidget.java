package org.figuramc.figura.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.utils.ClickableTextHelper;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.Objects;

public class BackendMotdWidget extends AbstractScrollWidget {
    private final Font font;
    private final ClickableTextHelper textHelper;
    private int maxWidth;

    public BackendMotdWidget(int i, int j, int k, int l, Component text, Font font) {
        super(i, j, k, l, text);
        this.font = font;
        this.textHelper = new ClickableTextHelper();
        this.maxWidth = this.getWidth() - this.totalInnerPadding();
    }

    @Override
    public void setMessage(Component message) {
        super.setMessage(message);
        textHelper.setMessage(message);
    }

    public void setWidth(int value) {
        super.setWidth(value);
        int prevWidth = this.maxWidth;
        this.maxWidth = this.getWidth() - this.totalInnerPadding();
        if (maxWidth != prevWidth)
            textHelper.markDirty();
    }

    protected int getInnerHeight() {
        Objects.requireNonNull(font);
        return textHelper.lineCount() * font.lineHeight;
    }

    protected double scrollRate() {
        Objects.requireNonNull(this.font);
        return font.lineHeight;
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
            if (!scrollbarVisible()) {
                renderBackground(graphics);
                renderContents(graphics, mouseX, mouseY, delta);
            } else {
                super.renderWidget(graphics, mouseX, mouseY, delta);
            }
        }
    }

    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int xx = this.getX() + this.innerPadding();
        int yy = this.getY() + this.innerPadding();

        int scroll = (int)scrollAmount();
        textHelper.update(font, maxWidth);

        textHelper.visit((text, style, x, y, textWidth, textHeight) -> graphics.drawString(font, Component.literal(text).setStyle(style), xx + x, yy + y, 0xFFFFFFFF));

        //textHelper.renderDebug(graphics, xx, yy, mouseX, mouseY + scroll);

        if (withinContentAreaPoint(mouseX, mouseY)) {
            Component tooltip = textHelper.getHoverTooltip(xx, yy, mouseX, mouseY + scroll);
            if (tooltip != null)
                UIHelper.setTooltip(tooltip);

            if (mouseDown) {
                String link = textHelper.getClickLink(xx, yy, mouseX, mouseY + scroll);
                if (link != null)
                    UIHelper.openURL(link).run();

                mouseDown = false;
            }
        }
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        // Don't play the button click sound
    }

    private boolean mouseDown = false;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseDown = super.mouseClicked(mouseX, mouseY, button);
        return mouseDown;
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
