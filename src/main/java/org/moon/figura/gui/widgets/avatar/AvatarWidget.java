package org.moon.figura.gui.widgets.avatar;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.gui.widgets.TexturedButton;
import org.moon.figura.gui.widgets.lists.AvatarList;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.nio.file.Path;

public class AvatarWidget extends AbstractAvatarWidget {

    private final TexturedButton button;

    public AvatarWidget(int depth, int width, Path path, AvatarList parent) {
        super(depth, width, path, parent);

        AvatarWidget instance = this;
        this.button = new TexturedButton(x, y, width, 20, Component.literal("  ".repeat(depth)).append(getName()), null, button -> {
            AvatarManager.loadLocalAvatar(path);
            AvatarList.selectedEntry = instance;
        }) {
            @Override
            public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
                super.renderButton(stack, mouseX, mouseY, delta);

                //selected border
                if (AvatarList.selectedEntry != null && AvatarList.selectedEntry.path != null && AvatarList.selectedEntry.path.equals(instance.path))
                    UIHelper.fillOutline(stack, x - 1, y - 1, width + 2, height + 2, 0xFFFFFFFF);
            }

            @Override
            protected void renderText(PoseStack stack) {
                //variables
                Font font = Minecraft.getInstance().font;
                Component message = TextUtils.trimToWidthEllipsis(font, getMessage(), this.width - 6);

                //draw text
                font.drawShadow(
                        stack, message,
                        this.x + 3, this.y + this.height / 2f - font.lineHeight / 2f,
                        (!this.active ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE).getColor()
                );

                //tooltip
                if (message != getMessage())
                    setTooltip(instance.getName());
            }

            @Override
            public boolean isMouseOver(double mouseX, double mouseY) {
                return parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
            }
        };

        this.button.shouldHaveBackground(false);
        children.add(this.button);
    }

    @Override
    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;

        this.button.x = x;
        this.button.y = y;
    }
}
