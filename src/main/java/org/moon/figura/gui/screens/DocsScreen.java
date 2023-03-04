package org.moon.figura.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moon.figura.utils.FiguraText;

public class DocsScreen extends AbstractPanelScreen {

    public static final Component TITLE = FiguraText.of("gui.panels.title.docs");

    public DocsScreen(Screen parentScreen) {
        super(parentScreen, TITLE, DocsScreen.class);
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    protected void init() {
        super.init();
    }
}
