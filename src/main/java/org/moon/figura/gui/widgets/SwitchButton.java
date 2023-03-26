package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.ui.UIHelper;

public class SwitchButton extends Button {

    public static final ResourceLocation SWITCH_TEXTURE = new FiguraIdentifier("textures/gui/switch.png");
    public static final Component ON = FiguraText.of("gui.on");
    public static final Component OFF = FiguraText.of("gui.off");

    protected boolean toggled = false;
    private boolean defaultTexture = false;
    private boolean underline = true;
    private float headPos = 0f;

    //text constructor
    public SwitchButton(int x, int y, int width, int height, Component text, Component tooltip, OnPress pressAction) {
        super(x, y, width, height, text, tooltip, pressAction);
    }

    //texture constructor
    public SwitchButton(int x, int y, int width, int height, int u, int v, int interactionOffset, ResourceLocation texture, int textureWidth, int textureHeight, Component tooltip, OnPress pressAction) {
        super(x, y, width, height, u, v, interactionOffset, texture, textureWidth, textureHeight, tooltip, pressAction);
    }

    //default texture constructor
    public SwitchButton(int x, int y, int width, int height, Component text, boolean toggled) {
        super(x, y, width, height, text, null, button -> {});
        this.toggled = toggled;
        this.headPos = toggled ? 20f : 0f;
        defaultTexture = true;
    }

    @Override
    public void onPress() {
        this.toggled = !this.toggled;
        super.onPress();
    }

    @Override
    protected void renderText(PoseStack stack, float delta) {
        //draw text
        Component text = this.toggled && underline ? getMessage().copy().withStyle(ChatFormatting.UNDERLINE) : getMessage();
        int x = getX() + 1;
        int width = getWidth() - 2;

        if (defaultTexture) {
            x += 31;
            width -= 31;
        }

        UIHelper.renderCenteredScrollingText(stack, text, x, getY(), width, getHeight(), getTextColor());
    }

    @Override
    protected void renderDefaultTexture(PoseStack stack, float delta) {
        if (!defaultTexture) {
            super.renderDefaultTexture(stack, delta);
            return;
        }

        //set texture
        UIHelper.setupTexture(SWITCH_TEXTURE);
        int x = getX();
        int y = getY();

        //render switch
        blit(stack, x + 5, y + 5, 20, 10, 10f, (this.toggled ? 20f : 0f) + (this.isHoveredOrFocused() ? 10f : 0f), 20, 10, 30, 40);

        //render head
        headPos = (float) Mth.lerp(1f - Math.pow(0.2f, delta), headPos, this.toggled ? 20f : 0f);
        blit(stack, Math.round(x + headPos), y, 10, 20, 0f, this.isHoveredOrFocused() ? 20f : 0f, 10, 20, 30, 40);
    }

    public boolean isToggled() {
        return this.toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public void setUnderline(boolean underline) {
        this.underline = underline;
    }
}
