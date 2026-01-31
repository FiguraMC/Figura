package org.figuramc.figura.mixin.render.renderers;

import net.minecraft.client.renderer.PlayerSkinRenderCache;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.special.PlayerHeadSpecialRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.PlayerHeadRenderInfoExtension;
import org.figuramc.figura.ducks.SkullBlockRendererHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerHeadSpecialRenderer.class)
public abstract class PlayerHeadSpecialRendererMixin {
    @ModifyReturnValue(method = "extractArgument(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/renderer/special/PlayerHeadSpecialRenderer$PlayerHeadRenderInfo;", at = @At("RETURN"))
    public PlayerHeadSpecialRenderer.PlayerHeadRenderInfo setAvatar(PlayerHeadSpecialRenderer.PlayerHeadRenderInfo original, @Local(argsOnly = true) ItemStack itemStack) {
        if (original == null) {
            return null;
        }
        ResolvableProfile profile = itemStack.get(DataComponents.PROFILE);
        Avatar avatar = profile != null ? AvatarManager.getAvatarForPlayer(profile.partialProfile().id()) : null;
        ((PlayerHeadRenderInfoExtension)(Object)original).figura$setAvatar(avatar);
        return original;
    }

    @Inject(method = "render(Lnet/minecraft/client/renderer/special/PlayerHeadSpecialRenderer$PlayerHeadRenderInfo;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IIZ)V", at = @At("HEAD"))
    private void captureAvatar(PlayerHeadSpecialRenderer.PlayerHeadRenderInfo playerHeadRenderInfo, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, boolean bl, CallbackInfo ci) {
        if (playerHeadRenderInfo == null) {
            return;
        }
        Avatar avatar = ((PlayerHeadRenderInfoExtension)(Object)playerHeadRenderInfo).figura$getAvatar();
        SkullBlockRendererHelper.setAvatar(avatar);
    }
}
