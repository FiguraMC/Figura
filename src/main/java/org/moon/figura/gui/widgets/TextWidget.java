package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

public class TextWidget implements FiguraWidget, GuiEventListener {

    private Component text;

    public int x, y;
    public int width, height;
    private boolean visible = true;

    private final Font font;

    public TextWidget(Component text, int x, int y) {
        this.font = Minecraft.getInstance().font;
        this.text = text;
        this.x = x;
        this.y = y;
        calculateDimensions();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
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
