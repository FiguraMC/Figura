package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.moon.figura.utils.ui.UIHelper;

public class Label implements FiguraWidget, GuiEventListener {

    private Component text;

    public int x, y;
    public int width, height;
    private boolean visible = true;

    private final Font font;
    private final boolean centred;
    private final Integer outlineColor;

    public Label(Component text, int x, int y, boolean centred) {
        this(text, x, y, centred, null);
    }

    public Label(Component text, int x, int y, boolean centred, Integer outlineColor) {
        this.font = Minecraft.getInstance().font;
        this.text = text;
        this.x = x;
        this.y = y;
        this.centred = centred;
        this.outlineColor = outlineColor;
        calculateDimensions();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!isVisible())
            return;

        int x = this.x;
        int y = this.y;
        if (centred) {
            x -= font.width(text) / 2;
            y -= font.lineHeight / 2;
        }

        if (outlineColor != null)
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
