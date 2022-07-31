package org.moon.figura.mixin.gui;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.config.Config;
import org.moon.figura.lua.api.nameplate.NameplateCustomization;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @Inject(at = @At("RETURN"), method = "getNameForDisplay", cancellable = true)
    private void getPlayerName(PlayerInfo playerInfo, CallbackInfoReturnable<Component> cir) {
        //get config
        int config = (int) Config.LIST_NAMEPLATE.value;
        if (config == 0)
            return;

        //get data
        UUID uuid = playerInfo.getProfile().getId();
        Avatar avatar = AvatarManager.getAvatarForPlayer(uuid);
        if (avatar == null)
            return;

        //apply customization
        Component text = cir.getReturnValue();
        Component replacement;

        NameplateCustomization custom = avatar.luaRuntime == null ? null : avatar.luaRuntime.nameplate.LIST;
        if (custom != null && custom.getText() != null && avatar.trust.get(TrustContainer.Trust.NAMEPLATE_EDIT) == 1) {
            replacement = NameplateCustomization.applyCustomization(custom.getText());
        } else {
            replacement = Component.literal(playerInfo.getProfile().getName());
        }

        if (config > 1) {
            Component badges = NameplateCustomization.fetchBadges(avatar);
            ((MutableComponent) replacement).append(badges);
        }

        text = TextUtils.replaceInText(text, "\\b" + playerInfo.getProfile().getName() + "\\b", replacement);

        cir.setReturnValue(text);
    }
}
