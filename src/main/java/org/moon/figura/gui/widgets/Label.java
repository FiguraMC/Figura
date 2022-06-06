package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.util.List;

public class Label implements FiguraWidget, GuiEventListener {

    private Component text;

    public int x, y;
    public int width = 0;
    public int height = 0;
    private boolean visible = true;

    private final Font font;
    private final boolean centred;
    private final Integer outlineColor;

    public Label(Object text, int x, int y, boolean centred) {
        this(text, x, y, centred, null);
    }

    public Label(Object text, int x, int y, boolean centred, Integer outlineColor) {
        this.font = Minecraft.getInstance().font;
        this.text = text instanceof Component c ? c : new TextComponent(text.toString());
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

        //split new lines
        List<Component> split = TextUtils.splitText(text, "\n");
        for (int i = 0; i < split.size(); i++) {
            Component line = split.get(i);

            int x = this.x;
            int y = this.y + font.lineHeight * i;
            if (centred) {
                x -= font.width(line) / 2;
                y -= font.lineHeight / 2;
            }

            if (outlineColor != null)
                UIHelper.renderOutlineText(stack, font, line, x, y, 0xFFFFFF, outlineColor);
            else
                font.drawShadow(stack, line, x, y, 0xFFFFFF);
        }
    }

    private void calculateDimensions() {
        List<Component> split = TextUtils.splitText(text, "\n");
        for (Component line : split)
            this.width = Math.max(this.width, font.width(line));
        this.height = font.lineHeight * split.size();
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

    public void setText(Object text) {
        this.text = text instanceof Component c ? c : new TextComponent(text.toString());
        calculateDimensions();
    }
}
