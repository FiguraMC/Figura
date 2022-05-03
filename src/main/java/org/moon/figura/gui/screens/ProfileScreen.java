package org.moon.figura.gui.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.utils.FiguraText;

public class ProfileScreen extends AbstractPanelScreen {

    public static final Component TITLE = new FiguraText("gui.panels.title.profile").withStyle(ChatFormatting.RED);

    public ProfileScreen(Screen parentScreen) {
        super(parentScreen, TITLE, 0);
    }

    @Override
    public void init() {
        super.init();

        FiguraToast.sendToast("not yet!", "<3");
    }
}
