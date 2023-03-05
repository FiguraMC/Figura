package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
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
    public int alpha = 0xFF;
    public int maxWidth;
    public boolean wrap;

    private Style hovered;

    //widget
    public int x, y;
    private int width, height;
    private float scale;
    private boolean visible = true;

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

    public Label(Object text, int x, int y, int maxWidth, boolean wrap) {
        this(text, x, y, 1f, maxWidth, wrap, TextUtils.Alignment.LEFT, null);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        hovered = null;

        if (!isVisible())
            return;

        stack.pushPose();
        stack.translate(this.x, this.y, 0);
        stack.scale(scale, scale, scale);

        //prepare pos
        int y = 0;
        int height = font.lineHeight;

        if (alignment == TextUtils.Alignment.CENTER)
            y -= height * formattedText.size() / 2f;

        for (Component text : formattedText) {
            //dimensions
            int x = -alignment.apply(font, text);
            int width = font.width(text);

            //hovered
            if (mouseX >= this.x + x * scale && mouseX < this.x + (x + width) * scale && mouseY >= this.y + y * scale && mouseY < this.y + (y + height) * scale) {
                hovered = font.getSplitter().componentStyleAtWidth(text, (int) ((mouseX - this.x - x * scale) / scale));
                if (hovered != null && hovered.getClickEvent() != null)
                    text = text.copy().withStyle(ChatFormatting.UNDERLINE);
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
        int x = this.x;
        int y = this.y;

        if (alignment == TextUtils.Alignment.RIGHT) {
            x -= width;
        } else if (alignment == TextUtils.Alignment.CENTER) {
            x -= width / 2;
            y -= height / 2;
        }

        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height)
            return true;
        return GuiEventListener.super.isMouseOver(mouseX, mouseY);
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

    public int getWidth() {
        return width;
    }

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
}
