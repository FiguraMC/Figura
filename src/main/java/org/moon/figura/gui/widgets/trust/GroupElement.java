package org.moon.figura.gui.widgets.trust;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.gui.widgets.lists.PlayerList;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.ui.UIHelper;

public class GroupElement extends AbstractTrustElement {

    private static final ResourceLocation BACKGROUND = new FiguraIdentifier("textures/gui/group_trust.png");
    private boolean enabled;

    public GroupElement(TrustContainer container, PlayerList parent) {
        super(20, container, parent);
        this.enabled = container.visible;
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
        stack.pushPose();
        stack.translate(x + width / 2f, y + height / 2f, 100);
        stack.scale(scale, scale, scale);

        animate(delta, this.isMouseOver(mouseX, mouseY) || this.isFocused());

        //fix x, y
        int x = -width / 2;
        int y = -height / 2;

        //selected overlay
        if (this.parent.selectedEntry == this) {
            UIHelper.fillRounded(stack, x - 1, y - 1, width + 2, height + 2, 0xFFFFFFFF);
        }

        //background
        UIHelper.setupTexture(BACKGROUND);
        blit(stack, x, y, width, height, 0f, enabled ? 20f : 0f, 174, 20, 174, 40);

        //name
        Component text = trust.getGroupName();
        Font font = Minecraft.getInstance().font;
        UIHelper.renderOutlineText(stack, font, text, x + width / 2 - font.width(text) / 2, y + height / 2 - font.lineHeight / 2, 0xFFFFFF, 0);

        stack.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.isMouseOver(mouseX, mouseY) && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onPress() {
        if (parent.selectedEntry == this) {
            enabled = !enabled;
            trust.visible = enabled;

            parent.updateScroll();
        }

        super.onPress();
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}
