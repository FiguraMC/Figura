package org.moon.figura.gui.widgets.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.ConfigType;
import org.moon.figura.gui.widgets.ParentedButton;
import org.moon.figura.gui.widgets.SwitchButton;
import org.moon.figura.gui.widgets.lists.ConfigList;

public class BooleanElement extends AbstractConfigElement {

    private final ParentedButton button;

    public BooleanElement(int width, ConfigType.BoolConfig config, ConfigList parent) {
        super(width, config, parent);

        //button
        children.add(0, button = new ParentedButton(0, 0, 90, 20, config.tempValue ? SwitchButton.ON : SwitchButton.OFF, this, button -> config.tempValue = !(boolean) config.tempValue));
        button.setActive(FiguraMod.DEBUG_MODE || !config.disabled);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!this.isVisible()) return;

        //reset enabled
        this.resetButton.setActive(!this.isDefault());

        //button text
        Component text = (boolean) config.tempValue ? SwitchButton.ON : SwitchButton.OFF;

        //edited colour
        if (isChanged())
            text = text.copy().setStyle(FiguraMod.getAccentColor());

        //set text
        this.button.setMessage(text);

        //super render
        super.render(stack, mouseX, mouseY, delta);
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        this.button.setX(x + getWidth() - 154);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.button.setY(y);
    }
}
