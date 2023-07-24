package org.figuramc.figura.gui.widgets.config;

import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.gui.widgets.ParentedButton;
import org.figuramc.figura.gui.widgets.lists.ConfigList;

public class ButtonElement extends AbstractConfigElement {

    private final ParentedButton button;

    public ButtonElement(int width, ConfigType.ButtonConfig config, ConfigList parentList, CategoryWidget parentCategory) {
        super(width, config, parentList, parentCategory);
        resetButton.setActive(false);
        children.add(0, button = new ParentedButton(0, 0, 90, 20, config.name, this, button -> config.toRun.run()));
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
