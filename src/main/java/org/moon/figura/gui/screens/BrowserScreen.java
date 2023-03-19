package org.moon.figura.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.widgets.TexturedButton;
import org.moon.figura.utils.FiguraText;

public class BrowserScreen extends AbstractPanelScreen {

    public BrowserScreen(Screen parentScreen) {
        super(parentScreen, FiguraText.of("gui.panels.title.browser"));
    }

    @Override
    public void init() {
        super.init();

        int y = -84;
        this.addRenderableWidget(new TexturedButton(width / 2 - 50, height / 2 + (y += 24), 100, 20, Component.literal("default toast"), FiguraText.of("backend.error"), button -> {
            FiguraToast.sendToast("default", "test", FiguraToast.ToastType.DEFAULT);
        }));
        this.addRenderableWidget(new TexturedButton(width / 2 - 50, height / 2 + (y += 24), 100, 20, Component.literal("error toast"), Component.literal("test2"), button -> {
            FiguraToast.sendToast("error", "test", FiguraToast.ToastType.ERROR);
        }));
        this.addRenderableWidget(new TexturedButton(width / 2 - 50, height / 2 + (y += 24), 100, 20, Component.literal("warning toast"), Component.literal("test3\novo"), button -> {
            FiguraToast.sendToast("warning", "test", FiguraToast.ToastType.WARNING);
        }));
        this.addRenderableWidget(new TexturedButton(width / 2 - 50, height / 2 + (y += 24), 100, 20, Component.literal("cheese toast"), Component.literal("test4\n\nhehe"), button -> {
            FiguraToast.sendToast("cheese", "test", FiguraToast.ToastType.CHEESE);
        }));
        this.addRenderableWidget(new TexturedButton(width / 2 - 50, height / 2 + (y + 24), 100, 20, Component.literal("fran toast"), Component.literal("fran ❤❤❤"), button -> {
            FiguraToast.sendToast("fran", "test", FiguraToast.ToastType.FRAN);
        }));
    }
}
