package org.moon.figura.gui.widgets.config;

import org.moon.figura.config.ConfigType;
import org.moon.figura.gui.widgets.ParentedButton;
import org.moon.figura.gui.widgets.lists.ConfigList;

public class ButtonElement extends AbstractConfigElement {

    private final ParentedButton button;

    public ButtonElement(int width, ConfigType.ButtonConfig config, ConfigList parent) {
        super(width, config, parent);
        resetButton.active = false;
        children.add(0, button = new ParentedButton(0, 0, 90, 20, config.name, this, button -> config.toRun.run()));
    }

    @Override
    public void setPos(int x, int y) {
        super.setPos(x, y);

        this.button.x = x + width - 154;
        this.button.y = y;
    }
}
