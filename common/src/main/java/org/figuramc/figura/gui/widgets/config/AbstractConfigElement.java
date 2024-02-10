package org.figuramc.figura.gui.widgets.config;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.gui.widgets.AbstractContainerElement;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.ParentedButton;
import org.figuramc.figura.gui.widgets.lists.ConfigList;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.Locale;
import java.util.Objects;

public abstract class AbstractConfigElement extends AbstractContainerElement {

    protected final ConfigType<?> config;
    protected final ConfigList parentList;
    protected final CategoryWidget parentCategory;

    protected Button resetButton;

    protected Object initValue;

    private String filter = "";

    public AbstractConfigElement(int width, ConfigType<?> config, ConfigList parentList, CategoryWidget parentCategory) {
        super(0, 0, width, 20);
        this.config = config;
        this.parentList = parentList;
        this.parentCategory = parentCategory;
        this.initValue = config.value;

        // reset button
        children.add(resetButton = new ParentedButton(0, 0, 60, 20, Component.translatable("controls.reset"), this, button -> config.resetTemp()));
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        if (!this.isVisible()) return;

        // vars
        Font font = Minecraft.getInstance().font;
        int textY = getY() + getHeight() / 2 - font.lineHeight / 2;

        // hovered arrow
        setHovered(isMouseOver(mouseX, mouseY));
        if (isHovered()) gui.drawString(font, HOVERED_ARROW, (int) (getX() + 8 - font.width(HOVERED_ARROW) / 2f), textY, 0xFFFFFF);

        // render name
        renderTitle(gui, font, textY);

        // render children
        super.render(gui, mouseX, mouseY, delta);
    }

    public void renderTitle(GuiGraphics gui, Font font, int y) {
        gui.drawString(font, config.name, getX() + 16, y, (config.disabled ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE).getColor());
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        boolean over = this.parentList.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);

        if (over && mouseX < this.getX() + this.getWidth() - 158)
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

    @Override
    public void setX(int x) {
        super.setX(x);
        resetButton.setX(x + getWidth() - 60);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        resetButton.setY(y);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible && parentCategory.isShowingChildren() && matchesFilter());
    }

    public void updateFilter(String query) {
        this.filter = query;
    }

    public boolean matchesFilter() {
        return config.name.getString().toLowerCase(Locale.US).contains(filter) || config.tooltip.getString().toLowerCase(Locale.US).contains(filter);
    }
}
