package org.moon.figura.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moon.figura.config.ConfigManager;
import org.moon.figura.gui.widgets.TexturedButton;
import org.moon.figura.gui.widgets.lists.ConfigList;
import org.moon.figura.utils.FiguraText;

public class ConfigScreen extends AbstractPanelScreen {

    public static final Component TITLE = new FiguraText("gui.panels.title.settings");

    private ConfigList list;
    private final boolean panelState;

    public ConfigScreen(Screen parentScreen) {
        this(parentScreen, true);
    }

    public ConfigScreen(Screen parentScreen, boolean enablePanels) {
        super(parentScreen, TITLE, 4);
        this.panelState = enablePanels;
    }

    @Override
    protected void init() {
        super.init();
        super.panels.setActive(panelState);

        // -- bottom buttons -- //

        //discard
        this.addRenderableWidget(new TexturedButton(width / 2 - 122, height - 24, 120, 20, new FiguraText("gui.cancel"), null, button -> {
            ConfigManager.discardConfig();
            list.updateList();
        }));

        //back
        addRenderableWidget(new TexturedButton(width / 2 + 4, height - 24, 120, 20, new FiguraText("gui.back"), null,
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
