package org.figuramc.figura.gui.widgets.config;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.ConfigKeyBind;
import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.widgets.KeybindWidgetHelper;
import org.figuramc.figura.gui.widgets.ParentedButton;
import org.figuramc.figura.gui.widgets.lists.ConfigList;

public class KeybindElement extends AbstractConfigElement {

    private final KeybindWidgetHelper helper = new KeybindWidgetHelper();
    private final KeyMapping binding;
    private final ParentedButton button;

    public KeybindElement(int width, ConfigType.KeybindConfig config, ConfigList parentList, CategoryWidget parentCategory) {
        super(width, config, parentList, parentCategory);
        this.binding = config.keyBind;

        // toggle button
        children.add(0, button = new ParentedButton(0, 0, 90, 20, this.binding.getTranslatedKeyMessage(), this, button -> {
            parentList.focusedBinding = binding;
            FiguraMod.processingKeybind = true;
            updateText();
        }));
        button.setActive(FiguraMod.debugModeEnabled() || !config.disabled);

        // overwrite reset button to update the keybind
        children.remove(resetButton);
        children.add(resetButton = new ParentedButton(getX() + width - 60, getY(), 60, 20, Component.translatable("controls.reset"), this, button -> {
            binding.setKey(binding.getDefaultKey());
            ((ConfigKeyBind)binding).saveConfigChanges();
            parentList.updateKeybinds();
        }));

        updateText();
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        if (!this.isVisible()) return;

        // reset enabled
        helper.renderConflictBars(gui, button.getX() - 8, button.getY() + 2, 4, 16);

        // super render
        super.render(gui, mouseX, mouseY, delta);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        boolean bool = super.isMouseOver(mouseX, mouseY);
        if (bool && button.isMouseOver(mouseX, mouseY))
            helper.renderTooltip();
        return bool;
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

    @Override
    public boolean isDefault() {
        return this.binding.isDefault();
    }

    @Override
    public boolean isChanged() {
        return false;
    }

    public void updateText() {
        // tooltip
        helper.setTooltip(binding);

        // reset button
        boolean isDefault = isDefault();
        this.resetButton.setActive(!isDefault);

        // text
        boolean selected = parentList.focusedBinding == binding;
        Component text = helper.getText(isDefault, selected, binding.getTranslatedKeyMessage());
        button.setMessage(text);
    }
}
