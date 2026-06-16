package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.AbstractSkullBlock;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.SkullBlockRendererAccessor;
import org.figuramc.figura.lua.api.vanilla_model.VanillaModelPart;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow private ItemStack mainHandItem;

    @Shadow
    protected abstract void renderPlayerArm(PoseStack matrices, SubmitNodeCollector submitNodeCollector, int light, float equipProgress, float swingProgress, HumanoidArm arm);

    @Shadow
    protected abstract void swingArm(float f, float g, PoseStack poseStack, int i, HumanoidArm humanoidArm);

    @Shadow
    protected abstract void applyItemArmTransform(PoseStack poseStack, HumanoidArm humanoidArm, float f);

    @Shadow
    protected abstract void applyEatTransform(PoseStack poseStack, float f, HumanoidArm humanoidArm, ItemStack itemStack, Player player);

    @Shadow
    protected abstract void applyBrushTransform(PoseStack poseStack, float f, HumanoidArm humanoidArm, ItemStack itemStack, Player player, float g);

    @Unique Avatar avatar;

    // apparently hands are basically still immediate mode, wow thanks game...
    @Inject(method = "renderHandsWithItems", at = @At("HEAD"))
    private void onRenderHandsWithItems(float tickDelta, PoseStack matrices, SubmitNodeCollector submitNodeCollector, LocalPlayer player, int light, CallbackInfo ci) {
        avatar = AvatarManager.getAvatarForPlayer(player.getUUID());
        if (avatar == null)
            return;

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler(avatar);
        FiguraMod.pushProfiler("renderEvent");
        avatar.renderMode = EntityRenderMode.FIRST_PERSON;
        avatar.renderEvent(tickDelta, new FiguraMat4().set(matrices.last().pose()));
        FiguraMod.popProfiler(3);
    }

    @Inject(method = "renderHandsWithItems", at = @At(value = "RETURN"))
    private void afterRenderHandsWithItems(float tickDelta, PoseStack matrices, SubmitNodeCollector submitNodeCollector, LocalPlayer player, int light, CallbackInfo ci) {
        if (avatar == null)
            return;

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler(avatar);
        FiguraMod.pushProfiler("postRenderEvent");
        avatar.postRenderEvent(tickDelta, new FiguraMat4().set(matrices.last().pose()));
        avatar = null;
        FiguraMod.popProfiler(3);

    }

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void renderArmWithItem(AbstractClientPlayer player, float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, SubmitNodeCollector submitNodeCollector, int light, CallbackInfo ci) {
        if (player.isScoping() || avatar == null || avatar.luaRuntime == null)
            return;

        boolean mainHand = hand == InteractionHand.MAIN_HAND;
        HumanoidArm arm = mainHand ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean leftHanded = arm == HumanoidArm.LEFT;
        boolean rightHanded = arm == HumanoidArm.RIGHT;

        VanillaModelPart vanillaPart = leftHanded
                ? avatar.luaRuntime.vanilla_model.LEFT_ITEM
                : avatar.luaRuntime.vanilla_model.RIGHT_ITEM;

        boolean willRenderItem = !item.isEmpty();
        boolean willRenderArm = (!willRenderItem && mainHand)
                || item.is(Items.FILLED_MAP)
                || (!willRenderItem && this.mainHandItem.is(Items.FILLED_MAP));

        // hide arm
        Boolean armVisible = leftHanded
                ? avatar.luaRuntime.renderer.renderLeftArm
                : avatar.luaRuntime.renderer.renderRightArm;

        if (willRenderArm && !willRenderItem && armVisible != null && !armVisible) {
            ci.cancel();
            return;
        }

        // render arm
        if (!willRenderArm && !player.isInvisible() && armVisible != null && armVisible) {
            matrices.pushPose();
            this.renderPlayerArm(matrices, submitNodeCollector, light, equipProgress, swingProgress, arm);
            matrices.popPose();
        }

        if (willRenderItem) {
            matrices.pushPose();

            int q = rightHanded ? 1 : -1;

            // these are all the checks mojang does. i'll leave them here in case we want to transform
            // custom items according to the underlying item.

//                if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
//                    switch (item.getUseAnimation()) {
//                        case NONE:
//                            this.applyItemArmTransform(matrices, humanoidArm, equipProgress);
//                            break;
//                        case EAT:
//                        case DRINK:
//                            this.applyEatTransform(matrices, tickDelta, humanoidArm, item, player);
//                            this.applyItemArmTransform(matrices, humanoidArm, equipProgress);
//                            break;
//                        case BLOCK:
//                            this.applyItemArmTransform(matrices, humanoidArm, equipProgress);
//                            if (!(item.getItem() instanceof ShieldItem)) {
//                                matrices.translate((float)q * -0.14142136F, 0.08F, 0.14142136F);
//                                matrices.mulPose(Axis.XP.rotationDegrees(-102.25F));
//                                matrices.mulPose(Axis.YP.rotationDegrees((float)q * 13.365F));
//                                matrices.mulPose(Axis.ZP.rotationDegrees((float)q * 78.05F));
//                            }
//                            break;
//                        case BOW: {
//                            this.applyItemArmTransform(matrices, humanoidArm, equipProgress);
//                            matrices.translate((float) q * -0.2785682F, 0.18344387F, 0.15731531F);
//                            matrices.mulPose(Axis.XP.rotationDegrees(-13.935F));
//                            matrices.mulPose(Axis.YP.rotationDegrees((float) q * 35.3F));
//                            matrices.mulPose(Axis.ZP.rotationDegrees((float) q * -9.785F));
//                            float r = (float) item.getUseDuration(player) - ((float) player.getUseItemRemainingTicks() - tickDelta + 1.0F);
//                            float l = r / 20.0F;
//                            l = (l * l + l * 2.0F) / 3.0F;
//                            if (l > 1.0F) {
//                                l = 1.0F;
//                            }
//
//                            if (l > 0.1F) {
//                                float m = Mth.sin((r - 0.1F) * 1.3F);
//                                float n = l - 0.1F;
//                                float o = m * n;
//                                matrices.translate(o * 0.0F, o * 0.004F, o * 0.0F);
//                            }
//
//                            matrices.translate(l * 0.0F, l * 0.0F, l * 0.04F);
//                            matrices.scale(1.0F, 1.0F, 1.0F + l * 0.2F);
//                            matrices.mulPose(Axis.YN.rotationDegrees((float) q * 45.0F));
//                            break;
//                        }
//                        case SPEAR:
//                            this.applyItemArmTransform(matrices, humanoidArm, equipProgress);
//                            matrices.translate((float)q * -0.5F, 0.7F, 0.1F);
//                            matrices.mulPose(Axis.XP.rotationDegrees(-55.0F));
//                            matrices.mulPose(Axis.YP.rotationDegrees((float)q * 35.3F));
//                            matrices.mulPose(Axis.ZP.rotationDegrees((float)q * -9.785F));
//                            float r = (float)item.getUseDuration(player) - ((float)player.getUseItemRemainingTicks() - tickDelta + 1.0F);
//                            float l = r / 10.0F;
//                            if (l > 1.0F) {
//                                l = 1.0F;
//                            }
//
//                            if (l > 0.1F) {
//                                float m = Mth.sin((r - 0.1F) * 1.3F);
//                                float n = l - 0.1F;
//                                float o = m * n;
//                                matrices.translate(o * 0.0F, o * 0.004F, o * 0.0F);
//                            }
//
//                            matrices.translate(0.0F, 0.0F, l * 0.2F);
//                            matrices.scale(1.0F, 1.0F, 1.0F + l * 0.2F);
//                            matrices.mulPose(Axis.YN.rotationDegrees((float)q * 45.0F));
//                            break;
//                        case BRUSH:
//                            this.applyBrushTransform(matrices, tickDelta, humanoidArm, item, player, equipProgress);
//                            break;
//                        case BUNDLE:
//                            this.swingArm(swingProgress, equipProgress, matrices, q, humanoidArm);
//                    }
//                } else if (player.isAutoSpinAttack()) {
//                    this.applyItemArmTransform(matrices, humanoidArm, equipProgress);
//                    matrices.translate((float)q * -0.4F, 0.8F, 0.3F);
//                    matrices.mulPose(Axis.YP.rotationDegrees((float)q * 65.0F));
//                    matrices.mulPose(Axis.ZP.rotationDegrees((float)q * -85.0F));
//                } else {

            // this is bare minimum
            this.swingArm(swingProgress, equipProgress, matrices, q, arm);
//                }

            boolean rendered = avatar.itemRenderEvent(
                    ItemStackAPI.verify(item),
                    leftHanded ? "FIRST_PERSON_LEFT_HAND" : "FIRST_PERSON_RIGHT_HAND",
                    FiguraVec3.of(0, 0, 0),
                    FiguraVec3.of(0,0,0),
                    FiguraVec3.of(1,1,1),
                    leftHanded,
                    matrices,
                    submitNodeCollector,
                    light,
                    OverlayTexture.NO_OVERLAY
            );

            matrices.popPose();

            if (rendered) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"))
    private void renderItem(LivingEntity entity, ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack matrices, SubmitNodeCollector submitNodeCollector, int light, CallbackInfo ci) {
        if (stack.getItem() instanceof BlockItem bl && bl.getBlock() instanceof AbstractSkullBlock) {
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
    }
}
