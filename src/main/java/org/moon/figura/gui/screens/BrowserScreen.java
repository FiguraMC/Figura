package org.moon.figura.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.widgets.TexturedButton;
import org.moon.figura.utils.FiguraText;

public class BrowserScreen extends AbstractPanelScreen {

    public BrowserScreen(Screen parentScreen) {
        super(parentScreen, new FiguraText("gui.panels.title.browser"));
    }

    @Override
    public void init() {
        super.init();

        int y = -84;
        this.addRenderableWidget(new TexturedButton(width / 2 - 50, height / 2 + (y += 24), 100, 20, new TextComponent("default toast"), new FiguraText("backend.error"), button -> {
            FiguraToast.sendToast("default", "test", FiguraToast.ToastType.DEFAULT);
        }));
        this.addRenderableWidget(new TexturedButton(width / 2 - 50, height / 2 + (y += 24), 100, 20, new TextComponent("error toast"), new TextComponent("test2"), button -> {
            FiguraToast.sendToast("error", "test", FiguraToast.ToastType.ERROR);
        }));
        this.addRenderableWidget(new TexturedButton(width / 2 - 50, height / 2 + (y += 24), 100, 20, new TextComponent("warning toast"), new TextComponent("test3\novo"), button -> {
            FiguraToast.sendToast("warning", "test", FiguraToast.ToastType.WARNING);
        }));
        this.addRenderableWidget(new TexturedButton(width / 2 - 50, height / 2 + (y += 24), 100, 20, new TextComponent("cheese toast"), new TextComponent("test4\n\nhehe"), button -> {
            FiguraToast.sendToast("cheese", "test", FiguraToast.ToastType.CHEESE);
        }));
        this.addRenderableWidget(new TexturedButton(width / 2 - 50, height / 2 + (y + 24), 100, 20, new TextComponent("fran toast"), new TextComponent("fran ❤❤❤"), button -> {
            FiguraToast.sendToast("fran", "test", FiguraToast.ToastType.FRAN);
        }));
    }
}
