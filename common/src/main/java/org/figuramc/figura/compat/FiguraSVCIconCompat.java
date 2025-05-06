package org.figuramc.figura.compat;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;

import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.GuiGraphics;

public class FiguraSVCIconCompat {

    private static boolean passed = false;

    public static boolean shouldRenderVanillaIcon(GuiGraphics guiGraphics, PlayerState state, int x, int y, int size, float scale) {
        // So that it doesnt render the figura portrait twice
        if (passed) {
            passed = false;
            return false;
        }

        Avatar avatar = AvatarManager.getAvatarForPlayer(state.getUuid());
        if ((avatar != null && avatar.renderPortrait(guiGraphics, x, y, size, scale, false))) {
            passed = true;
            return false;
        }
        return true;
    }
}
