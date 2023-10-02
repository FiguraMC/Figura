package org.figuramc.figura.gui.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.MathUtils;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AvatarInfoWidget implements FiguraWidget, FiguraTickable, GuiEventListener {

    private static final MutableComponent UNKNOWN = Component.literal("?").setStyle(ColorUtils.Colors.AWESOME_BLUE.style);
    private static final MutableComponent ELLIPSIS = TextUtils.ELLIPSIS.copy().setStyle(ColorUtils.Colors.AWESOME_BLUE.style);
    private static final List<Component> TITLES = List.of(
            FiguraText.of("gui.name").withStyle(ChatFormatting.UNDERLINE),
            FiguraText.of("gui.authors").withStyle(ChatFormatting.UNDERLINE),
            FiguraText.of("gui.size").withStyle(ChatFormatting.UNDERLINE),
            FiguraText.of("gui.complexity").withStyle(ChatFormatting.UNDERLINE)
    );

    private int x, y;
    private int width, height;
    private boolean visible = true;
    private final int maxSize;

    private final Font font;
    private final List<Component> values = new ArrayList<>() {{
        for (Component ignored : TITLES)
            this.add(UNKNOWN);
    }};

    public AvatarInfoWidget(int x, int y, int width, int maxSize) {
        this.x = x;
        this.y = y;
        this.font = Minecraft.getInstance().font;

        this.width = width;
        this.height = (font.lineHeight + 4) * TITLES.size() * 2 + 4; // font + spacing + border
        this.maxSize = maxSize;
    }

    @Override
    public void tick() {
        if (!visible) return;

        Style accent = FiguraMod.getAccentColor();
        ELLIPSIS.setStyle(accent);
        UNKNOWN.setStyle(accent);

        // update values
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null && avatar.nbt != null) {
            values.set(0, avatar.name == null || avatar.name.isBlank() ? UNKNOWN : Emojis.applyEmojis(Component.literal(avatar.name).setStyle(accent))); // name
            values.set(1, avatar.authors == null || avatar.authors.isBlank() ? UNKNOWN : Emojis.applyEmojis(Component.literal(avatar.authors).setStyle(accent))); // authors
            values.set(2, Component.literal(MathUtils.asFileSize(avatar.fileSize)).setStyle(accent)); // size
            values.set(3, Component.literal(String.valueOf(avatar.complexity.pre)).setStyle(accent)); // complexity
        } else {
            for (int i = 0; i < TITLES.size(); i++) {
                values.set(i, UNKNOWN);
            }
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        // prepare vars
        int x = this.x + width / 2;
        int y = this.y + 4;
        int height = font.lineHeight + 4;
        int maxLines = (maxSize - 8) / height;

        // special author stuff
        int authorFreeLines = maxLines - 7;
        Component authors = values.get(1);
        List<Component> authorLines = authors == null ? Collections.emptyList() : TextUtils.splitText(authors, "\n");
        int authorUsedLines = Math.min(authorLines.size(), authorFreeLines);

        // set new widget height
        int newHeight = height * TITLES.size() * 2 + 4 + height * (authorUsedLines - 1);
        this.height = Math.min(newHeight + height, maxSize);
        y += (this.height - newHeight) / 2;

        // render background
        UIHelper.blitSliced(gui, this.x, this.y, this.width, this.height, UIHelper.OUTLINE_FILL);

        // render texts
        for (int i = 0; i < TITLES.size(); i++) {
            // -- title -- // 

            Component title = TITLES.get(i);
            if (title != null)
                gui.drawCenteredString(font, title, x, y, 0xFFFFFF);
            y += height;

            // -- value -- // 

            Component value = values.get(i);
            if (value == null) {
                y += height;
                continue;
            }

            // default rendering
            if (i != 1) {
                Component toRender = TextUtils.trimToWidthEllipsis(font, value, width - 10, ELLIPSIS);
                gui.drawCenteredString(font, toRender, x, y, 0xFFFFFF);

                // tooltip
                if (value != toRender && UIHelper.isMouseOver(this.x, y - height, width, height * 2 - 4, mouseX, mouseY))
                    UIHelper.setTooltip(value);

                y += height;
                continue;
            }

            // author special rendering
            for (int j = 0; j < authorUsedLines; j++) {
                Component text = authorLines.get(j);
                Component newText = TextUtils.trimToWidthEllipsis(font, text, width - 10, ELLIPSIS);

                if (j == authorUsedLines - 1 && authorLines.size() > authorUsedLines) {
                    text = value;
                    newText = ELLIPSIS;
                }

                if (text != newText && UIHelper.isMouseOver(this.x, y, width, height, mouseX, mouseY))
                    UIHelper.setTooltip(text);

                gui.drawCenteredString(font, newText, x, y, 0xFFFFFF);
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

    @Override
    public void setFocused(boolean bl) {}

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }
}
