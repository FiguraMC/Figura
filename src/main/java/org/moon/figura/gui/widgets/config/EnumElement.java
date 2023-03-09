package org.moon.figura.gui.widgets.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.ConfigType;
import org.moon.figura.gui.widgets.ContextMenu;
import org.moon.figura.gui.widgets.ParentedButton;
import org.moon.figura.gui.widgets.lists.ConfigList;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.util.List;

public class EnumElement extends AbstractConfigElement {

    private final List<Component> names;
    private final ParentedButton button;
    private final Component enumTooltip;
    private ContextMenu context;

    public EnumElement(int width, ConfigType.EnumConfig config, ConfigList parent) {
        super(width, config, parent);

        names = config.enumList;
        enumTooltip = config.enumTooltip;

        //toggle button
        children.add(0, button = new ParentedButton(0, 0, 90, 20, names.get((int) this.config.tempValue % this.names.size()), this, button -> {
            this.context.setVisible(!this.context.isVisible());

            if (context.isVisible()) {
                updateContextText();
                UIHelper.setContext(this.context);
            }
        }) {
            @Override
            protected void renderText(PoseStack stack) {
                Font font = Minecraft.getInstance().font;
                Component arrow = context.isVisible() ? UIHelper.DOWN_ARROW : UIHelper.UP_ARROW;
                int arrowWidth = font.width(arrow);

                Component message = getMessage();
                int textWidth = font.width(message);

                //draw text
                int color = (!this.active ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE).getColor();
                UIHelper.renderScrollingText(stack, message, x, y, getWidth() - (textWidth <= width - arrowWidth - 9 ? 0 : arrowWidth + 1), getHeight(), color);

                //draw arrow
                font.drawShadow(stack, arrow, x + getWidth() - arrowWidth - 3, y + getHeight() / 2 - font.lineHeight / 2, color);
            }
        });
        button.active = FiguraMod.DEBUG_MODE || !config.disabled;

        //context menu
        context = new ContextMenu(button, button.getWidth());
        for (int i = 0; i < names.size(); i++) {
            int finalI = i; //bruh
            context.addAction(names.get(i), button1 -> config.tempValue = finalI);
        }
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!this.isVisible()) return;

        //reset enabled
        this.resetButton.active = !this.isDefault();

        //button text
        Component text = names.get((int) this.config.tempValue % this.names.size());

        //edited colour
        if (this.isChanged())
            text = text.copy().setStyle(FiguraMod.getAccentColor());

        //set text
        this.button.setMessage(text);

        //super render
        super.render(stack, mouseX, mouseY, delta);
    }

    @Override
    public void setPos(int x, int y) {
        //update self pos
        super.setPos(x, y);

        //update button pos
        this.button.x = x + width - 154;
        this.button.y = y;

        //update context pos
        this.context.setPos(this.button.x + this.button.getWidth() / 2 - this.context.width / 2, this.button.y + 20);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (UIHelper.getContext() == this.context && this.context.isVisible()) {
            this.button.setHovered(true);
            return true;
        }

        return super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public MutableComponent getTooltip() {
        MutableComponent tooltip = super.getTooltip();
        if (enumTooltip != null) {
            tooltip.append("\n");
            for (Component component : TextUtils.splitText(enumTooltip, "\n"))
                tooltip.append("\n• ").append(component);
        }
        return tooltip;
    }

    private void updateContextText() {
        //cache entries
        List<? extends AbstractWidget> entries = context.getEntries();

        //entries should have the same size as names
        //otherwise something went really wrong
        for (int i = 0; i < names.size(); i++) {
            //get text
            Component text = names.get(i);

            //selected entry
            if (i == (int) this.config.tempValue % this.names.size())
                text = TextComponent.EMPTY.copy().setStyle(FiguraMod.getAccentColor()).append(text);

            //apply text
            entries.get(i).setMessage(text);
        }
    }
}
