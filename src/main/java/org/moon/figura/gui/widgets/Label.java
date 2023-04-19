package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.util.List;

public class Label implements FiguraWidget, GuiEventListener, NarratableEntry {

    //text
    private final Font font;
    private Component rawText;
    private List<Component> formattedText;
    public TextUtils.Alignment alignment;
    public Integer outlineColor;
    public Integer backgroundColor;
    public int alpha = 0xFF;
    public int maxWidth;
    public boolean wrap;

    private Style hovered;

    //widget
    private int x, y;
    private int width, height;
    private float scale;
    private boolean visible = true;
    public boolean centerVertically;

    public Label(Object text, int x, int y, float scale, int maxWidth, boolean wrap, TextUtils.Alignment alignment, Integer outlineColor) {
        this.font = Minecraft.getInstance().font;
        this.rawText = text instanceof Component c ? c : Component.literal(String.valueOf(text));
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.maxWidth = maxWidth;
        this.wrap = wrap;
        this.alignment = alignment;
        this.outlineColor = outlineColor;
        updateText();
    }

    public Label(Object text, int x, int y, int outlineColor) {
        this(text, x, y, 1f, -1, false, TextUtils.Alignment.LEFT, outlineColor);
    }

    public Label(Object text, int x, int y, TextUtils.Alignment alignment) {
        this(text, x, y, 1f, -1, false, alignment, null);
    }

    public Label(Object text, int x, int y, TextUtils.Alignment alignment, int outlineColor) {
        this(text, x, y, 1f, -1, false, alignment, outlineColor);
    }

    public Label(Object text, int x, int y, int maxWidth, boolean wrap, TextUtils.Alignment alignment) {
        this(text, x, y, 1f, maxWidth, wrap, alignment, null);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        hovered = null;

        if (!isVisible())
            return;

        renderBackground(stack);
        renderText(stack, mouseX, mouseY);
    }

    private void renderBackground(PoseStack stack) {
        if (backgroundColor == null)
            return;

        int x = getX();
        int y = getY();

        UIHelper.fill(stack, x, y, x + width, y + height, backgroundColor);
    }

    private void renderText(PoseStack stack, int mouseX, int mouseY) {
        stack.pushPose();
        stack.translate(this.x, getY(), 0);
        stack.scale(scale, scale, scale);

        //prepare pos
        int y = 0;
        int height = font.lineHeight;

        for (Component text : formattedText) {
            //dimensions
            int x = -alignment.apply(font, text);
            int width = font.width(text);

            //hovered
            if (mouseX >= this.x + x * scale && mouseX < this.x + (x + width) * scale && mouseY >= this.y + y * scale && mouseY < this.y + (y + height) * scale) {
                //get style at the mouse pos
                int pos = (int) ((mouseX - this.x - x * scale) / scale);
                hovered = font.getSplitter().componentStyleAtWidth(text, pos);

                //add underline for the text with the click event
                if (hovered != null && hovered.getClickEvent() != null)
                    text = TextUtils.setStyleAtWidth(text, pos, font, Style.EMPTY.withUnderlined(true));

                //set tooltip for hovered text, if any
                UIHelper.setTooltip(hovered);
            }

            //render text
            if (outlineColor != null) {
                UIHelper.renderOutlineText(stack, font, text, x, y, 0xFFFFFF, outlineColor);
            } else {
                font.drawShadow(stack, text, x, y, 0xFFFFFF + (alpha << 24));
            }

            y += height;
        }

        stack.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered != null && Minecraft.getInstance().screen != null) {
            Minecraft.getInstance().screen.handleComponentClicked(hovered);
            return true;
        }

        return GuiEventListener.super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!isVisible())
            return false;

        int x = getX();
        int y = getY();

        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height)
            return true;
        return GuiEventListener.super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public void setFocused(boolean focused) {}

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateNarration(NarrationElementOutput builder) {
        builder.add(NarratedElementType.POSITION, rawText);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setScale(float scale) {
        this.scale = scale;
        updateText();
    }

    public void setText(Component text) {
        this.rawText = text;
        updateText();
    }

    private void updateText() {
        this.formattedText = TextUtils.formatInBounds(rawText, font, (int) (maxWidth / scale), wrap);
        this.width = (int) (TextUtils.getWidth(formattedText, font) * scale);
        this.height = (int) (font.lineHeight * formattedText.size() * scale);
    }

    @Override
    public int getX() {
        int x = this.x;

        if (alignment == TextUtils.Alignment.RIGHT)
            x -= width;
        else if (alignment == TextUtils.Alignment.CENTER)
            x -= width / 2;

        return x;
    }

    public int getRawX() {
        return x;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getY() {
        int y = this.y;

        if (centerVertically)
            y -= height / 2;

        return y;
    }

    public int getRawY() {
        return y;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public void setWidth(int width) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeight(int height) {
        throw new UnsupportedOperationException();
    }
}
