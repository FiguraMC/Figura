package org.figuramc.figura.mixin.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.joml.Quaternionf;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "de.maxhenkel.voicechat.voice.client.RenderEvents")
public class SimpleVCMixin {
    // Simple VC will hide your icon, this makes it so it is rendered if your nameplate is rendered
    @Redirect(method = "onRenderName", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;", opcode = Opcodes.GETFIELD, remap = true), remap = false)
    private LocalPlayer renderSelfNameplate(Minecraft minecraft){
        return Configs.SELF_NAMEPLATE.value ? null : minecraft.player;
    }

    @Inject(method = "shouldShowIcons", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void renderSelfNameplate(CallbackInfoReturnable<Boolean> callbackInfoReturnable){
        callbackInfoReturnable.setReturnValue(true);
    }

    @Final
    @Shadow
    private Minecraft minecraft;

    // Everything under this just cancels SimpleVC's transforms to the PoseStack so that Figura's values are used instead

    @Redirect(method = "renderPlayerIcon", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V", remap = true), remap = false)
    private void figuraScale(PoseStack instance, float x, float y, float z, Player player){
        // return on config or high entity distance
        int config = Configs.ENTITY_NAMEPLATE.value;
        if (config == 0 || AvatarManager.panic || minecraft.getEntityRenderDispatcher().distanceToSqr(player) > 4096) {
            instance.scale(x, y, z);
        }
    }

    @Redirect(method = "renderPlayerIcon", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",remap = true), slice = @Slice(from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",remap = true), to = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V", remap = true)), remap = false)
    private void figuraPivot(PoseStack instance, double x, double y, double z, Player player){
        // return on config or high entity distance
        int config = Configs.ENTITY_NAMEPLATE.value;
        if (config == 0 || AvatarManager.panic || minecraft.getEntityRenderDispatcher().distanceToSqr(player) > 4096) {
            instance.translate(x, y, z);
        }
    }

    @Redirect(method = "renderPlayerIcon", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V", remap = true), remap = false)
    private void cancelMul(PoseStack instance, Quaternionf quaternionIn, Player player){
        // return on config or high entity distance
        int config = Configs.ENTITY_NAMEPLATE.value;
        if (config == 0 || AvatarManager.panic || minecraft.getEntityRenderDispatcher().distanceToSqr(player) > 4096) {
            instance.mulPose(quaternionIn);
        }
    }

    @Redirect(method = "renderPlayerIcon", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", remap = true), remap = false)
    private void cancelPush(PoseStack instance, Player player){
        // return on config or high entity distance
        int config = Configs.ENTITY_NAMEPLATE.value;
        if (config == 0 || AvatarManager.panic || minecraft.getEntityRenderDispatcher().distanceToSqr(player) > 4096) {
            instance.pushPose();
        }
    }

    @Redirect(method = "renderPlayerIcon", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", remap = true), remap = false)
    private void cancelPop(PoseStack instance, Player player){
        // return on config or high entity distance
        int config = Configs.ENTITY_NAMEPLATE.value;
        if (config == 0 || AvatarManager.panic || minecraft.getEntityRenderDispatcher().distanceToSqr(player) > 4096) {
            instance.popPose();
        } else {
            // resets the poseStack's y as Simple VC offsets it
            instance.translate(0.0, 1.0, 0.0);
        }
    }
}