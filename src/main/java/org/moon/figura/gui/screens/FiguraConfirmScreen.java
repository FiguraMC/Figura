package org.moon.figura.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;
import org.moon.figura.config.Configs;
import org.moon.figura.utils.ui.UIHelper;

public class FiguraConfirmScreen extends ConfirmScreen {

    public FiguraConfirmScreen(BooleanConsumer callback, Component title, Component message) {
        super(callback, title, message);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        double speed = Configs.BACKGROUND_SCROLL_SPEED.tempValue * 0.5;
        UIHelper.renderAnimatedBackground(stack, AbstractPanelScreen.BACKGROUND, 0, 0, this.width, this.height, 64, 64, speed, delta);
        super.render(stack, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(PoseStack matrices) {}

    public static class FiguraConfirmLinkScreen extends ConfirmLinkScreen {

        public FiguraConfirmLinkScreen(BooleanConsumer callback, String link, boolean trusted) {
            super(callback, link, trusted);
        }

        @Override
        public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
            double speed = Configs.BACKGROUND_SCROLL_SPEED.tempValue * 0.5;
            UIHelper.renderAnimatedBackground(stack, AbstractPanelScreen.BACKGROUND, 0, 0, this.width, this.height, 64, 64, speed, delta);
            super.render(stack, mouseX, mouseY, delta);
        }

        @Override
        public void renderBackground(PoseStack matrices) {}
    }
}
