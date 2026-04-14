package org.figuramc.figura.mixin.render.renderers;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.PlayerHeadSpecialRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraSkullAvatarAssociationExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerHeadSpecialRenderer.class)
public abstract class PlayerHeadSpecialRendererMixin {
    @ModifyReturnValue(method = "extractArgument(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/renderer/PlayerSkinRenderCache$RenderInfo;", at = @At("TAIL"))
    public PlayerSkinRenderCache.RenderInfo setAvatar(PlayerSkinRenderCache.RenderInfo original, @Local(argsOnly = true) ItemStack itemStack) {
        ResolvableProfile profile = itemStack.get(DataComponents.PROFILE);
        Avatar avatar = profile != null ? AvatarManager.getAvatarForPlayer(profile.partialProfile().id()) : null;
        ((FiguraSkullAvatarAssociationExtension)(Object)original).figura$setAvatar(avatar);
        return original;
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/PlayerSkinRenderCache$RenderInfo;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V", at = @At("HEAD"))
    private void captureAvatar(PlayerSkinRenderCache.RenderInfo playerHeadRenderInfo, ItemDisplayContext itemDisplayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, boolean bl, int k, CallbackInfo ci) {
        if (playerHeadRenderInfo == null) {
            return;
        }
        // copy the avatar from the playerHeadRenderInfo into the inner renderType()
        // possibly redundant, see PlayerHeadRenderInfoMixin for implementation details
        Avatar avatar = ((FiguraSkullAvatarAssociationExtension)(Object)playerHeadRenderInfo).figura$getAvatar();
        ((FiguraSkullAvatarAssociationExtension)playerHeadRenderInfo.renderType()).figura$setAvatar(avatar);
    }
}
