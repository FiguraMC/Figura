package org.figuramc.figura.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.lists.KeybindList;
import org.figuramc.figura.lua.api.keybind.FiguraKeybind;
import org.figuramc.figura.utils.FiguraText;

public class KeybindScreen extends AbstractPanelScreen {

    private final Screen sourcePanel;

    private KeybindList list;

    public KeybindScreen(AbstractPanelScreen parentScreen) {
        super(parentScreen.parentScreen, FiguraText.of("gui.panels.title.keybind"));
        sourcePanel = parentScreen;
    }

    @Override
    public Class<? extends Screen> getSelectedPanel() {
        return sourcePanel.getClass();
    }

    @Override
    protected void init() {
        super.init();

        Avatar owner = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());

        // -- bottom buttons -- // 

        // reset
        Button reset;
        this.addRenderableWidget(reset = new Button(width / 2 - 122, height - 24, 120, 20, FiguraText.of("gui.reset_all"), null, button -> {
            if (owner == null || owner.luaRuntime == null)
                return;

            for (FiguraKeybind keybind : owner.luaRuntime.keybinds.keyBindings)
                keybind.resetDefaultKey();
            list.updateBindings();
        }));
        reset.setActive(false);

        // back
        addRenderableWidget(new Button(width / 2 + 4, height - 24, 120, 20, FiguraText.of("gui.done"), null, bx -> onClose()));

        // -- list -- // 

        int listWidth = Math.min(this.width - 8, 420);
        this.addRenderableWidget(list = new KeybindList((this.width - listWidth) / 2, 28, listWidth, height - 56, owner, reset));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(sourcePanel);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return list.updateKey(InputConstants.Type.MOUSE.getOrCreate(button)) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return list.updateKey(keyCode == 256 ? InputConstants.UNKNOWN : InputConstants.getKey(keyCode, scanCode)) || super.keyPressed(keyCode, scanCode, modifiers);
    }
}
