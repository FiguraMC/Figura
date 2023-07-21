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
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.MathUtils;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.List;

public class StatusWidget implements FiguraWidget, FiguraTickable, GuiEventListener {

    public static final String STATUS_INDICATORS = "-*/+";
    public static final List<String> STATUS_NAMES = List.of("size", "texture", "script", "backend");
    public static final List<Style> TEXT_COLORS = List.of(
            Style.EMPTY.withColor(ChatFormatting.WHITE),
            Style.EMPTY.withColor(ChatFormatting.RED),
            Style.EMPTY.withColor(ChatFormatting.YELLOW),
            Style.EMPTY.withColor(ChatFormatting.GREEN)
    );

    private final Font font;
    protected final int count;
    protected int status = 0;
    private Component scriptError, disconnectedReason;

    private int x, y;
    private int width, height;
    private boolean visible = true;
    private boolean background = true;

    public StatusWidget(int x, int y, int width) {
        this(x, y, width, STATUS_NAMES.size());
    }

    protected StatusWidget(int x, int y, int width, int count) {
        this.x = x;
        this.y = y;
        this.font = Minecraft.getInstance().font;
        this.width = width;
        this.height = font.lineHeight + 5;
        this.count = count;
    }

    @Override
    public void tick() {
        if (!isVisible()) return;

        // update status indicators
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        boolean empty = avatar == null || avatar.nbt == null;

        status = empty ? 0 : avatar.fileSize > NetworkStuff.getSizeLimit() ? 1 : avatar.fileSize > NetworkStuff.getSizeLimit() * 0.75 ? 2 : 3;

        int texture = empty || !avatar.hasTexture ? 0 : 3;
        status += texture << 2;

        int script = empty ? 0 : avatar.scriptError ? 1 : avatar.luaRuntime == null ? 0 : avatar.versionStatus > 0 ? 2 : 3;
        status += script << 4;
        scriptError = script == 1 ? avatar.errorText.copy() : null;

        int backend = NetworkStuff.backendStatus;
        status += backend << 6;

        String dc = NetworkStuff.disconnectedReason;
        disconnectedReason = backend == 1 && dc != null && !dc.isBlank() ? Component.literal(dc) : null;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        if (!isVisible()) return;

        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        boolean background = hasBackground();

        // background
        if (background)
            UIHelper.blitSliced(gui, x, y, width, height, UIHelper.OUTLINE_FILL);

        // hover
        boolean hovered = this.isMouseOver(mouseX, mouseY);

        // text and tooltip
        double spacing = (double) width / count;
        double hSpacing = spacing * 0.5;
        for (int i = 0; i < count; i++) {
            int xx = (int) (x + spacing * i + hSpacing);

            Component text = getStatusIcon(i);
            gui.drawString(font, text, xx - font.width(text) / 2, y + (background ? 3 : 0), 0xFFFFFF);

            if (hovered && mouseX >= xx - hSpacing && mouseX < xx + hSpacing && mouseY >= y && mouseY < y + font.lineHeight + (background ? 3 : 0))
                UIHelper.setTooltip(getTooltipFor(i));
        }
    }

    public MutableComponent getStatusIcon(int type) {
        return Component.literal(String.valueOf(STATUS_INDICATORS.charAt(status >> (type * 2) & 3))).setStyle(Style.EMPTY.withFont(UIHelper.UI_FONT));
    }

    public Component getTooltipFor(int i) {
        // get name and color
        int color = status >> (i * 2) & 3;
        String part = "gui.status." + STATUS_NAMES.get(i);

        MutableComponent info;
        if (i == 0) {
            double size = NetworkStuff.getSizeLimit();
            info = FiguraText.of(part + "." + color, MathUtils.asFileSize(size));
        } else {
            info = FiguraText.of(part + "." + color);
        }

        MutableComponent text = FiguraText.of(part).append("\n• ").append(info).setStyle(TEXT_COLORS.get(color));

        // script error
        if (i == 2 && color == 1 && scriptError != null)
            text.append("\n\n").append(FiguraText.of("gui.status.reason")).append("\n• ").append(scriptError);

        // get backend disconnect reason
        if (i == 3 && disconnectedReason != null)
            text.append("\n\n").append(FiguraText.of("gui.status.reason")).append("\n• ").append(disconnectedReason);

        return text;
    }

    public boolean hasBackground() {
        return this.background;
    }

    public void setBackground(boolean background) {
        this.background = background;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return UIHelper.isMouseOver(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY);
    }

    @Override
    public void setFocused(boolean bl) {}

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
