package org.moon.figura.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moon.figura.config.ConfigManager;
import org.moon.figura.gui.widgets.Label;
import org.moon.figura.gui.widgets.TexturedButton;
import org.moon.figura.gui.widgets.lists.ConfigList;
import org.moon.figura.utils.FiguraText;

public class ConfigScreen extends AbstractPanelScreen {

    public static final Component TITLE = FiguraText.of("gui.panels.title.settings");

    private ConfigList list;
    private final boolean hasPanels;

    public ConfigScreen(Screen parentScreen) {
        this(parentScreen, true);
    }

    public ConfigScreen(Screen parentScreen, boolean enablePanels) {
        super(parentScreen, TITLE, 4);
        this.hasPanels = enablePanels;
    }

    @Override
    protected void init() {
        super.init();

        if (!hasPanels) {
            this.removeWidget(panels);
            this.addRenderableOnly(new Label(TITLE, this.width / 2, 14, true));
        }

        // -- bottom buttons -- //

        //discard
        this.addRenderableWidget(new TexturedButton(width / 2 - 122 - 64, height - 24, 120, 20, FiguraText.of("gui.cancel"), null, button -> {
            ConfigManager.discardConfig();
            this.minecraft.setScreen(parentScreen);
        }));

        addRenderableWidget(new TexturedButton(width / 2 + 4 - 64, height - 24, 120, 20, FiguraText.of("gui.apply"), null, button -> {
            ConfigManager.applyConfig();
            list.updateList();
        }));

        //done
        addRenderableWidget(new TexturedButton(width / 2 + 130 - 64, height - 24, 120, 20, FiguraText.of("gui.done"), null,
                bx -> this.minecraft.setScreen(parentScreen)
        ));



        // -- config list -- //

        int width = Math.min(this.width - 8, 420);
        this.addRenderableWidget(list = new ConfigList((this.width - width) / 2, 28, width, height - 56));
    }

    @Override
    public void removed() {
        ConfigManager.applyConfig();
        ConfigManager.saveConfig();
        list.updateList();

        super.removed();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        KeyMapping bind = list.focusedBinding;
        //attempt to set keybind
        if (bind != null) {
            bind.setKey(InputConstants.Type.MOUSE.getOrCreate(button));
            list.focusedBinding = null;
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        KeyMapping bind = list.focusedBinding;
        //attempt to set keybind
        if (bind != null) {
            bind.setKey(keyCode == 256 ? InputConstants.UNKNOWN: InputConstants.getKey(keyCode, scanCode));
            list.focusedBinding = null;
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }
}
