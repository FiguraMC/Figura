package org.figuramc.figura.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.TextUtils;

public class ProfileScreen extends AbstractPanelScreen {

    public ProfileScreen(Screen parentScreen) {
        super(parentScreen, FiguraText.of("gui.panels.title.profile"));
    }

    @Override
    public void init() {
        super.init();

        this.addRenderableWidget(new Button(width / 2 - 30, height / 2 - 30, 60, 20, Component.literal("meow"),
                Component.literal("test").append("\n").append("one line").append("\n\n").append("two lines").append("\n").append("\n").append("two lines").append("\n\n\n").append("three lines").append("\n").append("\n").append("\n").append("three lines").append("\n"), button -> {
            FiguraToast.sendToast(Component.literal("Backend restarting").setStyle(Style.EMPTY.withColor(0x99BBEE)), "in 10 minutes!", FiguraToast.ToastType.DEFAULT);
        }));

        this.addRenderableWidget(new Button(width / 2 - 30, height / 2 + 10, 60, 20, Component.literal("meow"), TextUtils.tryParseJson(
                "{\"text\": \"△❗\n❌\uD83E\uDDC0\n\n☄❤\n\n\n☆★\",\"font\": \"figura:badges\"}"), button -> {
            FiguraToast.sendToast(Component.literal("Backend restarting").setStyle(Style.EMPTY.withColor(0x99BBEE)), "in 10 minutes!", FiguraToast.ToastType.DEFAULT);
        }));
    }

    @Override
    public void renderOverlays(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        // UIHelper.highlight(stack, button, TextUtils.tryParseJson("{\"text\":\"🦐🦐🦐🦐\",\"font\":\"figura:emojis\"}"));
        super.renderOverlays(gui, mouseX, mouseY, delta);
    }
}
