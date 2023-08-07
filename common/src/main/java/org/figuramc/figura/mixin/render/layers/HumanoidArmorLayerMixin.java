package org.figuramc.figura.mixin.render.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.utils.FiguraArmorPartRenderer;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> implements HumanoidArmorLayerAccessor<T, M, A> {

    @Shadow
    protected abstract A getArmorModel(EquipmentSlot slot);

    @Shadow
    @Final
    private TextureAtlas armorTrimAtlas;

    @Shadow
    protected abstract void renderArmorPiece(PoseStack matrices, MultiBufferSource vertexConsumers, T entity, EquipmentSlot armorSlot, int light, A model);

    @Unique
    private Avatar figura$avatar;

    @Unique
    private boolean figura$renderingVanillaArmor;

    public HumanoidArmorLayerMixin(RenderLayerParent<T, M> context) {
        super(context);
    }

    @Inject(at = @At(value = "HEAD"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
    public void onRender(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        this.figura$avatar = AvatarManager.getAvatar(livingEntity);
    }

    @Inject(at = @At(value = "TAIL"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
    public void onRenderEnd(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        this.figura$avatar = AvatarManager.getAvatar(livingEntity);
        if (figura$avatar == null) return;
        figura$tryRenderArmorPart(EquipmentSlot.HEAD, this::figura$helmetRenderer, poseStack, livingEntity, multiBufferSource, i, ParentType.HelmetPivot);
        figura$tryRenderArmorPart(EquipmentSlot.CHEST, this::figura$chestplateRenderer, poseStack, livingEntity, multiBufferSource, i, ParentType.LeftShoulderPivot, ParentType.ChestplatePivot, ParentType.RightShoulderPivot);
        figura$tryRenderArmorPart(EquipmentSlot.LEGS, this::figura$leggingsRenderer, poseStack, livingEntity, multiBufferSource, i, ParentType.LeftLeggingPivot, ParentType.RightLeggingPivot, ParentType.LeggingsPivot);
        figura$tryRenderArmorPart(EquipmentSlot.FEET, this::figura$bootsRenderer, poseStack, livingEntity, multiBufferSource, i, ParentType.LeftBootPivot, ParentType.RightBootPivot);

        figura$avatar = null;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;usesInnerModel(Lnet/minecraft/world/entity/EquipmentSlot;)Z"), method = "renderArmorPiece")
    public void onRenderArmorPiece(PoseStack poseStack, MultiBufferSource multiBufferSource, T livingEntity, EquipmentSlot equipmentSlot, int i, A humanoidModel, CallbackInfo ci) {
        if (figura$avatar == null) return;
        VanillaPart part = RenderUtils.partFromSlot(figura$avatar, equipmentSlot);
        if (part != null) {
            part.save(humanoidModel);
            part.preTransform(humanoidModel);
            part.posTransform(humanoidModel);
        }
    }

    @Inject(at = @At("HEAD"), method = "renderArmorPiece", cancellable = true)
    public void renderArmorPieceHijack(PoseStack poseStack, MultiBufferSource multiBufferSource, T livingEntity, EquipmentSlot equipmentSlot, int i, A humanoidModel, CallbackInfo ci) {
        if (figura$avatar == null) {
            return;
        }

        if (!figura$renderingVanillaArmor) {
            ci.cancel();
        }
    }


    @Inject(at = @At("RETURN"), method = "renderArmorPiece")
    public void postRenderArmorPiece(PoseStack poseStack, MultiBufferSource multiBufferSource, T livingEntity, EquipmentSlot equipmentSlot, int i, A humanoidModel, CallbackInfo ci) {
        if (figura$avatar == null) return;
        VanillaPart part = RenderUtils.partFromSlot(figura$avatar, equipmentSlot);
        if (part != null)
            part.restore(humanoidModel);
    }

    @Unique
    private void figura$tryRenderArmorPart(EquipmentSlot slot, FiguraArmorPartRenderer<T, M, A> renderer, PoseStack vanillaPoseStack, T entity, MultiBufferSource vertexConsumers, int light, ParentType... parentTypes) {
        ItemStack itemStack = entity.getItemBySlot(slot);

        // Make sure the item in the equipment slot is actually a piece of armor
        if ((itemStack.getItem() instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == slot)) {
            A armorModel = getArmorModel(slot);
            boolean allFailed = true;

            // Go through each parent type needed to render the current piece of armor
            for (ParentType parentType : parentTypes) {

                // Try to render the pivot part
                boolean renderedPivot = figura$avatar.pivotPartRender(parentType, stack -> {
                    stack.pushPose();
                    figura$prepareArmorRender(stack);
                    renderer.renderArmorPart(stack, vertexConsumers, light, armorModel, entity, itemStack, slot, armorItem, parentType);
                    stack.popPose();
                });

                if (renderedPivot) {
                    allFailed = false;
                }
            }

            // As a fallback, render armor the vanilla way
            if (allFailed) {
                figura$renderingVanillaArmor = true;
                renderArmorPiece(vanillaPoseStack, vertexConsumers, entity, slot, light, armorModel);
                figura$renderingVanillaArmor = false;
            }
        }

    }

    // Prepare the transformations for rendering armor on the avatar
    @Unique
    private void figura$prepareArmorRender(PoseStack stack) {
        stack.scale(16, 16, 16);
        stack.mulPose(Axis.XP.rotationDegrees(180f));
        stack.mulPose(Axis.YP.rotationDegrees(180f));
    }

    @Unique
    private void figura$helmetRenderer(PoseStack poseStack, MultiBufferSource vertexConsumers, int light, A model, T entity, ItemStack itemStack, EquipmentSlot armorSlot, ArmorItem armorItem, ParentType parentType) {
        if (parentType == ParentType.HelmetPivot) {
            figura$renderArmorPart(model.head, poseStack, vertexConsumers, light, entity, itemStack, armorSlot, armorItem);
        }
    }

    @Unique
    private void figura$chestplateRenderer(PoseStack poseStack, MultiBufferSource vertexConsumers, int light, A model, T entity, ItemStack itemStack, EquipmentSlot armorSlot, ArmorItem armorItem, ParentType parentType) {
        if (parentType == ParentType.ChestplatePivot) {
            figura$renderArmorPart(model.body, poseStack, vertexConsumers, light, entity, itemStack, armorSlot, armorItem);
        }

        if (parentType == ParentType.LeftShoulderPivot) {
            poseStack.pushPose();
            poseStack.translate(-6 / 16f, 0f, 0f);
            figura$renderArmorPart(model.leftArm, poseStack, vertexConsumers, light, entity, itemStack, armorSlot, armorItem);
            poseStack.popPose();
        }

        if (parentType == ParentType.RightShoulderPivot) {
            poseStack.pushPose();
            poseStack.translate(6 / 16f, 0f, 0f);
            figura$renderArmorPart(model.rightArm, poseStack, vertexConsumers, light, entity, itemStack, armorSlot, armorItem);
            poseStack.popPose();
        }
    }

    @Unique
    private void figura$leggingsRenderer(PoseStack poseStack, MultiBufferSource vertexConsumers, int light, A model, T entity, ItemStack itemStack, EquipmentSlot armorSlot, ArmorItem armorItem, ParentType parentType) {
        if (parentType == ParentType.LeggingsPivot) {
            poseStack.pushPose();
            poseStack.translate(0, -12 / 16f, 0);
            figura$renderArmorPart(model.body, poseStack, vertexConsumers, light, entity, itemStack, armorSlot, armorItem);
            poseStack.popPose();
        }

        if (parentType == ParentType.LeftLeggingPivot) {
            poseStack.pushPose();
            poseStack.translate(-2 / 16f, -12 / 16f, 0);
            figura$renderArmorPart(model.leftLeg, poseStack, vertexConsumers, light, entity, itemStack, armorSlot, armorItem);
            poseStack.popPose();
        }

        if (parentType == ParentType.RightLeggingPivot) {
            poseStack.pushPose();
            poseStack.translate(2 / 16f, -12 / 16f, 0);
            figura$renderArmorPart(model.rightLeg, poseStack, vertexConsumers, light, entity, itemStack, armorSlot, armorItem);
            poseStack.popPose();
        }
    }

    @Unique
    private void figura$bootsRenderer(PoseStack poseStack, MultiBufferSource vertexConsumers, int light, A model, T entity, ItemStack itemStack, EquipmentSlot armorSlot, ArmorItem armorItem, ParentType parentType) {
        if (parentType == ParentType.LeftBootPivot) {
            poseStack.pushPose();
            poseStack.translate(-2 / 16f, -24 / 16f, 0);
            figura$renderArmorPart(model.leftLeg, poseStack, vertexConsumers, light, entity, itemStack, armorSlot, armorItem);
            poseStack.popPose();
        }

        if (parentType == ParentType.RightBootPivot) {
            poseStack.pushPose();
            poseStack.translate(2 / 16f, -24 / 16f, 0);
            figura$renderArmorPart(model.rightLeg, poseStack, vertexConsumers, light, entity, itemStack, armorSlot, armorItem);
            poseStack.popPose();
        }
    }


    // Similar to vanilla's renderArmorModel, but it renders each part individually, instead of the whole model at once.
    // Could be optimized by calculating the tint, overlays, and trims beforehand instead of re-calculating for each ModelPart, but it's not super important.
    @Unique
    private void figura$renderArmorPart(ModelPart modelPart, PoseStack poseStack, MultiBufferSource vertexConsumers, int light, T entity, ItemStack itemStack, EquipmentSlot armorSlot, ArmorItem armorItem) {
        boolean bl = this.usesInnerModel(armorSlot);
        boolean hasOverlay = false;
        boolean hasGlint = itemStack.hasFoil();

        modelPart.visible = true;
        modelPart.xRot = 0;
        modelPart.yRot = 0;
        modelPart.zRot = 0;

        float tintR = 1;
        float tintG = 1;
        float tintB = 1;

        if (armorItem instanceof DyeableArmorItem dyeableArmorItem) {
            int i = dyeableArmorItem.getColor(itemStack);
            tintR = (float) (i >> 16 & 255) / 255.0F;
            tintG = (float) (i >> 8 & 255) / 255.0F;
            tintB = (float) (i & 255) / 255.0F;
            hasOverlay = true;
        }

        VertexConsumer regularArmorConsumer = vertexConsumers.getBuffer(RenderType.armorCutoutNoCull(this.getArmorLocation(armorItem, bl, null)));
        modelPart.render(poseStack, regularArmorConsumer, light, OverlayTexture.NO_OVERLAY, tintR, tintG, tintB, 1f);

        if (hasOverlay) {
            VertexConsumer overlaidArmorConsumer = vertexConsumers.getBuffer(RenderType.armorCutoutNoCull(this.getArmorLocation(armorItem, bl, "overlay")));
            modelPart.render(poseStack, overlaidArmorConsumer, light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        }

        ArmorTrim.getTrim(entity.level().registryAccess(), itemStack).ifPresent((permutation) -> {
            var armorMaterial = armorItem.getMaterial();
            TextureAtlasSprite trimAtlas = this.armorTrimAtlas.getSprite(bl ? permutation.innerTexture(armorMaterial) : permutation.outerTexture(armorMaterial));
            VertexConsumer trimConsumer = trimAtlas.wrap(vertexConsumers.getBuffer(Sheets.armorTrimsSheet()));
            modelPart.render(poseStack, trimConsumer, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        });

        if (hasGlint) {
            modelPart.render(poseStack, vertexConsumers.getBuffer(RenderType.armorEntityGlint()), light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    @Inject(at = @At("RETURN"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
    public void postRender(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        figura$avatar = null;
    }
}
