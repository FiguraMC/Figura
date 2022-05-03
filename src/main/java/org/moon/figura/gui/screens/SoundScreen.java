package org.moon.figura.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.widgets.TexturedButton;
import org.moon.figura.utils.FiguraText;

public class SoundScreen extends AbstractPanelScreen {

    public static final Component TITLE = new FiguraText("gui.panels.title.sound");

    public SoundScreen(Screen parentScreen) {
        super(parentScreen, TITLE, 2);
    }

    @Override
    protected void init() {
        super.init();

        FiguraToast.sendToast(new TextComponent("lol nope").setStyle(Style.EMPTY.withColor(0xFFADAD)), FiguraToast.ToastType.DEFAULT);

        this.addRenderableWidget(new TexturedButton(width / 2 - 30, height / 2 - 30, 60, 20, new TextComponent("test2"), new TextComponent("test2"), button -> {
            FiguraToast.sendToast("test2", "test2", FiguraToast.ToastType.DEFAULT);
        }));
    }
}
