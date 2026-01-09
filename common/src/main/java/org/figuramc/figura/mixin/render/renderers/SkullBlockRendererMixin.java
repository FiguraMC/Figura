package org.figuramc.figura.mixin.render.renderers;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.state.SkullBlockRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.figuramc.figura.ducks.SkullBlockRendererAccessor;
import org.figuramc.figura.ducks.SkullBlockRendererHelper;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.lua.api.world.BlockStateAPI;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.permissions.Permissions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererMixin implements BlockEntityRenderer<SkullBlockEntity, SkullBlockRenderState> {

    @Unique
    private static Avatar avatar;
    @Unique
    private static SkullBlockRenderState block;

    @Inject(at = @At("HEAD"), method = "submitSkull", cancellable = true)
    private static void renderSkull(Direction direction, float yaw, float animationProgress, PoseStack stack, SubmitNodeCollector submitNodeCollector, int light, SkullModelBase model, RenderType renderLayer, int outlineColor, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, CallbackInfo ci) {
        // parse block and items first, so we can yeet them in case of a missed event
        if (avatar == null)
            avatar = SkullBlockRendererHelper.getAvatar();

        SkullBlockRenderState localBlock = block;
        block = null;

        ItemStack localItem = SkullBlockRendererAccessor.getItem();
        SkullBlockRendererAccessor.setItem(null);

        Entity localEntity = SkullBlockRendererAccessor.getEntity();
        SkullBlockRendererAccessor.setEntity(null);

        SkullBlockRendererAccessor.SkullRenderMode localMode = SkullBlockRendererAccessor.getRenderMode();
        SkullBlockRendererAccessor.setRenderMode(SkullBlockRendererAccessor.SkullRenderMode.OTHER);

        // avatar pointer incase avatar variable is set during render. (unlikely)
        Avatar localAvatar = avatar;
        avatar = null;

        if (localAvatar == null || localAvatar.permissions.get(Permissions.CUSTOM_SKULL) == 0)
            return;

        float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);

        FiguraSubmitCallBackExtension modelExtension = (FiguraSubmitCallBackExtension) model;
        modelExtension.figura$setPreRenderingCallback((bufferSource, poseStack) -> {

            FiguraMod.pushProfiler(FiguraMod.MOD_ID);
            FiguraMod.pushProfiler(localAvatar);
            FiguraMod.pushProfiler("skullRender");

            // event
            BlockStateAPI b = localBlock == null ? null : new BlockStateAPI(localBlock.blockState, localBlock.blockPos);
            ItemStackAPI i = localItem != null ? ItemStackAPI.verify(localItem) : null;
            EntityAPI<?> e = localEntity != null ? EntityAPI.wrap(localEntity) : null;
            String m = localMode.name();

            FiguraMod.pushProfiler(localBlock != null ? localBlock.blockPos.toString() : String.valueOf(i));

            FiguraMod.pushProfiler("event");
            boolean bool = localAvatar.skullRenderEvent(tickDelta, b, i, e, m);

            // render skull :3
            FiguraMod.popPushProfiler("render");
            if (bool || localAvatar.skullRender(stack, bufferSource, light, direction, yaw))
                return false;

            FiguraMod.popProfiler(5);
            return true;
        });
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/SkullBlockRenderer;submitSkull(Lnet/minecraft/core/Direction;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/model/SkullModelBase;Lnet/minecraft/client/renderer/RenderType;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"), method = "submit(Lnet/minecraft/client/renderer/blockentity/state/SkullBlockRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V")
    public void render(SkullBlockRenderState skullBlockRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        block = skullBlockRenderState;
        SkullBlockRendererAccessor.setRenderMode(SkullBlockRendererAccessor.SkullRenderMode.BLOCK);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        Avatar localAvatar = avatar; // avatar pointer incase avatar variable is set during render.
        if (localAvatar == null)
            localAvatar = SkullBlockRendererHelper.getAvatar();

        return localAvatar == null || localAvatar.permissions == null ? BlockEntityRenderer.super.shouldRenderOffScreen() : localAvatar.permissions.get(Permissions.OFFSCREEN_RENDERING) == 1;
    }

    @Inject(at = @At("HEAD"), method = "resolveSkullRenderType")
    private static void getRenderType(SkullBlock.Type type, SkullBlockEntity skullBlockEntity, CallbackInfoReturnable<RenderType> cir) {
        if (type == SkullBlock.Types.PLAYER) {
            ResolvableProfile profile = skullBlockEntity.getOwnerProfile();
            if (profile != null) {
                avatar = AvatarManager.getAvatarForPlayer(profile.partialProfile().id());
            }
        }

    }
}
