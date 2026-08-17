package org.figuramc.figura.gui.widgets.fsb_pages;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.ui.UIHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

public class PageButton extends Button {
    // TODO: add additional resource for theming these if someone wants that (corners & stuff)
    //       we need one for the #FFFFFF edges anyway
    private static final ResourceLocation TEXTURE = new FiguraIdentifier("textures/gui/button_tintable.png");
    private final FiguraVec3 disabledColor; // self-explanatory
    private final FiguraVec3 defaultColor;  // self-explanatory
    private final FiguraVec3 selectedColor; // the current page
    private final FiguraVec3 hoveredColor;  // cursor hover / keyboard focus
    private final FiguraVec3 textColor;     // text color

    public boolean isCurrentPage;

    public PageButton(
            int x, int y, int width, int height,
            Component text, Component tooltip, OnPress pressAction,
            int hue
    ) {
        super(x, y, width, height, text, tooltip, pressAction);
        ColorUtils.ColorTheme theme = ColorUtils.ColorTheme.of(hue);
        disabledColor = theme.stop(1);
        defaultColor = theme.stop(3);
        selectedColor = theme.stop(5);
        hoveredColor = theme.stop(6);
        textColor = theme.stop(9);
    }

    public FiguraVec3 getSelectedColor() {
        return selectedColor.copy();
    }

    public static PageButton of(Component name, @Nullable Component tooltip, int height, OnPress pressAction, int color) {
        return new PageButton(0, 0, 100, height, name, tooltip, pressAction, color);
    }

    @Override
    public void relayout() {
        super.relayout();
    }

    private FiguraVec3 getDrawingColor() {
        if (isActive()) {
            if (isHoveredOrFocused()) {
                return hoveredColor;
            } else if (isCurrentPage) {
                return selectedColor;
            } else {
                return defaultColor;
            }
        } else {
            return disabledColor;
        }
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        FiguraVec3 color = getDrawingColor();

        gui.setColor((float) color.x, (float) color.y, (float) color.z, 1f);
        // Just chop off the right side of the button texture xd
        UIHelper.blitSlicedAlt(
                gui,
                getX(), getY(), getWidth(), getHeight(),
                1.0f, TEXTURE,
                1, 0, 1, 1,
                16, 16,
                0, 0, 15, 16,
                1.0f
        );

        PoseStack pose = gui.pose();
        pose.pushPose();
        pose.translate(0, 0, 3.0f);
        gui.setColor((float) textColor.x, (float) textColor.y, (float) textColor.z, 1f);
        UIHelper.renderScrollingText(
                gui, getMessage(), getX() + 4, getY() + 4, getWidth() - 2, getTextColor()
        );
        pose.popPose();
        gui.setColor(1f, 1f, 1f, 1f);
    }
}
