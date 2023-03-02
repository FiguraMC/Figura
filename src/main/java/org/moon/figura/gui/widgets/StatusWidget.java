package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.backend2.NetworkStuff;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.MathUtils;
import org.moon.figura.utils.ui.UIHelper;

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

    public int x, y;
    public int width, height;
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
        if (!visible) return;

        //update status indicators
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
        disconnectedReason = backend == 1 && dc != null && !dc.isBlank() ? new TextComponent(dc) : null;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        //background
        if (background)
            UIHelper.renderSliced(stack, x, y, width, height, UIHelper.OUTLINE_FILL);

        //hover
        boolean hovered = this.isMouseOver(mouseX, mouseY);

        //text and tooltip
        double spacing = (double) width / count;
        double hSpacing = spacing * 0.5;
        for (int i = 0; i < count; i++) {
            int x = (int) (this.x + spacing * i + hSpacing);

            Component text = getStatusIcon(i);
            UIHelper.drawString(stack, font, text, x - font.width(text) / 2, y + (background ? 3 : 0), 0xFFFFFF);

            if (hovered && mouseX >= x - hSpacing && mouseX < x + hSpacing && mouseY >= y && mouseY < y + font.lineHeight + (background ? 3 : 0))
                UIHelper.setTooltip(getTooltipFor(i));
        }
    }

    public MutableComponent getStatusIcon(int type) {
        return new TextComponent(String.valueOf(STATUS_INDICATORS.charAt(status >> (type * 2) & 3))).setStyle(Style.EMPTY.withFont(UIHelper.UI_FONT));
    }

    public Component getTooltipFor(int i) {
        //get name and color
        int color = status >> (i * 2) & 3;
        String part = "gui.status." + STATUS_NAMES.get(i);

        MutableComponent info;
        if (i == 0) {
            double size = color == 1 ? NetworkStuff.getSizeLimit() : NetworkStuff.getSizeLimit() * 0.75;
            info = new FiguraText(part + "." + color, MathUtils.asFileSize(size));
        } else {
            info = new FiguraText(part + "." + color);
        }

        MutableComponent text = new FiguraText(part).append("\n• ").append(info).setStyle(TEXT_COLORS.get(color));

        //script error
        if (i == 2 && color == 1 && scriptError != null)
            text.append("\n\n").append(new FiguraText("gui.status.reason")).append("\n• ").append(scriptError);

        //get backend disconnect reason
        if (i == 3 && disconnectedReason != null)
            text.append("\n\n").append(new FiguraText("gui.status.reason")).append("\n• ").append(disconnectedReason);

        return text;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return UIHelper.isMouseOver(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setBackground(boolean background) {
        this.background = background;
    }
}
