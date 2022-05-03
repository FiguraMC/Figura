package org.moon.figura.gui.widgets.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.moon.figura.config.Config;
import org.moon.figura.gui.widgets.lists.ConfigList;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;

public class BooleanElement extends AbstractConfigElement {

    private static final Component ON = new FiguraText("gui.on");
    private static final Component OFF = new FiguraText("gui.off");

    private final ParentedButton button;

    public BooleanElement(int width, Config config, ConfigList parent) {
        super(width, config, parent);

        //button
        button = new ParentedButton(0, 0, 90, 20, (boolean) config.configValue ? ON : OFF, this, button -> config.configValue = !(boolean) config.configValue);
        children.add(0, button);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!this.isVisible()) return;

        //reset enabled
        this.resetButton.active = this.config.configValue != this.config.defaultValue;

        //button text
        Component text = (boolean) config.configValue ? ON : OFF;

        //edited colour
        if (this.config.configValue != this.initValue)
            text = text.copy().setStyle(ColorUtils.Colors.FRAN_PINK.style);

        //set text
        this.button.setMessage(text);

        //super render
        super.render(stack, mouseX, mouseY, delta);
    }

    @Override
    public void setPos(int x, int y) {
        super.setPos(x, y);

        this.button.x = x + width - 154;
        this.button.y = y;
    }
}
