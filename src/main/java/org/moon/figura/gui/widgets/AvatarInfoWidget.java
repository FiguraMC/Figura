package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.MathUtils;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AvatarInfoWidget implements FiguraWidget, FiguraTickable, GuiEventListener {

    private static final MutableComponent UNKNOWN = Component.literal("?").setStyle(ColorUtils.Colors.FRAN_PINK.style);
    private static final MutableComponent ELLIPSIS = TextUtils.ELLIPSIS.copy().setStyle(ColorUtils.Colors.FRAN_PINK.style);
    private static final List<Component> TITLES = List.of(
            FiguraText.of("gui.name").withStyle(ChatFormatting.UNDERLINE),
            FiguraText.of("gui.authors").withStyle(ChatFormatting.UNDERLINE),
            FiguraText.of("gui.size").withStyle(ChatFormatting.UNDERLINE),
            FiguraText.of("gui.complexity").withStyle(ChatFormatting.UNDERLINE)
    );

    public int x, y;
    public int width, height;
    private boolean visible = true;
    private final int maxSize;

    private final Font font;
    private final List<Component> values = Arrays.asList(new Component[TITLES.size()]);

    public AvatarInfoWidget(int x, int y, int width, int maxSize) {
        this.x = x;
        this.y = y;
        this.font = Minecraft.getInstance().font;

        this.width = width;
        this.height = (font.lineHeight + 4) * TITLES.size() * 2 + 4; //font + spacing + border
        this.maxSize = maxSize;
    }

    @Override
    public void tick() {
        if (!visible) return;

        Style accent = FiguraMod.getAccentColor();
        ELLIPSIS.setStyle(accent);
        UNKNOWN.setStyle(accent);

        //update values
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null && avatar.nbt != null) {
            values.set(0, Component.literal(avatar.name).setStyle(accent)); //name
            values.set(1, avatar.authors.isBlank() ? UNKNOWN : Component.literal(avatar.authors).setStyle(accent)); //authors
            values.set(2, Component.literal(MathUtils.asFileSize(avatar.fileSize)).setStyle(accent)); //size
            values.set(3, Component.literal(String.valueOf(avatar.complexity.pre)).setStyle(accent)); //complexity
        } else {
            for (int i = 0; i < TITLES.size(); i++) {
                values.set(i, UNKNOWN);
            }
        }
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        //prepare vars
        int x = this.x + width / 2;
        int y = this.y + 4;
        int height = font.lineHeight + 4;
        int maxLines = (maxSize - 8) / height;

        //special author stuff
        int authorFreeLines = maxLines - 9;
        Component authors = values.get(1);
        List<FormattedCharSequence> authorLines = authors == null ? Collections.emptyList() : TextUtils.warpText(authors, width - 10, font);
        int authorUsedLines = Math.min(authorLines.size(), authorFreeLines);
        this.height = height * TITLES.size() * 2 + 4 + height * (Math.max(authorUsedLines - 1, 0));

        //render background
        UIHelper.renderSliced(stack, this.x, this.y, this.width, this.height, UIHelper.OUTLINE);

        //render texts
        for (int i = 0; i < TITLES.size(); i++) {
            // -- title -- //

            Component title = TITLES.get(i);
            if (title != null)
                UIHelper.drawCenteredString(stack, font, title, x, y, 0xFFFFFF);
            y += height;

            // -- value -- //

            Component value = values.get(i);
            if (value == null) {
                y += height;
                continue;
            }

            //default rendering
            if (i != 1) {
                Component toRender = TextUtils.trimToWidthEllipsis(font, value, width - 10, ELLIPSIS);
                UIHelper.drawCenteredString(stack, font, toRender, x, y, 0xFFFFFF);

                //tooltip
                if (value != toRender && UIHelper.isMouseOver(this.x, y - height, width, height * 2 - 4, mouseX, mouseY))
                    UIHelper.setTooltip(value);

                y += height;
                continue;
            }

            //author special rendering
            for (int j = 0; j < authorUsedLines; j++) {
                FormattedCharSequence text = authorLines.get(j);

                //add ellipsis and tooltip if it is the last entry and out lines exceeded the free space
                if (j == authorUsedLines - 1 && authorLines.size() > authorFreeLines) {
                    text = TextUtils.addEllipsis(font, TextUtils.charSequenceToText(text), width - 10, ELLIPSIS).getVisualOrderText();

                    if (UIHelper.isMouseOver(this.x, y - height * authorUsedLines, width, height * (authorUsedLines + 1) - 4, mouseX, mouseY))
                        UIHelper.setTooltip(value);
                }

                UIHelper.drawCenteredString(stack, font, text, x, y, 0xFFFFFF);
                y += height;
            }
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
