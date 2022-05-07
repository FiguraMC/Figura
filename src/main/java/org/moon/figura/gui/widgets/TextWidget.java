package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.moon.figura.utils.ui.UIHelper;

public class TextWidget implements FiguraWidget, GuiEventListener {

    private Component text;

    public int x, y;
    public int width, height;
    private boolean visible = true;

    private final Font font;
    private final boolean outline;
    private final int outlineColor;

    public TextWidget(Component text, int x, int y) {
        this(text, x, y, false, 0);
    }

    public TextWidget(Component text, int x, int y, boolean outline, int outlineColor) {
        this.font = Minecraft.getInstance().font;
        this.text = text;
        this.x = x;
        this.y = y;
        this.outline = outline;
        this.outlineColor = outlineColor;
        calculateDimensions();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!isVisible())
            return;

        if (outline)
            UIHelper.renderOutlineText(stack, font, text, x, y, 0xFFFFFF, outlineColor);
        else
            font.drawShadow(stack, text, x, y, 0xFFFFFF);
    }

    private void calculateDimensions() {
        this.width = font.width(text);
        this.height = font.lineHeight;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Component getText() {
        return text;
    }

    public void setText(Component text) {
        this.text = text;
        calculateDimensions();
    }
}
