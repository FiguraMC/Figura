package org.figuramc.figura.mixin.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.lua.api.nameplate.NameplateCustomization;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.regex.Pattern;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @Unique private UUID uuid;

    @Inject(at = @At("RETURN"), method = "getNameForDisplay", cancellable = true)
    private void getPlayerName(PlayerInfo playerInfo, CallbackInfoReturnable<Component> cir) {
        // get config
        int config = Configs.LIST_NAMEPLATE.value;
        if (config == 0 || AvatarManager.panic)
            return;

        // apply customization
        Component text = cir.getReturnValue();
        Component name = Component.literal(playerInfo.getProfile().getName());

        UUID uuid = playerInfo.getProfile().getId();
        Avatar avatar = AvatarManager.getAvatarForPlayer(uuid);
        NameplateCustomization custom = avatar == null || avatar.luaRuntime == null ? null : avatar.luaRuntime.nameplate.LIST;

        Component replacement = custom != null && custom.getJson() != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 1 ?
                TextUtils.replaceInText(custom.getJson().copy(), "\n|\\\\n", " ") : name;

        // name
        replacement = TextUtils.replaceInText(replacement, "\\$\\{name\\}", name);

        // badges
        replacement = Badges.appendBadges(replacement, uuid, config > 1);

        // trim
        replacement = TextUtils.trim(replacement);

        text = TextUtils.replaceInText(text, "\\b" + Pattern.quote(playerInfo.getProfile().getName()) + "\\b", replacement);

        cir.setReturnValue(text);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getPlayerByUUID(Ljava/util/UUID;)Lnet/minecraft/world/entity/player/Player;"), method = "render")
    private UUID getPlayerByUUID(UUID id) {
        uuid = id;
        return id;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerFaceRenderer;draw(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;IIIZZ)V"), index = 4)
    private int doNotDrawFace(GuiGraphics guiGraphics, ResourceLocation id, int x, int y, int size, boolean hasHatLayer, boolean upsideDown) {
        if (uuid != null) {
            Avatar avatar = AvatarManager.getAvatarForPlayer(uuid);
            if (avatar != null && avatar.renderPortrait(guiGraphics, x, y, size, 16, upsideDown))
                return 0;
        }
        return size;
    }
}
