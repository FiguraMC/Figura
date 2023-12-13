package org.figuramc.figura.gui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.ui.UIHelper;

public class SliderWidget extends ScrollBarWidget {

    // -- fields -- // 

    public static final ResourceLocation SLIDER_TEXTURE = new FiguraIdentifier("textures/gui/slider.png");

    protected final int headHeight = 11;
    protected final int headWidth = 11;
    protected final boolean showSteps;

    private int max;
    private double stepSize;
    private double steppedPos;

    // -- constructors -- // 

    public SliderWidget(int x, int y, int width, int height, double initialValue, int maxValue, boolean showSteps) {
        super(x, y, width, height, initialValue);
        this.vertical = false;
        this.showSteps = showSteps;
        this.steppedPos = initialValue;
        setMax(maxValue);
    }

    // -- methods -- // 

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount, double d) {
        if (!this.isActive()) return false;
        scroll(stepSize * Math.signum(-amount-d) * (getWidth() - headWidth + 2d));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isActive()) return false;

        if (keyCode > 261 && keyCode < 266) {
            scroll(stepSize * (keyCode % 2 == 0 ? 1 : -1) * Math.max(modifiers * 10, 1) * (getWidth() - headWidth + 2d));
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void scroll(double amount) {
        // normal scroll
        super.scroll(amount);

        // get the closest step
        steppedPos = getClosestStep();
    }

    private double getClosestStep() {
        // get closer steps
        double lowest = scrollPrecise - scrollPrecise % stepSize;
        double highest = lowest + stepSize;

        // get distance
        double distanceLow = Math.abs(lowest - scrollPrecise);
        double distanceHigh = Math.abs(highest - scrollPrecise);

        // return closest
        return distanceLow < distanceHigh ? lowest : highest;
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        // set hovered
        this.isHovered = this.isMouseOver(mouseX, mouseY);

        // render button
        UIHelper.enableBlend();
        int x = getX();
        int y = getY();
        int width = getWidth();

        // draw bar
        gui.blit(SLIDER_TEXTURE, x, y + 3, width, 5, isScrolling ? 10f : 0f, 0f, 5, 5, 33, 16);

        // draw steps
        if (showSteps) {
            for (int i = 0; i < max; i++) {
                gui.blit(SLIDER_TEXTURE, (int) Math.floor(x + 3 + stepSize * i * (width - 11)), y + 3, 5, 5, isScrolling ? 15f : 5f, 0f, 5, 5, 33, 16);
            }
        }

        // draw header
        lerpPos(delta);
        gui.blit(SLIDER_TEXTURE, (int) (x + Math.round(Mth.lerp(scrollPos, 0, width - headWidth))), y, isActive() ? (isHoveredOrFocused() || isScrolling ? headWidth * 2 : headWidth) : 0f, 5f, headWidth, headHeight, 33, 16);
    }

    // -- getters and setters -- // 

    @Override
    public double getScrollProgress() {
        return steppedPos;
    }

    @Override
    public void setScrollProgress(double amount, boolean force) {
        steppedPos = force ? amount : Mth.clamp(amount, 0d, 1d);
        super.setScrollProgress(amount, force);
    }

    public int getMax() {
        return max;
    }

    public void setMax(int maxValue) {
        // set steps data
        this.max = maxValue;
        this.stepSize = 1d / (maxValue - 1);
    }

    public int getIntValue() {
        return (int) Math.round(getScrollProgress() * (getMax() - 1));
    }
}
