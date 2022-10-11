package org.moon.figura.gui.widgets.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.Config;
import org.moon.figura.gui.widgets.ParentedButton;
import org.moon.figura.gui.widgets.lists.ConfigList;
import org.moon.figura.utils.FiguraText;

public class BooleanElement extends AbstractConfigElement {

    private static final Component ON = FiguraText.of("gui.on");
    private static final Component OFF = FiguraText.of("gui.off");

    private final ParentedButton button;

    public BooleanElement(int width, Config config, ConfigList parent) {
        super(width, config, parent);

        //button
        children.add(0, button = new ParentedButton(0, 0, 90, 20, (boolean) config.tempValue ? ON : OFF, this, button -> config.tempValue = !(boolean) config.tempValue));
        button.active = FiguraMod.DEBUG_MODE || !config.disabled;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!this.isVisible()) return;

        //reset enabled
        this.resetButton.active = this.isDefault();

        //button text
        Component text = (boolean) config.tempValue ? ON : OFF;

        //edited colour
        if (isChanged())
            text = text.copy().setStyle(FiguraMod.getAccentColor());

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
