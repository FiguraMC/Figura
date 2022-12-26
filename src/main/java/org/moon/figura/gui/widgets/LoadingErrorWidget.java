package org.moon.figura.gui.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.local.LocalAvatarLoader;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.ui.UIHelper;

public class LoadingErrorWidget extends StatusWidget {

    private static final MutableComponent ICON = Component.literal("=").withStyle(Style.EMPTY.withFont(UIHelper.UI_FONT).withColor(ChatFormatting.WHITE));

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
        return string == null ? Component.empty() : FiguraText.of("gui.status.load_error").withStyle(ChatFormatting.RED)
                .append("\n\n")
                .append(FiguraText.of("gui.status.reason"))
                .append("\n• ")
                .append(FiguraText.of("gui.load_error." + LocalAvatarLoader.getLoadState()))
                .append("\n• ")
                .append(Component.literal(string));
    }
}
