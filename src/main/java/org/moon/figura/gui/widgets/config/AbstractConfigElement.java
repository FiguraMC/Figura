package org.moon.figura.gui.widgets.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.moon.figura.config.ConfigType;
import org.moon.figura.gui.widgets.AbstractContainerElement;
import org.moon.figura.gui.widgets.ParentedButton;
import org.moon.figura.gui.widgets.TexturedButton;
import org.moon.figura.gui.widgets.lists.ConfigList;
import org.moon.figura.utils.ui.UIHelper;

import java.util.Objects;

public abstract class AbstractConfigElement extends AbstractContainerElement {

    protected final ConfigType<?> config;
    protected final ConfigList parent;

    protected TexturedButton resetButton;

    protected Object initValue;

    public AbstractConfigElement(int width, ConfigType<?> config, ConfigList parent) {
        super(0, 0, width, 20);
        this.config = config;
        this.parent = parent;
        this.initValue = config.value;

        //reset button
        children.add(resetButton = new ParentedButton(x + width - 60, y, 60, 20, Component.translatable("controls.reset"), this, button -> config.resetTemp()));
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!this.isVisible()) return;

        //vars
        Font font = Minecraft.getInstance().font;
        int textY = y + height / 2 - font.lineHeight / 2;

        //hovered arrow
        setHovered(isMouseOver(mouseX, mouseY));
        if (isHovered()) font.draw(stack, HOVERED_ARROW, x + 8 - font.width(HOVERED_ARROW) / 2, textY, 0xFFFFFF);

        //render name
        renderTitle(stack, font, textY);

        //render children
        super.render(stack, mouseX, mouseY, delta);
    }

    public void renderTitle(PoseStack stack, Font font, int y) {
        font.draw(stack, config.name, x + 16, y, (config.disabled ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE).getColor());
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        boolean over = this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);

        if (over && mouseX < this.x + this.width - 158)
            UIHelper.setTooltip(getTooltip());

        return over;
    }

    public MutableComponent getTooltip() {
        return config.tooltip.copy();
    }

    public boolean isDefault() {
        return this.config.isDefault();
    }

    public boolean isChanged() {
        return !Objects.equals(this.config.tempValue, this.initValue);
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;

        resetButton.setX(x + width - 60);
        resetButton.setY(y);
    }
}
