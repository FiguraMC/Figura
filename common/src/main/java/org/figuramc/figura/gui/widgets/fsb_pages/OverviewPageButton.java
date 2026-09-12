package org.figuramc.figura.gui.widgets.fsb_pages;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.ui.UIHelper;
import org.jetbrains.annotations.Nullable;

// Page button that also shows connection state.
public class OverviewPageButton extends PageButton {
    private final Component status;
    private final FiguraVec3 subtextColor;

    public OverviewPageButton(
            int x, int y, int width, int height,
            Component text, Component tooltip,
            OnPress pressAction,
            int hue,
            String identifier, Component status
    ) {
        super(x, y, width, height, text, tooltip, pressAction, hue, identifier);
        this.status = status;
        ColorUtils.ColorTheme theme = ColorUtils.ColorTheme.of(hue); // this is a guaranteed cache hit. nice!
        subtextColor = theme.stop(7);
    }

    public static OverviewPageButton of(
            String identifier, Component name, Component status, @Nullable Component tooltip,
            int height, int color, OnPress pressAction
    ) {
        return new OverviewPageButton(0, 0, 100, height, name, tooltip, pressAction, color, identifier, status);
    }

    @Override
    protected void renderAdditionalContent(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        gui.setColor((float) subtextColor.x, (float) subtextColor.y, (float) subtextColor.z, 1f);
        // Yoink smallcaps from Minecraft font files for use with translatable? Maybe? Unsure.
        // For now, full height.
        UIHelper.renderScrollingText(
                gui, status, getX() + 4, getY() + 14, getWidth() - 2, getTextColor()
        );
    }
}
