package org.moon.figura.gui.widgets.avatar;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.local.LocalAvatarFetcher;
import org.moon.figura.gui.widgets.TexturedButton;
import org.moon.figura.gui.widgets.lists.AvatarList;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

public class AvatarWidget extends AbstractAvatarWidget {

    public AvatarWidget(int depth, int width, LocalAvatarFetcher.AvatarPath avatar, AvatarList parent) {
        super(depth, width, avatar, parent);

        AvatarWidget instance = this;
        this.button = new TexturedButton(x, y, width, 20, Component.literal("  ".repeat(depth)).append(getName()), null, button -> {
            AvatarManager.loadLocalAvatar(avatar == null ? null : avatar.getPath());
            AvatarList.selectedEntry = instance;
        }) {
            @Override
            public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
                super.renderButton(stack, mouseX, mouseY, delta);

                //selected border
                if (instance.equals(AvatarList.selectedEntry))
                    UIHelper.fillOutline(stack, x - 1, y - 1, width + 2, height + 2, 0xFFFFFFFF);
            }

            @Override
            protected void renderText(PoseStack stack) {
                //variables
                Font font = Minecraft.getInstance().font;
                Component message = TextUtils.trimToWidthEllipsis(font, getMessage(), this.width - 6, TextUtils.ELLIPSIS.copy().withStyle(getMessage().getStyle()));

                //draw text
                font.drawShadow(
                        stack, message,
                        this.x + 3, this.y + this.height / 2 - font.lineHeight / 2,
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
}
