package org.moon.figura.mixin.render.renderers;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.ducks.SkullBlockRendererAccessor;
import org.moon.figura.lua.api.world.BlockStateAPI;
import org.moon.figura.lua.api.world.ItemStackAPI;
import org.moon.figura.trust.Trust;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererMixin implements BlockEntityRenderer<SkullBlockEntity> {

    @Unique
    private static Avatar avatar;
    @Unique
    private static SkullBlockEntity block;

    @Inject(at = @At("HEAD"), method = "renderSkull", cancellable = true)
    private static void renderSkull(Direction direction, float yaw, float animationProgress, PoseStack stack, MultiBufferSource bufferSource, int light, SkullModelBase model, RenderType renderLayer, CallbackInfo ci) {
        //parse block and items first, so we can yeet them in case of a missed event
        SkullBlockEntity localBlock = block;
        block = null;

        ItemStack localItem = SkullBlockRendererAccessor.getReferenceItem();
        SkullBlockRendererAccessor.setReferenceItem(null);

        //avatar
        Avatar localAvatar = avatar;
        avatar = null;

        if (localAvatar == null || localAvatar.trust.get(Trust.CUSTOM_HEADS) == 0)
            return;

        //event
        BlockStateAPI b = localBlock == null ? null : new BlockStateAPI(localBlock.getBlockState(), localBlock.getBlockPos());
        ItemStackAPI i = localItem != null ? ItemStackAPI.verify(localItem) : null;

        boolean bool = localAvatar.skullRenderEvent(Minecraft.getInstance().getFrameTime(), b, i);

        //render skull :3
        if (bool || localAvatar.skullRender(stack, bufferSource, light, direction, yaw))
            ci.cancel();
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/SkullBlockRenderer;renderSkull(Lnet/minecraft/core/Direction;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/SkullModelBase;Lnet/minecraft/client/renderer/RenderType;)V"), method = "render(Lnet/minecraft/world/level/block/entity/SkullBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V")
    public void render(SkullBlockEntity skullBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, CallbackInfo ci) {
        block = skullBlockEntity;
    }

    @Override
    public boolean shouldRenderOffScreen(SkullBlockEntity blockEntity) {
        return avatar != null && avatar.trust.get(Trust.OFFSCREEN_RENDERING) == 1;
    }

    @Inject(at = @At("HEAD"), method = "getRenderType")
    private static void getRenderType(SkullBlock.Type type, GameProfile profile, CallbackInfoReturnable<RenderType> cir) {
        avatar = profile != null && profile.getId() != null ? AvatarManager.getAvatarForPlayer(profile.getId()) : null;
    }
}
