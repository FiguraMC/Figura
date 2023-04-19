package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.ui.UIHelper;

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
        super(x, y, width, height, TextComponent.EMPTY.copy());
        scrollPrecise = initialValue;
        scrollPos = initialValue;
    }

    // -- methods -- //

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isActive() || !this.isHoveredOrFocused() || !this.isMouseOver(mouseX, mouseY))
            return false;

        if (button == 0) {
            //jump to pos when not clicking on head
            double scrollPos = Mth.lerp(scrollPrecise, 0d, (vertical ? getHeight() - headHeight : getWidth() - headWidth) + 2d);

            if (vertical && mouseY < y + scrollPos || mouseY > y + scrollPos + headHeight)
                scroll(-(y + scrollPos + headHeight / 2d - mouseY));
            else if (!vertical && mouseX < x + scrollPos || mouseX > x + scrollPos + headWidth)
                scroll(-(x + scrollPos + headWidth / 2d - mouseX));

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
            //vertical drag
            if (vertical) {
                if (Math.signum(deltaY) == -1) {
                    if (mouseY <= this.getY() + this.getHeight()) {
                        scroll(deltaY);
                        return true;
                    }

                } else if (mouseY >= this.y) {
                    scroll(deltaY);
                    return true;
                }
            }
            //horizontal drag
            else if (Math.signum(deltaX) == -1) {
                if (mouseX <= this.getX() + this.getWidth()) {
                    scroll(deltaX);
                    return true;
                }
            } else if (mouseX >= this.x) {
                scroll(deltaX);
                return true;
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!this.isActive()) return false;
        scroll(-amount * (vertical ? getHeight() : getWidth()) * 0.05d * scrollRatio);
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

    //apply scroll value
    protected void scroll(double amount) {
        scrollPrecise += amount / ((vertical ? getHeight() - headHeight : getWidth() - headWidth) + 2d);
        setScrollProgress(scrollPrecise);
    }

    //animate scroll head
    protected void lerpPos(double delta) {
        scrollPos = Mth.lerp(1 - Math.pow(0.2d, delta), scrollPos, getScrollProgress());
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (!isVisible())
            return;

        isHovered = this.isMouseOver(mouseX, mouseY);
        renderButton(matrices, mouseX, mouseY, delta);
    }

    //render the scroll
    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
        UIHelper.setupTexture(SCROLLBAR_TEXTURE);
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        //render bar
        blit(stack, x, y, width, 1, 10f, isScrolling ? 20f : 0f, 10, 1, 20, 40);
        blit(stack, x, y + 1, width, height - 2, 10f, isScrolling ? 21f : 1f, 10, 18, 20, 40);
        blit(stack, x, y + height - 1, width, 1, 10f, isScrolling ? 39f : 19f, 10, 1, 20, 40);

        //render head
        lerpPos(delta);
        blit(stack, x, (int) (y + Math.round(Mth.lerp(scrollPos, 0, height - headHeight))), 0f, isHoveredOrFocused() || isScrolling ? headHeight : 0f, headWidth, headHeight, 20, 40);
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
    }

    // -- getters and setters -- //


    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    //set scrollbar height
    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    //get scroll value
    public double getScrollProgress() {
        return scrollPrecise;
    }

    //manually set scroll
    public void setScrollProgress(double amount) {
        setScrollProgress(amount, false);
    }

    public void setScrollProgressNoAnim(double amount) {
        setScrollProgress(amount, false);
        scrollPos = scrollPrecise;
    }

    //manually set scroll with optional clamping
    public void setScrollProgress(double amount, boolean force) {
        scrollPrecise = force ? amount : Mth.clamp(amount, 0d, 1d);

        if (onPress != null)
            onPress.onPress(this);
    }

    //set button action
    public void setAction(OnPress onPress) {
        this.onPress = onPress;
    }

    //set scroll ratio
    public void setScrollRatio(double entryHeight, double heightDiff) {
        scrollRatio = (getHeight() + entryHeight) / (heightDiff / 2d);
    }

    //press action
    public interface OnPress {
        void onPress(ScrollBarWidget scrollbar);
    }
}
