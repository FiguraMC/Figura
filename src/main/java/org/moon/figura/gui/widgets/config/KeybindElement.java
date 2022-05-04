package org.moon.figura.gui.widgets.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.moon.figura.config.Config;
import org.moon.figura.gui.widgets.lists.ConfigList;
import org.moon.figura.utils.ColorUtils;

public class KeybindElement extends AbstractConfigElement {

    private final KeyMapping binding;
    private final ParentedButton button;

    public KeybindElement(int width, Config config, ConfigList parent) {
        super(width, config, parent);
        this.binding = config.keyBind;

        //toggle button
        children.add(0, button = new ParentedButton(0, 0, 90, 20, this.binding.getTranslatedKeyMessage(), this, button -> parent.focusedBinding = binding));

        //overwrite reset button to update the keybind
        children.remove(resetButton);
        children.add(resetButton = new ParentedButton(x + width - 60, y, 60, 20, new TranslatableComponent("controls.reset"), this, button -> binding.setKey(binding.getDefaultKey())));
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!this.isVisible()) return;

        //reset enabled
        this.resetButton.active = !this.binding.isDefault();

        //button message
        button.setMessage(binding.getTranslatedKeyMessage());

        //editing message
        if (parent.focusedBinding == this.binding) {
            button.setMessage(new TextComponent("> ").setStyle(ColorUtils.Colors.FRAN_PINK.style).append(button.getMessage()).append(" <"));
        }
        //conflict check
        else if (!this.binding.isUnbound()) {
            for (KeyMapping key : Minecraft.getInstance().options.keyMappings) {
                if (key != this.binding && this.binding.equals(key)) {
                    button.setMessage(button.getMessage().copy().withStyle(ChatFormatting.RED));
                    break;
                }
            }
        }

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
