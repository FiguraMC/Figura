package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.ui.UIHelper;

public class TexturedButton extends Button {

    //default textures
    private static final ResourceLocation TEXTURE = new FiguraIdentifier("textures/gui/button.png");

    //texture data
    protected Integer u;
    protected Integer v;

    protected final Integer textureWidth;
    protected final Integer textureHeight;
    protected final Integer interactionOffset;
    protected final ResourceLocation texture;

    //extra fields
    protected Component tooltip;
    private boolean hasBackground = true;

    //texture and text constructor
    public TexturedButton(int x, int y, int width, int height, Integer u, Integer v, Integer interactionOffset, ResourceLocation texture, Integer textureWidth, Integer textureHeight, Component text, Component tooltip, Button.OnPress pressAction) {
        super(x, y, width, height, text, pressAction);

        this.u = u;
        this.v = v;
        this.interactionOffset = interactionOffset;
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.tooltip = tooltip;
    }

    //text constructor
    public TexturedButton(int x, int y, int width, int height, Component text, Component tooltip, Button.OnPress pressAction) {
        this(x, y, width, height, null, null, null, null, null, null, text, tooltip, pressAction);
    }

    //texture constructor
    public TexturedButton(int x, int y, int width, int height, int u, int v, int interactionOffset, ResourceLocation texture, int textureWidth, int textureHeight, Component tooltip, Button.OnPress pressAction) {
        this(x, y, width, height, u, v, interactionOffset, texture, textureWidth, textureHeight, null, tooltip, pressAction);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!this.visible)
            return;

         //render button
        this.renderButton(stack, mouseX, mouseY, delta);

        //update hovered
        this.setHovered(this.isMouseOver(mouseX, mouseY));
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
        //render texture
        if (this.texture != null) {
            renderTexture(stack, delta);
        } else {
            float u;

            if (!this.active)
                u = 0f;
            else if (this.isHoveredOrFocused())
                u = 32f;
            else
                u = 16f;

            UIHelper.renderSliced(stack, x, y, width, height, u, this.hasBackground ? 0f : 16f, 16, 16, 48, 32, TEXTURE);
        }

        //render text
        if (this.getMessage() != null)
            renderText(stack);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.isHoveredOrFocused() && this.isMouseOver(mouseX, mouseY) && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        boolean over = UIHelper.isMouseOver(x, y, width, height, mouseX, mouseY);
        if (over) UIHelper.setTooltip(this.tooltip);
        return over;
    }

    protected void renderTexture(PoseStack stack, float delta) {
        //uv transforms
        int u = this.u;
        int v = this.v;
        if (this.isHoveredOrFocused())
            v += this.interactionOffset;
        if (!this.active)
            u -= this.interactionOffset;

        //draw texture
        UIHelper.setupTexture(this.texture);

        int size = this.interactionOffset;
        blit(stack, this.x + this.width / 2 - size / 2, this.y + this.height / 2 - size / 2, u, v, size, size, this.textureWidth, this.textureHeight);
    }

    protected void renderText(PoseStack stack) {
        //draw text
        Font font = Minecraft.getInstance().font;
        drawCenteredString(
                stack, font,
                this.getMessage(),
                this.x + this.width / 2, this.y + this.height / 2 - font.lineHeight / 2,
                (!this.active ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE).getColor()
        );
    }

    public void setUV(int x, int y) {
        this.u = x;
        this.v = y;
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
}
