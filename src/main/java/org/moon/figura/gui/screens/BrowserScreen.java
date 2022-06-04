package org.moon.figura.gui.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.widgets.TexturedButton;
import org.moon.figura.utils.FiguraText;

public class BrowserScreen extends AbstractPanelScreen {

    public static final Component TITLE = FiguraText.of("gui.panels.title.browser").withStyle(ChatFormatting.RED);

    public BrowserScreen(Screen parentScreen) {
        super(parentScreen, TITLE, 1);
    }

    @Override
    public void init() {
        super.init();

        FiguraToast.sendToast("not yet!", "<3");

        int y = -72;
        this.addRenderableWidget(new TexturedButton(width / 2 - 30, height / 2 + (y += 24), 60, 20, Component.literal("default toast"), FiguraText.of("backend.error"), button -> {
            FiguraToast.sendToast("default", "test", FiguraToast.ToastType.DEFAULT);
        }));
        this.addRenderableWidget(new TexturedButton(width / 2 - 30, height / 2 + (y += 24), 60, 20, Component.literal("error toast"), Component.literal("test2"), button -> {
            FiguraToast.sendToast("error", "test", FiguraToast.ToastType.ERROR);
        }));
        this.addRenderableWidget(new TexturedButton(width / 2 - 30, height / 2 + (y += 24), 60, 20, Component.literal("warning toast"), Component.literal("test3\novo"), button -> {
            FiguraToast.sendToast("warning", "test", FiguraToast.ToastType.WARNING);
        }));
        this.addRenderableWidget(new TexturedButton(width / 2 - 30, height / 2 + (y + 24), 60, 20, Component.literal("cheese toast"), Component.literal("test4\n\nhehe"), button -> {
            FiguraToast.sendToast("cheese", "test", FiguraToast.ToastType.CHEESE);
        }));
    }
}
