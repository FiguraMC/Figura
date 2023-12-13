package org.figuramc.figura.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.MathUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class ScrollBarWidget extends AbstractWidget implements FiguraWidget {

    // -- fields -- // 

    public static final ResourceLocation SCROLLBAR_TEXTURE = new FiguraIdentifier("textures/gui/scrollbar.png");

    protected final int headHeight = 20;
    protected final int headWidth = 10;

    protected boolean isScrolling = false;
    protected boolean vertical = true;

    protected double scrollPos;
    protected double scrollPrecise;
    protected double scrollRatio = 1d;

    protected OnPress onPress;

    // -- constructors -- // 

    public ScrollBarWidget(int x, int y, int width, int height, double initialValue) {
        super(x, y, width, height, Component.empty());
        scrollPrecise = initialValue;
        scrollPos = initialValue;
    }

    // -- methods -- // 

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isActive() || !this.isHoveredOrFocused() || !this.isMouseOver(mouseX, mouseY))
            return false;

        if (button == 0) {
            // jump to pos when not clicking on head
            double scrollPos = Mth.lerp(scrollPrecise, 0d, (vertical ? getHeight() - headHeight : getWidth() - headWidth) + 2d);

            if (vertical && mouseY < getY() + scrollPos || mouseY > getY() + scrollPos + headHeight)
                scroll(-(getY() + scrollPos + headHeight / 2d - mouseY));
            else if (!vertical && mouseX < getX() + scrollPos || mouseX > getX() + scrollPos + headWidth)
                scroll(-(getX() + scrollPos + headWidth / 2d - mouseX));

            isScrolling = true;
            playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && isScrolling) {
            isScrolling = false;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isScrolling) {
            // vertical drag
            if (vertical) {
                if (Math.signum(deltaY) == -1) {
                    if (mouseY <= this.getY() + this.getHeight()) {
                        scroll(deltaY);
                        return true;
                    }

                } else if (mouseY >= this.getY()) {
                    scroll(deltaY);
                    return true;
                }
            }
            // horizontal drag
            else if (Math.signum(deltaX) == -1) {
                if (mouseX <= this.getX() + this.getWidth()) {
                    scroll(deltaX);
                    return true;
                }
            } else if (mouseX >= this.getX()) {
                scroll(deltaX);
                return true;
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount, double d) {
        if (!this.isActive()) return false;
        scroll(-amount-d * (vertical ? getHeight() : getWidth()) * 0.05d * scrollRatio);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isActive()) return false;

        if (keyCode > 261 && keyCode < 266) {
            scroll((keyCode % 2 == 0 ? 1 : -1) * (vertical ? getHeight() : getWidth()) * 0.05d * scrollRatio);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return UIHelper.isMouseOver(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY);
    }

    // apply scroll value
    protected void scroll(double amount) {
        scrollPrecise += amount / ((vertical ? getHeight() - headHeight : getWidth() - headWidth) + 2d);
        setScrollProgress(scrollPrecise);
    }

    // animate scroll head
    protected void lerpPos(float delta) {
        float lerpDelta = MathUtils.magicDelta(0.2f, delta);
        scrollPos = Mth.lerp(lerpDelta, scrollPos, getScrollProgress());
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        if (!isVisible())
            return;

        isHovered = this.isMouseOver(mouseX, mouseY);

        // render the scroll
        UIHelper.enableBlend();
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        // render bar
        gui.blit(SCROLLBAR_TEXTURE, x, y, width, 1, 10f, isScrolling ? 20f : 0f, 10, 1, 20, 40);
        gui.blit(SCROLLBAR_TEXTURE, x, y + 1, width, height - 2, 10f, isScrolling ? 21f : 1f, 10, 18, 20, 40);
        gui.blit(SCROLLBAR_TEXTURE, x, y + height - 1, width, 1, 10f, isScrolling ? 39f : 19f, 10, 1, 20, 40);

        // render head
        lerpPos(delta);
        gui.blit(SCROLLBAR_TEXTURE, x, (int) (y + Math.round(Mth.lerp(scrollPos, 0, height - headHeight))), 0f, isHoveredOrFocused() || isScrolling ? headHeight : 0f, headWidth, headHeight, 20, 40);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }

    // -- getters and setters -- // 

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public int getX() {
        return super.getX();
    }

    @Override
    public void setX(int x) {
        super.setX(x);
    }

    @Override
    public int getY() {
        return super.getY();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
    }

    @Override
    public int getWidth() {
        return super.getWidth();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
    }

    @Override
    public int getHeight() {
        return super.getHeight();
    }

    // set scrollbar height
    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // get scroll value
    public double getScrollProgress() {
        return scrollPrecise;
    }

    // manually set scroll
    public void setScrollProgress(double amount) {
        setScrollProgress(amount, false);
    }

    public void setScrollProgressNoAnim(double amount) {
        setScrollProgress(amount, false);
        scrollPos = scrollPrecise;
    }

    // manually set scroll with optional clamping
    public void setScrollProgress(double amount, boolean force) {
        amount = Double.isNaN(amount) ? 0 : amount;
        scrollPrecise = force ? amount : Mth.clamp(amount, 0d, 1d);

        if (onPress != null)
            onPress.onPress(this);
    }

    // set button action
    public void setAction(OnPress onPress) {
        this.onPress = onPress;
    }

    // set scroll ratio
    public void setScrollRatio(double entryHeight, double heightDiff) {
        scrollRatio = (getHeight() + entryHeight) / (heightDiff / 2d);
    }

    // press action
    public interface OnPress {
        void onPress(ScrollBarWidget scrollbar);
    }
}
