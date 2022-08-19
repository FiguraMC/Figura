package org.moon.figura.gui.widgets.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.moon.figura.config.Config;
import org.moon.figura.gui.widgets.AbstractContainerElement;
import org.moon.figura.gui.widgets.TexturedButton;
import org.moon.figura.gui.widgets.lists.ConfigList;
import org.moon.figura.utils.ui.UIHelper;

public abstract class AbstractConfigElement extends AbstractContainerElement {

    public static final Component HOVERED_ARROW = new TextComponent("•");

    protected final Config config;
    protected final ConfigList parent;

    protected TexturedButton resetButton;

    protected Object initValue;

    private boolean hovered = false;

    public AbstractConfigElement(int width, Config config, ConfigList parent) {
        super(0, 0, width, 20);
        this.config = config;
        this.parent = parent;
        this.initValue = config.value;

        //reset button
        children.add(resetButton = new ParentedButton(x + width - 60, y, 60, 20, new TranslatableComponent("controls.reset"), this, button -> config.tempValue = config.defaultValue));
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!this.isVisible()) return;

        //vars
        Font font = Minecraft.getInstance().font;
        int textY = y + height / 2 - font.lineHeight / 2;

        //hovered arrow
        hovered = isMouseOver(mouseX, mouseY);
        if (hovered) font.draw(stack, HOVERED_ARROW, x + 8 - font.width(HOVERED_ARROW) / 2, textY, 0xFFFFFF);

        //render name
        font.draw(stack, config.name, x + 16, textY, 0xFFFFFF);

        //render children
        super.render(stack, mouseX, mouseY, delta);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        boolean over = this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);

        if (over && mouseX < this.x + this.width - 158)
            UIHelper.setTooltip(config.tooltip);

        return over;
    }

    public boolean isDefault() {
        return this.config.tempValue != this.config.defaultValue;
    }

    public boolean isChanged() {
        return this.config.tempValue != this.initValue;
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;

        resetButton.x = x + width - 60;
        resetButton.y = y;
    }

    public boolean isHovered() {
        return hovered;
    }

    protected static class ParentedButton extends TexturedButton {

        private final AbstractConfigElement parent;

        public ParentedButton(int x, int y, int width, int height, Component text, AbstractConfigElement parent, OnPress pressAction) {
            super(x, y, width, height, text, null, pressAction);
            this.parent = parent;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.parent.isHovered() && super.isMouseOver(mouseX, mouseY);
        }
    }
}
