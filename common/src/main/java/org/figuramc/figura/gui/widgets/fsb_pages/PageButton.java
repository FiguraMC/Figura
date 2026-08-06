package org.figuramc.figura.gui.widgets.fsb_pages;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.ui.UIHelper;
import org.jetbrains.annotations.Nullable;

public class PageButton extends Button {
    private static final ResourceLocation TEXTURE = new FiguraIdentifier("textures/gui/button.png");

    public boolean isActivePage;

    public PageButton(
            int x, int y, int width, int height,
            Component text, Component tooltip, OnPress pressAction,
            int color
    ) {
        super(x, y, width, height, text, tooltip, pressAction);
    }

    public static PageButton of(Component name, @Nullable Component tooltip, OnPress pressAction, int color) {
        return new PageButton(0, 0, 100, 20, name, tooltip, pressAction, color);
    }

    @Override
    public void relayout() {
        super.relayout();
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        int u = (isActive() ? isHoveredOrFocused() ? 2 : 1 : 0) * 16;
        // Just chop off the right side of the button texture xd
        UIHelper.blitSlicedAlt(
                gui,
                getX(), getY(), getWidth(), getHeight(),
                1.0f, TEXTURE,
                1, 0, 1, 1,
                48, 32,
                u, 0, 15, 16,
                1.0f
        );

        PoseStack pose = gui.pose();
        pose.pushPose();
        pose.translate(0, 0, 3.0f);
        UIHelper.renderScrollingText(
                gui, getMessage(), getX() + 4, getY() + 4, getWidth() - 2, getTextColor()
        );
        pose.popPose();
    }
}
