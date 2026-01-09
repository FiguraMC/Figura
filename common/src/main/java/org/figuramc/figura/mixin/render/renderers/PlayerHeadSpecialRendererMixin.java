package org.figuramc.figura.mixin.render.renderers;

import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.special.PlayerHeadSpecialRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.SkullBlockRendererHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerHeadSpecialRenderer.class)
public abstract class PlayerHeadSpecialRendererMixin {
    @Inject(method = "extractArgument(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/renderer/PlayerSkinRenderCache$RenderInfo;", at = @At("HEAD"))
    public void setAvatar(ItemStack itemStack, CallbackInfoReturnable<PlayerSkinRenderCache.RenderInfo> cir) {
        ResolvableProfile profile = itemStack.get(DataComponents.PROFILE);
        Avatar avatar = profile != null ? AvatarManager.getAvatarForPlayer(profile.partialProfile().id()) : null;
        SkullBlockRendererHelper.setAvatar(avatar);
    }
}
