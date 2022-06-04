package org.moon.figura.gui.widgets.lists;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.moon.figura.config.Config;
import org.moon.figura.gui.widgets.config.ConfigWidget;
import org.moon.figura.gui.widgets.config.InputElement;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class ConfigList extends AbstractList {

    private static final List<ConfigWidget> CONFIGS = new ArrayList<>();
    public KeyMapping focusedBinding;

    private int totalHeight = 0;

    public ConfigList(int x, int y, int width, int height) {
        super(x, y, width, height);
        updateList();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //background and scissors
        UIHelper.renderSliced(stack, x, y, width, height, UIHelper.OUTLINE);
        UIHelper.setupScissor(x + scissorsX, y + scissorsY, width + scissorsWidth, height + scissorsHeight);

        //scrollbar
        totalHeight = -4;
        for (ConfigWidget config : CONFIGS)
            totalHeight += config.getHeight() + 8;
        int entryHeight = CONFIGS.isEmpty() ? 0 : totalHeight / CONFIGS.size();

        scrollBar.visible = totalHeight > height;
        scrollBar.setScrollRatio(entryHeight, totalHeight - height);

        //render list
        int xOffset = scrollBar.visible ? 4 : 11;
        int yOffset = scrollBar.visible ? (int) -(Mth.lerp(scrollBar.getScrollProgress(), -4, totalHeight - height)) : 4;
        for (ConfigWidget config : CONFIGS) {
            config.setPos(x + xOffset, y + yOffset);
            yOffset += config.getHeight() + 8;
        }

        //children
        super.render(stack, mouseX, mouseY, delta);

        //reset scissor
        RenderSystem.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //fix mojang focusing for text fields
        for (ConfigWidget configWidget : CONFIGS) {
            for (GuiEventListener children : configWidget.children()) {
                if (children instanceof InputElement inputElement)
                    inputElement.getTextField().getField().setFocus(inputElement.getTextField().isMouseOver(mouseX, mouseY));
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void updateList() {
        //clear old widgets
        CONFIGS.forEach(children::remove);

        //temp list
        List<ConfigWidget> temp = new ArrayList<>();

        //add configs
        ConfigWidget lastCategory = null;
        for (Config config : Config.values()) {
            //add new config entry into the category
            if (config.type != Config.ConfigType.CATEGORY) {
                //create dummy category if empty
                if (lastCategory == null) {
                    ConfigWidget widget = new ConfigWidget(width - 22, Component.empty(), null, this);
                    lastCategory = widget;

                    temp.add(widget);
                    children.add(widget);
                }

                //add entry
                lastCategory.addConfig(config);
            //add new config category
            } else {
                ConfigWidget widget = new ConfigWidget(width - 22, config.name, config.tooltip, this);
                lastCategory = widget;

                temp.add(widget);
                children.add(widget);
            }
        }

        //fix expanded status
        if (!CONFIGS.isEmpty()) {
            for (int i = 0; i < CONFIGS.size(); i++)
                temp.get(i).setShowChildren(CONFIGS.get(i).isShowingChildren());
        }

        //add configs
        CONFIGS.clear();
        CONFIGS.addAll(temp);
    }

    public void updateScroll() {
        //store old scroll pos
        double pastScroll = (totalHeight - height) * scrollBar.getScrollProgress();

        //get new height
        totalHeight = -4;
        for (ConfigWidget config : CONFIGS)
            totalHeight += config.getHeight() + 8;

        //set new scroll percentage
        scrollBar.setScrollProgress(pastScroll / (totalHeight - height));
    }
}
