package org.figuramc.figura.gui.widgets.config;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.widgets.ContextMenu;
import org.figuramc.figura.gui.widgets.ParentedButton;
import org.figuramc.figura.gui.widgets.lists.ConfigList;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.List;

public class EnumElement extends AbstractConfigElement {

    private final List<Component> names;
    private final ParentedButton button;
    private ContextMenu context;

    public EnumElement(int width, ConfigType.EnumConfig config, ConfigList parentList, CategoryWidget parentCategory) {
        super(width, config, parentList, parentCategory);

        names = config.enumList;

        // toggle button
        int selectedIndex = (int) this.config.tempValue % this.names.size();
        children.add(0, button = new ParentedButton(0, 0, 90, 20, names.get(selectedIndex), this, button -> {
            this.context.setVisible(!this.context.isVisible());

            if (context.isVisible()) {
                updateContextText();
                UIHelper.setContext(this.context);
            }
        }) {
            @Override
            protected void renderText(GuiGraphics gui, float delta) {
                Font font = Minecraft.getInstance().font;
                Component arrow = context.isVisible() ? UIHelper.DOWN_ARROW : UIHelper.UP_ARROW;
                int arrowWidth = font.width(arrow);

                Component message = getMessage();
                int textWidth = font.width(message);

                // draw text
                int color = getTextColor();
                UIHelper.renderCenteredScrollingText(gui, message, getX() + 1, getY(), getWidth() - (textWidth <= getWidth() - arrowWidth - 9 ? 0 : arrowWidth + 1) - 2, getHeight(), color);

                // draw arrow
                gui.drawString(font, arrow, getX() + getWidth() - arrowWidth - 3, (int) (getY() + getHeight() / 2f - font.lineHeight / 2f), color);
            }

            @Override
            public void setHovered(boolean hovered) {
                if (!hovered && UIHelper.getContext() == context && context.isVisible())
                    hovered = true;

                super.setHovered(hovered);
            }
        });
        button.setActive(FiguraMod.debugModeEnabled() || !config.disabled);
        if (config.enumTooltip != null)
            button.setTooltip(config.enumTooltip.get(selectedIndex));

        // context menu
        context = new ContextMenu(button, button.getWidth());
        for (int i = 0; i < names.size(); i++) {
            int finalI = i;
            Component tooltip = config.enumTooltip != null ? config.enumTooltip.get(i) : null;
            context.addAction(names.get(i), tooltip, button1 -> config.tempValue = finalI);
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        if (!this.isVisible()) return;

        // reset enabled
        this.resetButton.setActive(!this.isDefault());

        // button text
        int selectedIndex = (int) this.config.tempValue % this.names.size();
        Component text = names.get(selectedIndex);

        // edited colour
        if (this.isChanged())
            text = text.copy().setStyle(FiguraMod.getAccentColor());

        // set text
        this.button.setMessage(text);

        // set tooltip
        List<Component> tooltip = ((ConfigType.EnumConfig) this.config).enumTooltip;
        if (tooltip != null)
            button.setTooltip(tooltip.get(selectedIndex));

        // super render
        super.render(gui, mouseX, mouseY, delta);
    }

    @Override
    public void setX(int x) {
        // update self pos
        super.setX(x);
        // update button pos
        this.button.setX(x + getWidth() - 154);
        // update context pos
        this.context.setX(this.button.getX() + this.button.getWidth() / 2 - this.context.getWidth() / 2);
    }

    @Override
    public void setY(int y) {
        // update self pos
        super.setY(y);

        // update button pos
        this.button.setY(y);

        // update context pos
        this.context.setY(this.button.getY() + 20);
    }

    private void updateContextText() {
        // cache entries
        List<? extends AbstractWidget> entries = context.getEntries();

        // entries should have the same size as names
        // otherwise something went really wrong
        for (int i = 0; i < names.size(); i++) {
            // get text
            Component text = names.get(i);

            // selected entry
            if (i == (int) this.config.tempValue % this.names.size())
                text = Component.empty().setStyle(FiguraMod.getAccentColor()).withStyle(ChatFormatting.UNDERLINE).append(text);

            // apply text
            entries.get(i).setMessage(text);
        }
    }
}
