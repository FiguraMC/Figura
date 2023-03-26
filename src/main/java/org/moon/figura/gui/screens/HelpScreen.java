package org.moon.figura.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.moon.figura.gui.widgets.Label;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.TextUtils;

public class HelpScreen extends AbstractPanelScreen {

    public HelpScreen(Screen parentScreen) {
        super(parentScreen, FiguraText.of("gui.panels.title.help"));
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new Label(Component.empty().append("TEST ").append(Component.literal("LABEL :3").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "")))), width / 2, height / 2, 3f, 200, true, TextUtils.Alignment.CENTER, 0xFF72AD));
    }
}
