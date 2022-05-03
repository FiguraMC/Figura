package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.moon.figura.utils.FiguraIdentifier;

public class ScrollBarWidget extends AbstractWidget {

    // -- fields -- //

    public static final ResourceLocation SCROLLBAR_TEXTURE = new FiguraIdentifier("textures/gui/scrollbar.png");

    protected final int headHeight = 20;
    protected final int headWidth = 10;

    protected boolean isScrolling = false;
    protected boolean vertical = true;

    protected float scrollPos;
    protected float scrollPrecise;
    protected float scrollRatio = 1f;

    protected OnPress onPress;

    // -- constructors -- //

    public ScrollBarWidget(int x, int y, int width, int height, float initialValue) {
        super(x, y, width, height, TextComponent.EMPTY);
        scrollPrecise = initialValue;
        scrollPos = initialValue;
    }

    // -- methods -- //

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.isHoveredOrFocused() || !this.isMouseOver(mouseX, mouseY))
            return false;

        if (button == 0) {
            //jump to pos when not clicking on head
            float scrollPos = Mth.lerp(scrollPrecise, 0f, (vertical ? height - headHeight : width - headWidth) + 2f);

            if (vertical && mouseY < y + scrollPos || mouseY > y + scrollPos + headHeight)
                scroll(-(y + scrollPos + headHeight / 2f - mouseY));
            else if (!vertical && mouseX < x + scrollPos || mouseX > x + scrollPos + headWidth)
                scroll(-(x + scrollPos + headWidth / 2f - mouseX));

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
            if (vertical && mouseY >= this.y && mouseY <= this.y + this.height) {
                scroll(deltaY);
                return true;
            }
            //horizontal drag
            else if (!vertical && mouseX >= this.x && mouseX <= this.x + this.width) {
                scroll(deltaX);
                return true;
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!this.active) return false;
        scroll(-amount * (vertical ? height : width) * 0.05f * scrollRatio);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.active) return false;

        if (keyCode > 261 && keyCode < 266) {
            scroll((keyCode % 2 == 0 ? 1 : -1) * (vertical ? height : width) * 0.05f * scrollRatio);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    //apply scroll value
    protected void scroll(double amount) {
        scrollPrecise += amount / ((vertical ? height - headHeight : width - headWidth) + 2f);
        setScrollProgress(scrollPrecise);
    }

    //animate scroll head
    protected void lerpPos(float delta) {
        scrollPos = (float) Mth.lerp(1 - Math.pow(0.2f, delta), scrollPos, getScrollProgress());
    }

    //render the scroll
    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SCROLLBAR_TEXTURE);

        //render bar
        blit(stack, x, y, width, 1, 10f, isScrolling ? 20f : 0f, 10, 1, 20, 40);
        blit(stack, x, y + 1, width, height - 2, 10f, isScrolling ? 21f : 1f, 10, 18, 20, 40);
        blit(stack, x, y + height - 1, width, 1, 10f, isScrolling ? 39f : 19f, 10, 1, 20, 40);

        //render head
        lerpPos(delta);
        blit(stack, x, y + Math.round(Mth.lerp(scrollPos, 0, height - headHeight)), 0f, isHoveredOrFocused() || isScrolling ? headHeight : 0f, headWidth, headHeight, 20, 40);
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
    }

    // -- getters and setters -- //

    //set scrollbar height
    public void setHeight(int height) {
        this.height = height;
    }

    //get scroll value
    public float getScrollProgress() {
        return scrollPrecise;
    }

    //manually set scroll
    public void setScrollProgress(float amount) {
        setScrollProgress(amount, false);
    }

    //manually set scroll with optional clamping
    public void setScrollProgress(float amount, boolean force) {
        scrollPrecise = force ? amount : Mth.clamp(amount, 0f, 1f);

        if (onPress != null)
            onPress.onPress(this);
    }

    //set button action
    public void setAction(OnPress onPress) {
        this.onPress = onPress;
    }

    //set scroll ratio
    public void setScrollRatio(float entryHeight, float heightDiff) {
        scrollRatio = (height + entryHeight) / (heightDiff / 2f);
    }

    //press action
    public interface OnPress {
        void onPress(ScrollBarWidget scrollbar);
    }
}
