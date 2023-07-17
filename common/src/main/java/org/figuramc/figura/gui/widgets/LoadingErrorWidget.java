package org.figuramc.figura.gui.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.local.LocalAvatarLoader;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.ui.UIHelper;

public class LoadingErrorWidget extends StatusWidget {

    private static final MutableComponent ICON = new TextComponent("=").withStyle(Style.EMPTY.withFont(UIHelper.UI_FONT).withColor(ChatFormatting.WHITE));

    private String string;

    public LoadingErrorWidget(int x, int y, int width) {
        super(x, y, width, 1);
    }

    @Override
    public void tick() {
        string = LocalAvatarLoader.getLoadError();
        this.setVisible(!AvatarManager.localUploaded && string != null);
    }

    @Override
    public MutableComponent getStatusIcon(int type) {
        return ICON;
    }

    @Override
    public Component getTooltipFor(int i) {
        return string == null ? TextComponent.EMPTY.copy() : new FiguraText("gui.load_error").withStyle(ChatFormatting.RED)
                .append("\n\n")
                .append(new FiguraText("gui.status.reason"))
                .append("\n• ")
                .append(new FiguraText("gui.load_error." + LocalAvatarLoader.getLoadState()))
                .append("\n• ")
                .append(new TextComponent(string));
    }
}
