package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.SkullBlockRendererAccessor;
import org.figuramc.figura.lua.api.vanilla_model.VanillaModelPart;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.figuramc.figura.ducks.FiguraItemStackRenderStateExtension;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.math.vector.FiguraVec3;


@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow
    protected abstract void renderPlayerArm(PoseStack matrices, SubmitNodeCollector submitNodeCollector, int light, float equipProgress, float swingProgress, HumanoidArm arm, PlayerRenderState playerState);

    @Unique Avatar avatar;

    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    private void onRenderHandsWithItems(float tickDelta, PoseStack matrices, SubmitNodeCollector submitNodeCollector, PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState handsState, CallbackInfo ci) {
        avatar = AvatarManager.getAvatar(playerState.avatarRenderState);
        if (avatar == null)
            return;

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler(avatar);
        FiguraMod.pushProfiler("renderEvent");
        avatar.renderMode = EntityRenderMode.FIRST_PERSON;
        avatar.renderEvent(tickDelta, new FiguraMat4().set(matrices.last().pose()));
        FiguraMod.popProfiler(3);
    }

    @Inject(method = "submitHandsWithItems", at = @At(value = "RETURN"))
    private void afterRenderHandsWithItems(float tickDelta, PoseStack matrices, SubmitNodeCollector submitNodeCollector, PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState handsState, CallbackInfo ci) {
        if (avatar == null)
            return;

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler(avatar);
        FiguraMod.pushProfiler("postRenderEvent");
        avatar.postRenderEvent(tickDelta, new FiguraMat4().set(matrices.last().pose()));
        avatar = null;
        FiguraMod.popProfiler(3);

    }

    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void renderArmWithItem(PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState handsState, float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, SubmitNodeCollector submitNodeCollector, int light, CallbackInfo ci) {
        if (handsState.isScoping || playerState.avatarRenderState == null || avatar == null || avatar.luaRuntime == null)
            return;

        boolean main = hand ==InteractionHand.MAIN_HAND;
        HumanoidArm arm = main ? playerState.avatarRenderState.mainArm : playerState.avatarRenderState.mainArm.getOpposite();
        Boolean armVisible = arm == HumanoidArm.LEFT ? avatar.luaRuntime.renderer.renderLeftArm : avatar.luaRuntime.renderer.renderRightArm;

        boolean willRenderItem = !item.isEmpty();
        boolean willRenderArm = (!willRenderItem && main) || item.has(DataComponents.MAP_ID) || (!willRenderItem && handsState.mainHandItem.has(DataComponents.MAP_ID));

        // hide arm
        if (willRenderArm && !willRenderItem && armVisible != null && !armVisible) {
            ci.cancel();
            return;
        }
        // render arm
        if (!willRenderArm && !playerState.avatarRenderState.isInvisible && armVisible != null && armVisible) {
            matrices.pushPose();
            this.renderPlayerArm(matrices, submitNodeCollector, light, equipProgress, swingProgress, arm, playerState);
            matrices.popPose();
        }

        // hide item
        VanillaModelPart part = arm == HumanoidArm.LEFT ? avatar.luaRuntime.vanilla_model.LEFT_ITEM : avatar.luaRuntime.vanilla_model.RIGHT_ITEM;
        if (willRenderItem && !part.checkVisible()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderPlayerHand", at = @At("HEAD"), cancellable = true)
    private void hidePlayerHand(PoseStack matrices, SubmitNodeCollector collector, int light, HumanoidArm arm, PlayerRenderState playerState, CallbackInfo ci) {
        if (avatar == null || avatar.luaRuntime == null)
            return;
        Boolean visible = arm == HumanoidArm.LEFT ? avatar.luaRuntime.renderer.renderLeftArm : avatar.luaRuntime.renderer.renderRightArm;
        if (Boolean.FALSE.equals(visible))
            ci.cancel();
    }
    @WrapOperation(method = "submitArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"))
    private void renderItemSubmit(
            ItemStackRenderState instance,
            PoseStack matrices,
            SubmitNodeCollector submitNodeCollector,
            int light,
            int overlay,
            int outlineColor,
            Operation<Void> original,
            @Local(argsOnly = true) PlayerRenderState playerState,
            @Local(argsOnly = true) ItemStack stack
    ) {
        Entity entity = AvatarManager.getEntity(playerState.avatarRenderState);
        ItemDisplayContext itemDisplayContext = ((FiguraItemStackRenderStateExtension) instance).figura$getDisplayContext();
        if (stack.getItem() instanceof BlockItem bl && bl.getBlock() instanceof AbstractSkullBlock) {
            SkullBlockRendererAccessor.setItem(stack);
            SkullBlockRendererAccessor.setEntity(entity);
            SkullBlockRendererAccessor.setRenderMode(switch (itemDisplayContext) {
                case FIRST_PERSON_LEFT_HAND -> SkullBlockRendererAccessor.SkullRenderMode.FIRST_PERSON_LEFT_HAND;
                case FIRST_PERSON_RIGHT_HAND -> SkullBlockRendererAccessor.SkullRenderMode.FIRST_PERSON_RIGHT_HAND;
                case THIRD_PERSON_LEFT_HAND -> SkullBlockRendererAccessor.SkullRenderMode.THIRD_PERSON_LEFT_HAND;
                case THIRD_PERSON_RIGHT_HAND -> SkullBlockRendererAccessor.SkullRenderMode.THIRD_PERSON_RIGHT_HAND;
                default -> itemDisplayContext.leftHand() ? SkullBlockRendererAccessor.SkullRenderMode.THIRD_PERSON_LEFT_HAND // should never happen
                        : SkullBlockRendererAccessor.SkullRenderMode.THIRD_PERSON_RIGHT_HAND; 
            });
        }

        Avatar av = AvatarManager.getAvatar(entity);
        if (av == null)
            av = this.avatar;

        ItemTransform transform = ((FiguraItemStackRenderStateExtension) instance).figura$getItemTransform();

        if (av == null || !av.itemRenderEvent(
                ItemStackAPI.verify(stack),
                itemDisplayContext.name(),
                FiguraVec3.fromVec3f(transform.translation()),
                FiguraVec3.of(transform.rotation().z(), transform.rotation().y(), transform.rotation().x()),
                FiguraVec3.fromVec3f(transform.scale()),
                itemDisplayContext.leftHand(),
                matrices,
                submitNodeCollector,
                light,
                overlay
        )) {
            original.call(instance, matrices, submitNodeCollector, light, overlay, outlineColor);
        }
    }
}
