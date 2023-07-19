package org.figuramc.figura.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.figuramc.figura.gui.widgets.Label;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.TextUtils;

public class DocsScreen extends AbstractPanelScreen {

    private final Screen sourcePanel;

    public DocsScreen(AbstractPanelScreen parentScreen) {
        super(parentScreen.parentScreen, new FiguraText("gui.panels.title.docs"));
        sourcePanel = parentScreen;
    }

    @Override
    public Class<? extends Screen> getSelectedPanel() {
        return sourcePanel.getClass();
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new Label(TextComponent.EMPTY.copy().append("TEST ").append(new TextComponent("LABEL :3").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "")))), width / 2, height / 2, 3f, 200, true, TextUtils.Alignment.CENTER, 0xFF72AD));
    }
}
