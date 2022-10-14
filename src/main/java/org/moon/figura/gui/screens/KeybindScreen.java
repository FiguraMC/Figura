package org.moon.figura.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.gui.widgets.TexturedButton;
import org.moon.figura.gui.widgets.lists.KeybindList;
import org.moon.figura.lua.api.keybind.FiguraKeybind;
import org.moon.figura.utils.FiguraText;

public class KeybindScreen extends AbstractPanelScreen {

    public static final Component TITLE = FiguraText.of("gui.panels.title.keybind");

    private KeybindList list;

    public KeybindScreen(Screen parentScreen) {
        super(parentScreen, TITLE, 2);
    }

    @Override
    protected void init() {
        super.init();

        Avatar owner = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());

        //list
        int listWidth = Math.min(this.width - 8, 420);
        this.addRenderableWidget(list = new KeybindList((this.width - listWidth) / 2, 28, listWidth, height - 56, owner));

        // -- bottom buttons -- //

        //discard
        this.addRenderableWidget(new TexturedButton(width / 2 - 122, height - 24, 120, 20, FiguraText.of("gui.reset_all"), null, button -> {
            if (owner == null || owner.luaRuntime == null)
                return;

            for (FiguraKeybind keybind : owner.luaRuntime.keybind.keyBindings)
                keybind.resetDefaultKey();
        }));

        //back
        addRenderableWidget(new TexturedButton(width / 2 + 4, height - 24, 120, 20, FiguraText.of("gui.done"), null,
                bx -> this.minecraft.setScreen(parentScreen)
        ));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        FiguraKeybind bind = list.focusedKeybind;
        //attempt to set keybind
        if (bind != null) {
            bind.setKey(InputConstants.Type.MOUSE.getOrCreate(button));
            list.focusedKeybind = null;
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        FiguraKeybind bind = list.focusedKeybind;
        //attempt to set keybind
        if (bind != null) {
            bind.setKey(keyCode == 256 ? InputConstants.UNKNOWN: InputConstants.getKey(keyCode, scanCode));
            list.focusedKeybind = null;
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }
}
