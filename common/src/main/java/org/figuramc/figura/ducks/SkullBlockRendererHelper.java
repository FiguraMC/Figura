package org.figuramc.figura.ducks;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.world.item.component.ResolvableProfile;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;

import java.util.UUID;

public class SkullBlockRendererHelper {
    private static Avatar avatar;
    public static void setAvatar(Avatar av) {
        avatar = av;
    }

    public static Avatar getAvatar() {
        return avatar;
    }

    public static Avatar resolveAvatar(ResolvableProfile profile) {
        if (profile == null)
            return null;

        UUID id = null;
        try {
            PlayerSkinRenderCache cache = Minecraft.getInstance().playerSkinRenderCache();
            PlayerSkinRenderCache.RenderInfo info = cache.getOrDefault(profile);
            GameProfile gp = info == null ? null : info.gameProfile();
            if (gp != null)
                id = gp.id();
        } catch (Exception ignored) {}

        if (id == null) {
            GameProfile partial = profile.partialProfile();
            if (partial != null)
                id = partial.id();
        }

        return id == null ? null : AvatarManager.getAvatarForPlayer(id);
    }
}
