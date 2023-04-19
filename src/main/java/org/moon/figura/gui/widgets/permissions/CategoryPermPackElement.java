package org.moon.figura.gui.widgets.permissions;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.gui.widgets.lists.PlayerList;
import org.moon.figura.permissions.PermissionPack;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.ui.UIHelper;

public class CategoryPermPackElement extends AbstractPermPackElement {

    private static final ResourceLocation BACKGROUND = new FiguraIdentifier("textures/gui/group_permissions.png");
    private boolean enabled;

    public CategoryPermPackElement(int width, PermissionPack pack, PlayerList parent) {
        super(width, 20, pack, parent);
        this.enabled = pack.isVisible();
    }

    @Override
    public void renderWidget(PoseStack stack, int mouseX, int mouseY, float delta) {
        int width = getWidth();
        int height = getHeight();

        stack.pushPose();
        stack.translate(getX() + width / 2f, getY() + height / 2f, 100);
        stack.scale(scale, scale, 1f);

        animate(delta, this.isMouseOver(mouseX, mouseY) || this.isFocused());

        //fix x, y
        int x = -width / 2;
        int y = -height / 2;

        //selected overlay
        if (this.parent.selectedEntry == this) {
            UIHelper.fillRounded(stack, x - 1, y - 1, width + 2, height + 2, 0xFFFFFFFF);
        }

        //background
        UIHelper.renderHalfTexture(stack, x, y, width, height, 0f, enabled ? 20f : 0f, 174, 20, 174, 40, BACKGROUND);

        //name
        Component text = pack.getCategoryName().append(pack.hasChanges() ? "*" : "");
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
            pack.setVisible(enabled);

            parent.updateScroll();
        }

        super.onPress();
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}
