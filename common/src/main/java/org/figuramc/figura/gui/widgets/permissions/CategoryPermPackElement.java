package org.figuramc.figura.gui.widgets.permissions;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.gui.widgets.lists.PlayerList;
import org.figuramc.figura.permissions.PermissionPack;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.ui.UIHelper;
import org.joml.Matrix3x2fStack;

public class CategoryPermPackElement extends AbstractPermPackElement {

    private static final Identifier BACKGROUND = new FiguraIdentifier("textures/gui/group_permissions.png");
    private boolean enabled;

    public CategoryPermPackElement(int width, PermissionPack pack, PlayerList parent) {
        super(width, 20, pack, parent);
        this.enabled = pack.isVisible();
    }

    @Override
    public void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        Matrix3x2fStack pose = gui.pose();
        int width = getWidth();
        int height = getHeight();

        pose.pushMatrix();
        pose.translate(getX() + width / 2f, getY() + height / 2f);
        pose.scale(scale, scale);

        animate(delta, this.isMouseOver(mouseX, mouseY) || this.isFocused());

        // fix x, y
        int x = -width / 2;
        int y = -height / 2;

        // selected overlay
        if (this.parent.selectedEntry == this) {
            UIHelper.fillRounded(gui, x - 1, y - 1, width + 2, height + 2, 0xFFFFFFFF);
        }

        // background
        UIHelper.renderHalfTexture(gui, x, y, width, height, 0f, enabled ? 20f : 0f, 174, 20, 174, 40, BACKGROUND);

        // name
        Component text = pack.getCategoryName().append(pack.hasChanges() ? "*" : "");
        Font font = Minecraft.getInstance().font;
        UIHelper.renderOutlineText(gui, font, text, x + width / 2 - font.width(text) / 2, y + height / 2 - font.lineHeight / 2, 0xFFFFFF, 0);

        pose.popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        return this.isMouseOver(mouseButtonEvent.x(), mouseButtonEvent.y()) && super.mouseClicked(mouseButtonEvent, bl);
    }

    @Override
    public void onPress(InputWithModifiers inputWithModifiers) {
        if (parent.selectedEntry == this) {
            enabled = !enabled;
            pack.setVisible(enabled);

            parent.updateScroll();
        }

        super.onPress(inputWithModifiers);
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}
