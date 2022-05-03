package org.moon.figura.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.widgets.TexturedButton;
import org.moon.figura.utils.FiguraText;

public class KeybindScreen extends AbstractPanelScreen {

    public static final Component TITLE = new FiguraText("gui.panels.title.keybind");

    public KeybindScreen(Screen parentScreen) {
        super(parentScreen, TITLE, 2);
    }

    @Override
    protected void init() {
        super.init();

        FiguraToast.sendToast(new TextComponent("lol nope").setStyle(Style.EMPTY.withColor(0xFFADAD)), FiguraToast.ToastType.DEFAULT);

        this.addRenderableWidget(new TexturedButton(width / 2 - 30, height / 2 + 10, 60, 20, new TextComponent("test"), new TextComponent("test"), button -> {
            FiguraToast.sendToast("test", "test", FiguraToast.ToastType.DEFAULT);
        }));
    }
}
