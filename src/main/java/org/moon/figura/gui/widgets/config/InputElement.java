package org.moon.figura.gui.widgets.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.Config;
import org.moon.figura.gui.widgets.ParentedButton;
import org.moon.figura.gui.widgets.TextField;
import org.moon.figura.gui.widgets.lists.ConfigList;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.util.function.Consumer;

public class InputElement extends AbstractConfigElement {

    private final TextField textField;
    private final boolean isHex;

    public InputElement(int width, Config config, ConfigList parent) {
        super(width, config, parent);

        //get input type
        Config.InputType inputType = config.inputType;
        isHex = inputType == Config.InputType.HEX_COLOR;

        //text field
        textField = new InputField(0, 0, 90, 20, inputType.hint, this, text -> {
            //only write config value if it's valid
            if (inputType.validator.test(text))
                config.tempValue = isHex ? ColorUtils.userInputHex(text, FiguraVec3.of()) : text;
        });
        updateTextFieldText(formatText(config.tempValue));
        textField.getField().moveCursorToStart();
        textField.setEnabled(FiguraMod.DEBUG_MODE || !config.disabled);

        children.add(0, textField);

        //overwrite reset button to update the text field
        children.remove(resetButton);
        children.add(resetButton = new ParentedButton(x + width - 60, y, 60, 20, Component.translatable("controls.reset"), this, button -> {
            config.tempValue = config.defaultValue;
            updateTextFieldText(formatText(config.tempValue));
        }));
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!this.isVisible()) return;

        //reset enabled
        this.resetButton.active = isDefault();

        //text colour
        int color = 0xFFFFFF;

        //invalid config
        String text = textField.getField().getValue();
        if (!config.inputType.validator.test(text)) {
            color = 0xFF5555;
        }
        //config was changed
        else if (!text.equals(formatText(initValue))) {
            TextColor textColor = FiguraMod.getAccentColor().getColor();
            color = textColor == null ? ColorUtils.Colors.FRAN_PINK.hex : textColor.getValue();
        }

        //set text colour
        textField.setColor(color);
        textField.setBorderColour(0xFF000000 + color);

        //super render
        super.render(stack, mouseX, mouseY, delta);

        //hex colour preview
        if (isHex) {
            //border
            UIHelper.fillRounded(stack, x + width - 178, y, 20, 20, getTextField().isFocused() ? getTextField().getBorderColour() : 0xFF404040);
            //inside
            UIHelper.fillRounded(stack, x + width - 177, y + 1, 18, 18, 0xFF000000 + (int) config.tempValue);
        }
    }

    @Override
    public void setPos(int x, int y) {
        this.textField.setPos(x + width - 154, y);
        super.setPos(x, y);
    }

    @Override
    public boolean isDefault() {
        return !textField.getField().getValue().equals(formatText(config.defaultValue));
    }

    @Override
    public boolean isChanged() {
        return !textField.getField().getValue().equals(formatText(initValue));
    }

    public TextField getTextField() {
        return textField;
    }

    public void updateTextFieldText(String text) {
        textField.getField().setValue(text);
    }

    private String formatText(Object configValue) {
        return config.inputType == Config.InputType.HEX_COLOR ? String.format("#%06X", (int) configValue) : configValue.toString();
    }

    private static class InputField extends TextField {

        private final InputElement parent;

        public InputField(int x, int y, int width, int height, Component hint, InputElement parent, Consumer<String> changedListener) {
            super(x, y, width, height, hint, changedListener);
            this.parent = parent;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.parent.isHovered() && super.isMouseOver(mouseX, mouseY);
        }
    }
}
