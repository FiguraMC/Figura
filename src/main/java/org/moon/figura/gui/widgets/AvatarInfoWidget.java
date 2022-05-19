package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.util.Arrays;
import java.util.List;

public class AvatarInfoWidget implements FiguraWidget, FiguraTickable, GuiEventListener {

    public static final Component UNKNOWN = new TextComponent("?").setStyle(ColorUtils.Colors.FRAN_PINK.style);
    public static final List<Component> TITLES = List.of(
            new FiguraText("gui.name").withStyle(ChatFormatting.UNDERLINE),
            new FiguraText("gui.authors").withStyle(ChatFormatting.UNDERLINE),
            new FiguraText("gui.size").withStyle(ChatFormatting.UNDERLINE),
            new FiguraText("gui.complexity").withStyle(ChatFormatting.UNDERLINE)
    );

    public int x, y;
    public int width, height;
    private boolean visible = true;

    private final Font font;
    private final List<Component> values = Arrays.asList(new Component[TITLES.size()]);

    public AvatarInfoWidget(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.font = Minecraft.getInstance().font;

        this.width = width;
        this.height = (font.lineHeight + 4) * TITLES.size() * 2 + 4; //font + spacing + border
    }

    @Override
    public void tick() {
        if (!visible) return;

        //update values
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar == null) {
            for (int i = 0; i < TITLES.size(); i++) {
                values.set(i, UNKNOWN);
            }
        } else {
            values.set(0, new TextComponent(avatar.name).setStyle(ColorUtils.Colors.FRAN_PINK.style)); //name
            values.set(1, new TextComponent(avatar.authors).setStyle(ColorUtils.Colors.FRAN_PINK.style)); //authors
            values.set(2, new TextComponent(String.valueOf(avatar.fileSize)).setStyle(ColorUtils.Colors.FRAN_PINK.style)); //size
            values.set(3, new TextComponent(String.valueOf(avatar.complexity)).setStyle(ColorUtils.Colors.FRAN_PINK.style)); //complexity
        }
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        //render background
        UIHelper.renderSliced(stack, x, y, width, height, UIHelper.OUTLINE);

        //prepare vars
        int x = this.x + width / 2;
        int y = this.y + 4;
        int height = font.lineHeight + 4;

        //render texts
        for (int i = 0; i < TITLES.size(); i++) {
            //title
            Component title = TITLES.get(i);
            if (title != null)
                UIHelper.drawCenteredString(stack, font, title, x, y, 0xFFFFFF);
            y += height;

            //value
            Component value = values.get(i);
            if (value != null) {
                Component toRender = TextUtils.trimToWidthEllipsis(font, value, width - 10);
                UIHelper.drawCenteredString(stack, font, toRender, x, y, 0xFFFFFF);

                //tooltip
                if (value != toRender && UIHelper.isMouseOver(this.x, y - height, width, height * 2 - 4, mouseX, mouseY))
                    UIHelper.setTooltip(value);
            }
            y += height;
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
