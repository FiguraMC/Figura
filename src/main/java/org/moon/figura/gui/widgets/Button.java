package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.ui.UIHelper;

public class Button extends net.minecraft.client.gui.components.Button implements FiguraWidget {

    //default textures
    private static final ResourceLocation TEXTURE = new FiguraIdentifier("textures/gui/button.png");

    //texture data
    protected Integer u;
    protected Integer v;

    protected final Integer textureWidth;
    protected final Integer textureHeight;
    protected final Integer regionSize;
    protected final ResourceLocation texture;

    //extra fields
    protected Component tooltip;
    private boolean hasBackground = true;

    //texture and text constructor
    public Button(int x, int y, int width, int height, Integer u, Integer v, Integer regionSize, ResourceLocation texture, Integer textureWidth, Integer textureHeight, Component text, Component tooltip, OnPress pressAction) {
        super(x, y, width, height, text, pressAction, DEFAULT_NARRATION);

        this.u = u;
        this.v = v;
        this.regionSize = regionSize;
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.tooltip = tooltip;
    }

    //text constructor
    public Button(int x, int y, int width, int height, Component text, Component tooltip, OnPress pressAction) {
        this(x, y, width, height, null, null, null, null, null, null, text, tooltip, pressAction);
    }

    //texture constructor
    public Button(int x, int y, int width, int height, int u, int v, int regionSize, ResourceLocation texture, int textureWidth, int textureHeight, Component tooltip, OnPress pressAction) {
        this(x, y, width, height, u, v, regionSize, texture, textureWidth, textureHeight, Component.empty(), tooltip, pressAction);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!this.isVisible())
            return;

        //update hovered
        this.setHovered(this.isMouseOver(mouseX, mouseY));

         //render button
        this.renderWidget(stack, mouseX, mouseY, delta);
    }

    @Override
    public void renderWidget(PoseStack stack, int mouseX, int mouseY, float delta) {
        //render texture
        if (this.texture != null) {
            renderTexture(stack, delta);
        } else {
            renderDefaultTexture(stack, delta);
        }

        //render text
        renderText(stack, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.isHoveredOrFocused() && this.isMouseOver(mouseX, mouseY) && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        boolean over = UIHelper.isMouseOver(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY);
        if (over && this.tooltip != null)
            UIHelper.setTooltip(this.tooltip);
        return over;
    }

    protected void renderDefaultTexture(PoseStack stack, float delta) {
        UIHelper.renderSliced(stack, getX(), getY(), getWidth(), getHeight(), getU() * 16f, getV() * 16f, 16, 16, 48, 32, TEXTURE);
    }

    protected void renderTexture(PoseStack stack, float delta) {
        //uv transforms
        int u = this.u + this.getU() * this.regionSize;
        int v = this.v + this.getV() * this.regionSize;

        //draw texture
        UIHelper.setupTexture(this.texture);

        int size = this.regionSize;
        blit(stack, this.getX() + this.getWidth() / 2 - size / 2, this.getY() + this.getHeight() / 2 - size / 2, u, v, size, size, this.textureWidth, this.textureHeight);
    }

    protected void renderText(PoseStack stack, float delta) {
        UIHelper.renderCenteredScrollingText(stack, getMessage(), getX() + 1, getY(), getWidth() - 2, getHeight(), getTextColor());
    }

    protected void renderVanillaBackground(PoseStack stack, int mouseX, int mouseY, float delta) {
        Component message = getMessage();
        setMessage(Component.empty());
        super.renderWidget(stack, mouseX, mouseY, delta);
        setMessage(message);
    }

    protected int getU() {
        if (!this.isActive())
            return 0;
        else if (this.isHoveredOrFocused())
            return 2;
        else
            return 1;
    }

    protected int getV() {
        return hasBackground ? 0 : 1;
    }

    protected int getTextColor() {
        return (!this.isActive() ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE).getColor();
    }

    public void setTooltip(Component tooltip) {
        this.tooltip = tooltip;
    }

    public Component getTooltip() {
        return tooltip;
    }

    public void shouldHaveBackground(boolean bool) {
        this.hasBackground = bool;
    }

    public void setHovered(boolean hovered) {
        this.isHovered = hovered;
    }

    public void run() {
        playDownSound(Minecraft.getInstance().getSoundManager());
        onPress();
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
}
