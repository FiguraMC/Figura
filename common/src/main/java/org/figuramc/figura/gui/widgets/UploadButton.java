package org.figuramc.figura.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * Button that also works when it's disabled, but does something different.
 */
public class UploadButton extends Button {
    private final Function<UploadButton, Boolean> disabledAction;

    public UploadButton(
            int x, int y, int width, int height, Integer u, Integer v,
            Integer regionSize, ResourceLocation texture, Integer textureWidth, Integer textureHeight,
            Component text, Component tooltip, OnPress pressAction, Function<UploadButton, Boolean> pressDisabledAction
    ) {
        super(x, y, width, height, u, v, regionSize, texture, textureWidth, textureHeight, text, tooltip, pressAction);
        this.disabledAction = pressDisabledAction;
    }

    public UploadButton(
            int x, int y, int width, int height,
            Component text, Component tooltip, OnPress pressAction, Function<UploadButton, Boolean> pressDisabledAction
    ) {
        super(x, y, width, height, text, tooltip, pressAction);
        this.disabledAction = pressDisabledAction;
    }

    public UploadButton(
            int x, int y, int width, int height,
            int u, int v,
            int regionSize, ResourceLocation texture, int textureWidth, int textureHeight,
            Component tooltip, OnPress pressAction, Function<UploadButton, Boolean> pressDisabledAction
    ) {
        super(x, y, width, height, u, v, regionSize, texture, textureWidth, textureHeight, tooltip, pressAction);
        this.disabledAction = pressDisabledAction;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button))
            return true;
        boolean target = this.isHoveredOrFocused() && this.isMouseOver(mouseX, mouseY) && this.visible;
        if (!target) return false;
        if (!this.isValidClickButton(button)) return false;
        boolean bl = this.clickedDisabled(mouseX, mouseY);
        if (bl) {
            if (this.disabledAction.apply(this)) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                return true;
            }
        }
        return false;
    }

    protected boolean clickedDisabled(double mouseX, double mouseY) {
        return !this.active
                && this.visible
                && mouseX >= this.getX()
                && mouseY >= this.getY()
                && mouseX < this.getX() + this.width
                && mouseY < this.getY() + this.height;
    }
}
