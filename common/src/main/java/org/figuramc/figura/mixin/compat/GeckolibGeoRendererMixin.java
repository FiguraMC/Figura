package org.figuramc.figura.mixin.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.GeckolibGeoArmorAccessor;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;

@Pseudo
@Mixin(value = GeoRenderer.class, remap = false)
public interface GeckolibGeoRendererMixin {
    @Shadow
    void renderRecursively(PoseStack par1, GeoAnimatable par2, GeoBone par3, RenderType par4, MultiBufferSource par5, VertexConsumer par6, boolean par7, float par8, int par9, int par10, float par11, float par12, float par13, float par14);

    @Redirect(method = "actuallyRender", at = @At(value = "INVOKE", target = "Lsoftware/bernie/geckolib/renderer/GeoRenderer;renderRecursively(Lcom/mojang/blaze3d/vertex/PoseStack;Lsoftware/bernie/geckolib/core/animatable/GeoAnimatable;Lsoftware/bernie/geckolib/cache/object/GeoBone;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZFIIFFFF)V"))
    private void modifyBone(GeoRenderer instance, PoseStack poseStack, GeoAnimatable geoAnimatable, GeoBone geoBone, RenderType renderType, MultiBufferSource multiBufferSource, VertexConsumer vertexConsumer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
        boolean allFailed = true;

        // If the renderer is an armor renderer and the avatar is not null
        if (instance instanceof GeoArmorRenderer && ((GeckolibGeoArmorAccessor) instance).figura$getAvatar() != null) {
            GeoArmorRenderer armorRenderer = (GeoArmorRenderer) instance;
            Avatar avatar = ((GeckolibGeoArmorAccessor)armorRenderer).figura$getAvatar();
            ParentType type;
            if (geoBone.equals(armorRenderer.getHeadBone())) {
                type = ParentType.HelmetPivot;
            } else if (geoBone.equals(armorRenderer.getBodyBone())) {
                type = ParentType.ChestplatePivot;
            } else if (geoBone.equals(armorRenderer.getRightArmBone())) {
                type = ParentType.RightShoulderPivot;
            } else if (geoBone.equals(armorRenderer.getLeftArmBone())) {
                type = ParentType.LeftShoulderPivot;
            } else if (geoBone.equals(armorRenderer.getRightLegBone())) {
                type = ParentType.RightLeggingPivot;
            } else if (geoBone.equals(armorRenderer.getLeftLegBone())) {
                type = ParentType.LeftLeggingPivot;
            } else if (geoBone.equals(armorRenderer.getRightBootBone())) {
                type = ParentType.RightBootPivot;
            } else if (geoBone.equals(armorRenderer.getLeftBootBone())) {
                type = ParentType.LeftBootPivot;
            } else {
                 type = ParentType.None;
            }
            EquipmentSlot slot = RenderUtils.slotFromPart(type);
            if (slot != null && slot == armorRenderer.getCurrentSlot())
                allFailed = figura$renderPivot(armorRenderer, avatar, type, geoAnimatable, geoBone, renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        }

        // If pivots didn't exist for the type or failed, let geckolib render as normal
        if (allFailed)
            renderRecursively(poseStack, geoAnimatable, geoBone, renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    // Returns the opposite/false to match our MixinHumanoidArmorLayer
    @Unique
    private boolean figura$renderPivot(GeoArmorRenderer armorRenderer, Avatar avatar, ParentType parentType, GeoAnimatable geoAnimatable, GeoBone geoBone, RenderType renderType, MultiBufferSource multiBufferSource, VertexConsumer vertexConsumer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        return !avatar.pivotPartRender(parentType, stack -> {
            stack.pushPose();
            figura$prepareArmorRender(stack);
            figura$transformStackBasedOnType(stack, parentType);

            ((GeckolibGeoArmorAccessor)armorRenderer).figura$setEntityRenderTranslations(stack.last().pose());

            stack.pushPose();
            BakedGeoModel model = armorRenderer.getGeoModel().getBakedModel(armorRenderer.getGeoModel().getModelResource(geoAnimatable));
            armorRenderer.scaleModelForBaby(stack, (Item) geoAnimatable, partialTick, isReRender);
            armorRenderer.scaleModelForRender(((GeckolibGeoArmorAccessor) armorRenderer).figura$getScaleWidth(), ((GeckolibGeoArmorAccessor) armorRenderer).figura$getScaleHeight(), stack, geoAnimatable, model, isReRender, partialTick, packedLight, packedOverlay);

            stack.translate(0, 24 / 16f, 0);
            stack.scale(-1, -1, 1);

            ((GeckolibGeoArmorAccessor)armorRenderer).figura$setModelRenderTranslations(stack.last().pose());
            renderRecursively(stack, geoAnimatable, geoBone, renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
            stack.popPose();
            stack.popPose();
        });
    }

    @Unique
    private void figura$transformStackBasedOnType(PoseStack poseStack, ParentType parentType) {
        if (parentType == ParentType.LeftShoulderPivot) {
            poseStack.translate(-6 / 16f, 0f, 0f);
        }  else if (parentType == ParentType.RightShoulderPivot) {
            poseStack.translate(6 / 16f, 0f, 0f);
        } else if (parentType == ParentType.LeggingsPivot) {
            poseStack.translate(0, -12 / 16f, 0);
        } else if (parentType == ParentType.LeftLeggingPivot) {
            poseStack.translate(-2 / 16f, -12 / 16f, 0);
        } else if (parentType == ParentType.RightLeggingPivot) {
            poseStack.translate(2 / 16f, -12 / 16f, 0);
        } else if (parentType == ParentType.LeftBootPivot) {
            poseStack.translate(-2 / 16f, -24 / 16f, 0);
        } else if (parentType == ParentType.RightBootPivot) {
            poseStack.translate(2 / 16f, -24 / 16f, 0);
        }
    }

    @Unique
    private void figura$prepareArmorRender(PoseStack stack) {
        stack.scale(16, 16, 16);
        stack.mulPose(Axis.XP.rotationDegrees(180f));
        stack.mulPose(Axis.YP.rotationDegrees(180f));
    }
}
