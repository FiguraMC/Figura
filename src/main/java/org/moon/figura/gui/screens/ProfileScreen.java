package org.moon.figura.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.widgets.TexturedButton;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

public class ProfileScreen extends AbstractPanelScreen {

    public static final Component TITLE = FiguraText.of("gui.panels.title.profile").withStyle(ChatFormatting.RED);

    private TexturedButton button;

    public ProfileScreen(Screen parentScreen) {
        super(parentScreen, TITLE, 0);
    }

    @Override
    public void init() {
        super.init();

        FiguraToast.sendToast("not yet!", "<3");

        this.addRenderableWidget(button = new TexturedButton(width / 2 - 30, height / 2 - 30, 60, 20, Component.literal("meow"),
                Component.literal("test").append("\n").append("one line").append("\n\n").append("two lines").append("\n").append("\n").append("two lines").append("\n\n\n").append("three lines").append("\n").append("\n").append("\n").append("three lines").append("\n"), button -> {
            FiguraToast.sendToast(Component.literal("Backend restarting").setStyle(Style.EMPTY.withColor(0x99BBEE)), "in 10 minutes!", FiguraToast.ToastType.DEFAULT);
        }));

        this.addRenderableWidget(new TexturedButton(width / 2 - 30, height / 2 + 10, 60, 20, Component.literal("meow"), TextUtils.tryParseJson(
                "{\"text\": \"△🟥🟧🟨🟩\n🟦🟪🟫⬜⬛\n\n❗❌🧀🍔🦐\n\n\n🌙🌀❤☆★\n\",\"font\": \"figura:default\"}"), button -> {
            FiguraToast.sendToast(Component.literal("Backend restarting").setStyle(Style.EMPTY.withColor(0x99BBEE)), "in 10 minutes!", FiguraToast.ToastType.DEFAULT);
        }));
    }

    @Override
    public void renderOverlays(PoseStack stack, int mouseX, int mouseY, float delta) {
        UIHelper.highlight(stack, button.x, button.y ,button.getWidth(), button.getHeight(), this.width, this.height);
        super.renderOverlays(stack, mouseX, mouseY, delta);
    }
}
