package org.figuramc.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.figuramc.figura.utils.ClickableTextHelper;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BackendMotdWidget extends AbstractWidget implements Widget, GuiEventListener {
    private final Font font;
    private final ClickableTextHelper textHelper;
    private int maxWidth;
    private double scrollAmount;
    private boolean scrolling;

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

    protected int totalInnerPadding() {
        return innerPadding() * 2;
    }

    protected int getInnerHeight() {
        Objects.requireNonNull(font);
        return textHelper.lineCount() * font.lineHeight;
    }

    protected double scrollRate() {
        Objects.requireNonNull(this.font);
        return font.lineHeight;
    }

    protected int innerPadding() {
        return 4;
    }

    protected void renderBorder(PoseStack stack, int x, int y, int width, int height) {
        UIHelper.renderSliced(stack, this.x - this.innerPadding(), this.y - this.innerPadding(), this.getWidth() + this.totalInnerPadding(), this.getHeight() + this.totalInnerPadding(), UIHelper.OUTLINE_FILL);
    }

    protected void renderBackground(PoseStack pose) {
        UIHelper.renderSliced(pose, this.x - this.innerPadding(), this.y - this.innerPadding(), this.getWidth() + this.totalInnerPadding(), this.getHeight() + this.totalInnerPadding(), UIHelper.OUTLINE_FILL);
    }

    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            if (!scrollbarVisible()) {
                renderBackground(pose);
                renderContents(pose, mouseX, mouseY, delta);
            } else {
                super.renderButton(pose, mouseX, mouseY, delta);
            }
        }
    }

    protected void renderContents(PoseStack pose, int mouseX, int mouseY, float delta) {
        int xx = this.x + this.innerPadding();
        int yy = this.y + this.innerPadding();

        int scroll = (int)scrollAmount();
        textHelper.update(font, maxWidth);

        textHelper.visit((text, style, x, y, textWidth, textHeight) -> UIHelper.drawString(pose, font, new TextComponent(text).setStyle(style), xx + x, yy + y, 0xFFFFFFFF));

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
        mouseDown = mouseClickedScroll(mouseX, mouseY, button);
        return mouseDown;
    }
    public void updateNarration(NarrationElementOutput builder) {
        builder.add(NarratedElementType.TITLE, this.getMessage());
    }

    public void setHeight(int height) {
        this.height = height;
    }

    protected boolean scrollbarVisible() {
        return this.getInnerHeight() > this.getHeight();
    }

    public boolean shouldRender() {
        return getScrollBarHeight() > 0 && this.height >= 48;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    protected double scrollAmount() {
        return this.scrollAmount;
    }

    protected void setScrollAmount(double scrollAmount) {
        this.scrollAmount = Mth.clamp(scrollAmount, 0.0, (double)this.getMaxScrollAmount());
    }

    protected int getMaxScrollAmount() {
        return Math.max(0, this.getContentHeight() - (this.height - 4));
    }

    private int getContentHeight() {
        return 4;
    }

    private void renderScrollBar() {
        int i = this.getScrollBarHeight();
        int j = this.x + this.width;
        int k = this.x + this.width + 8;
        int l = Math.max(this.y, (int)this.scrollAmount * (this.height - i) / this.getMaxScrollAmount() + this.y);
        int m = l + i;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(j, m, 0.0).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(k, m, 0.0).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(k, l, 0.0).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(j, l, 0.0).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(j, (m - 1), 0.0).color(192, 192, 192, 255).endVertex();
        bufferBuilder.vertex((k - 1), (m - 1), 0.0).color(192, 192, 192, 255).endVertex();
        bufferBuilder.vertex((k - 1), l, 0.0).color(192, 192, 192, 255).endVertex();
        bufferBuilder.vertex(j, l, 0.0).color(192, 192, 192, 255).endVertex();
        tesselator.end();
    }
    private int getScrollBarHeight() {
        return Mth.clamp((int)((float)(this.height * this.height) / (float)this.getContentHeight()), 32, this.height);
    }

    protected void renderDecorations(PoseStack matrices) {
        if (this.scrollbarVisible()) {
            this.renderScrollBar();
        }
    }

    protected boolean withinContentAreaTopBottom(int top, int bottom) {
        return (double)bottom - this.scrollAmount >= (double)this.y && (double)top - this.scrollAmount <= (double)(this.y + this.height);
    }

    protected boolean withinContentAreaPoint(double x, double y) {
        return x >= (double)this.x && x < (double)(this.x + this.width) && y >= (double)this.y && y < (double)(this.y + this.height);
    }

    public boolean mouseClickedScroll(double mouseX, double mouseY, int button) {
        if (!this.visible) {
            return false;
        } else {
            boolean bl = this.withinContentAreaPoint(mouseX, mouseY);
            boolean bl2 = this.scrollbarVisible() && mouseX >= (double)(this.x + this.width) && mouseX <= (double)(this.x + this.width + 8) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
            this.setFocused(bl || bl2);
            if (bl2 && button == 0) {
                this.scrolling = true;
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.scrolling = false;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.visible && this.isFocused() && this.scrolling) {
            if (mouseY < (double)this.y) {
                this.setScrollAmount(0.0);
            } else if (mouseY > (double)(this.y + this.height)) {
                this.setScrollAmount((double)this.getMaxScrollAmount());
            } else {
                int i = this.getScrollBarHeight();
                double d = (double)Math.max(1, this.getMaxScrollAmount() / (this.height - i));
                this.setScrollAmount(this.scrollAmount + deltaY * d);
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.visible && this.isFocused()) {
            this.setScrollAmount(this.scrollAmount - amount * this.scrollRate());
            return true;
        } else {
            return false;
        }
    }
}
