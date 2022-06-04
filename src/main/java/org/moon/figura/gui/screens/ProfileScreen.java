package org.moon.figura.gui.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.widgets.TexturedButton;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.TextUtils;

public class ProfileScreen extends AbstractPanelScreen {

    public static final Component TITLE = FiguraText.of("gui.panels.title.profile").withStyle(ChatFormatting.RED);

    public ProfileScreen(Screen parentScreen) {
        super(parentScreen, TITLE, 0);
    }

    @Override
    public void init() {
        super.init();

        FiguraToast.sendToast("not yet!", "<3");

        this.addRenderableWidget(new TexturedButton(width / 2 - 30, height / 2 - 30, 60, 20, Component.literal("meow"), null, button -> {
            FiguraToast.sendToast(Component.literal("Backend restarting").setStyle(Style.EMPTY.withColor(0x99BBEE)), "in 10 minutes!", FiguraToast.ToastType.DEFAULT);
        }));

        this.addRenderableWidget(new TexturedButton(width / 2 - 30, height / 2 + 10, 60, 20, Component.literal("meow"), TextUtils.tryParseJson(
                "{\"text\": \"△🟥🟧🟨🟩\n🟦🟪🟫⬜⬛\n\n❗❌🧀🍔🦐\n🌙🌀🚫❤★\",\"font\": \"figura:default\"}"), button -> {
            FiguraToast.sendToast(Component.literal("Backend restarting").setStyle(Style.EMPTY.withColor(0x99BBEE)), "in 10 minutes!", FiguraToast.ToastType.DEFAULT);
        }));
    }
}
