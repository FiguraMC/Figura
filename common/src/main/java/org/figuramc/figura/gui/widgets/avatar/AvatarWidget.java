package org.figuramc.figura.gui.widgets.avatar;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.local.LocalAvatarFetcher;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.lists.AvatarList;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FileTexture;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class AvatarWidget extends AbstractAvatarWidget {

    public static final ResourceLocation MISSING_ICON = new FiguraIdentifier("textures/gui/unknown_icon.png");

    public AvatarWidget(int depth, int width, LocalAvatarFetcher.AvatarPath avatar, AvatarList parent) {
        super(depth, width, 24, avatar, parent);

        AvatarWidget instance = this;
        Component description = Emojis.applyEmojis(Component.literal(avatar.getDescription()));
        this.button = new Button(getX(), getY(), width, 24, getName(), null, button -> {
            AvatarManager.loadLocalAvatar(avatar.getPath());
            AvatarList.selectedEntry = avatar.getTheActualPathForThis();
        }) {
            @Override
            public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float delta) {
                super.renderWidget(gui, mouseX, mouseY, delta);

                // selected border
                if (instance.isOf(AvatarList.selectedEntry))
                    UIHelper.fillOutline(gui, getX(), getY(), getWidth(), getHeight(), 0xFFFFFFFF);
            }

            @Override
            protected void renderText(GuiGraphics gui, float delta) {
                // variables
                Font font = Minecraft.getInstance().font;

                int space = SPACING * depth;
                int width = this.getWidth() - 26 - space;
                int x = getX() + 2 + space;
                int y = getY() + 2;

                // icon
                FileTexture texture = avatar.getIcon();
                ResourceLocation icon = texture == null ? MISSING_ICON : texture.getLocation();
                UIHelper.blit(gui, x, y, 20, 20, icon);

                // name
                Component parsedName = TextUtils.trimToWidthEllipsis(font, getMessage(), width, TextUtils.ELLIPSIS.copy().withStyle(getMessage().getStyle()));
                gui.drawString(font, parsedName, x + 22, y, -1);

                // description
                Component parsedDescription = TextUtils.trimToWidthEllipsis(font, description, width, TextUtils.ELLIPSIS.copy().withStyle(description.getStyle()));
                gui.drawString(font, parsedDescription, x + 22, y + font.lineHeight + 1, ChatFormatting.GRAY.getColor());

                // tooltip
                if (parsedName != getMessage() || parsedDescription != description) {
                    Component tooltip = instance.getName();
                    if (!description.getString().isBlank())
                        tooltip = tooltip.copy().append("\n\n").append(description);
                    setTooltip(tooltip);
                }
            }

            @Override
            public boolean isMouseOver(double mouseX, double mouseY) {
                return parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
            }

            @Override
            public void setHovered(boolean hovered) {
                if (!hovered && UIHelper.getContext() == context && context.isVisible())
                    hovered = true;

                super.setHovered(hovered);
            }
        };

        this.button.shouldHaveBackground(false);
        children.add(this.button);
    }
}
