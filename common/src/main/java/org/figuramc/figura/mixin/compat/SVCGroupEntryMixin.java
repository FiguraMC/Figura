package org.figuramc.figura.mixin.compat;

import java.util.function.Function;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.compat.FiguraSVCIconCompat;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.lua.api.nameplate.NameplateCustomization;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;

import de.maxhenkel.voicechat.gui.group.GroupEntry;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Pseudo
@Mixin(value = GroupEntry.class, remap = true)
public class SVCGroupEntryMixin {
    @Shadow
    protected PlayerState state;

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V"), slice = @Slice(from = @At(value = "INVOKE", target = "Lde/maxhenkel/voicechat/gui/GameProfileUtils;getSkin(Ljava/util/UUID;)Lnet/minecraft/resources/ResourceLocation;"), to = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V")))
    private boolean figuraSvcPortrait(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        return FiguraSVCIconCompat.shouldRenderVanillaIcon(guiGraphics, state, 1, 1, 20, 16);
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"))
    private boolean figuraSvcName(GuiGraphics guiGraphics, Font renderer, Component text, int x, int y, int color, boolean shadowed, @Local boolean hovered) {
        if (!Configs.SVC_NAMEPLATE.value)
            return true;

        Component playerName = Component.literal(state.getName());

        int config = Configs.CHAT_NAMEPLATE.value;

        // apply customization
        Avatar avatar = AvatarManager.getAvatarForPlayer(state.getUuid());
        NameplateCustomization custom = avatar == null || avatar.luaRuntime == null ? null : avatar.luaRuntime.nameplate.CHAT;

        if (custom == null && config < 2) // no customization and no possible badges to append
            return true;

        Component replacement = custom != null && custom.getJson() != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 1 ? TextUtils.replaceInText(custom.getJson().copy(), "\n|\\\\n", " ") : playerName;

        // name
        replacement = TextUtils.replaceInText(replacement, "\\$\\{name\\}", playerName);

        // badges
        Component emptyReplacement = Badges.appendBadges(replacement, state.getUuid(), config > 1);

        // trim
        emptyReplacement = TextUtils.trim(emptyReplacement);

        int width = (hovered && !ClientManager.getPlayerStateManager().getOwnID().equals(this.state.getUuid())) ? 80 : 180;
        UIHelper.renderScrollingText(guiGraphics, emptyReplacement, x, y, width, 0xFFFFFF);

        return false;
    }
}
