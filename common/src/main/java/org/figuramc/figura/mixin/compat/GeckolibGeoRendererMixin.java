package org.figuramc.figura.mixin.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Item;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.GeckolibGeoArmorAccessor;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;

@Pseudo
@Mixin(value = GeoRenderer.class, remap = false)
public interface GeckolibGeoRendererMixin<T extends GeoAnimatable> {

    @Shadow
    void renderRecursively(PoseStack par1, GeoAnimatable par2, GeoBone par3, RenderType par4, MultiBufferSource par5, VertexConsumer par6, boolean par7, float par8, int par9, int par10, float par11, float par12, float par13, float par14);

    @Shadow void updateAnimatedTextureFrame(T animatable);

    /**
     * @author UnlikePaladin
     * @reason Upstream Sponge Mixin, that is anything that's not Fabric's fork doesn't support interface injection so we have to overwrite :(
     *  The functionality is the same as geckolib's but calls our pivots first
     */
    @Overwrite
    default void actuallyRender(PoseStack poseStack, T animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        updateAnimatedTextureFrame(animatable);

        CallbackInfo callbackInfo = new CallbackInfo("figura$renderPivots", true);
        figura$renderPivots(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha, callbackInfo);
        if (callbackInfo.isCancelled())
            return;

        for (GeoBone group : model.topLevelBones()) {
            renderRecursively(poseStack, animatable, group, renderType, bufferSource, buffer, isReRender, partialTick, packedLight,
                    packedOverlay, red, green, blue, alpha);
        }
    }

    @Unique
    default void figura$renderPivots(PoseStack poseStack, GeoAnimatable geoAnimatable, BakedGeoModel model, RenderType renderType, MultiBufferSource multiBufferSource, VertexConsumer vertexConsumer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha, CallbackInfo ci){
        boolean allFailed = true;

        // If the renderer is an armor renderer and the avatar is not null
        if (this instanceof GeoArmorRenderer && ((GeckolibGeoArmorAccessor) this).figura$getAvatar() != null) {
            GeoArmorRenderer<?> armorRenderer = (GeoArmorRenderer<?>) this;
            if (armorRenderer.getCurrentSlot() == null) return; // ?
            Avatar avatar = ((GeckolibGeoArmorAccessor)armorRenderer).figura$getAvatar();

            // Check the user can edit the model
            if (avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) < 1) return;

            // Render the pivot depending on the current slot
            switch (armorRenderer.getCurrentSlot()) {
                case HEAD:
                    allFailed = figura$renderPivot(armorRenderer, avatar, ParentType.HelmetPivot, geoAnimatable, armorRenderer.getHeadBone(), renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                    if (allFailed)
                        renderRecursively(poseStack, geoAnimatable, armorRenderer.getHeadBone(), renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                    break;
                case CHEST:
                    allFailed = figura$renderPivot(armorRenderer, avatar, ParentType.ChestplatePivot, geoAnimatable, armorRenderer.getBodyBone(), renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                    if (allFailed)
                        renderRecursively(poseStack, geoAnimatable, armorRenderer.getBodyBone(), renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                    allFailed = figura$renderPivot(armorRenderer, avatar, ParentType.LeftShoulderPivot, geoAnimatable, armorRenderer.getLeftArmBone(), renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                    if (allFailed)
                        renderRecursively(poseStack, geoAnimatable, armorRenderer.getLeftArmBone(), renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                    allFailed = figura$renderPivot(armorRenderer, avatar, ParentType.RightShoulderPivot, geoAnimatable, armorRenderer.getRightArmBone(), renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                    if (allFailed)
                        renderRecursively(poseStack, geoAnimatable, armorRenderer.getRightArmBone(), renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                    break;
                case LEGS:
                    allFailed = figura$renderPivot(armorRenderer, avatar, ParentType.LeftLeggingPivot, geoAnimatable, armorRenderer.getLeftLegBone(), renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                    if (allFailed)
                        renderRecursively(poseStack, geoAnimatable, armorRenderer.getLeftLegBone(), renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                    figura$renderPivot(armorRenderer, avatar, ParentType.RightLeggingPivot, geoAnimatable, armorRenderer.getRightLegBone(), renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                    if (allFailed)
                        renderRecursively(poseStack, geoAnimatable, armorRenderer.getRightLegBone(), renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                    break;
                case FEET:
                    allFailed = figura$renderPivot(armorRenderer, avatar, ParentType.LeftBootPivot, geoAnimatable, armorRenderer.getLeftBootBone(), renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                    if (allFailed)
                        renderRecursively(poseStack, geoAnimatable, armorRenderer.getLeftBootBone(), renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                    allFailed = figura$renderPivot(armorRenderer, avatar, ParentType.RightBootPivot, geoAnimatable, armorRenderer.getRightBootBone(), renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                    if (allFailed)
                        renderRecursively(poseStack, geoAnimatable, armorRenderer.getRightBootBone(), renderType, multiBufferSource, vertexConsumer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                    break;
                default:
                    break;
            }
            ci.cancel();
        }
    }

    // Returns true if the pivot failed to render, false if it was successful to match HumanoidArmorLayerMixin
    @Unique
    default boolean figura$renderPivot(GeoArmorRenderer armorRenderer, Avatar avatar, ParentType parentType, GeoAnimatable geoAnimatable, GeoBone geoBone, RenderType renderType, MultiBufferSource multiBufferSource, VertexConsumer vertexConsumer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (geoBone == null)
            return true;

        // Returns successfully but skips rendering if the part is hidden
        VanillaPart part = RenderUtils.pivotToPart(avatar, parentType);
        if (avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1 && part != null && !part.checkVisible())
            return false;

        return !avatar.pivotPartRender(parentType, stack -> {
            geoBone.setRotX(0);
            geoBone.setRotY(0);
            geoBone.setRotZ(0);

            stack.pushPose();
            figura$prepareArmorRender(stack);
            figura$transformBasedOnType(geoBone, stack, parentType);

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

    // Based on the values from HumanoidArmorLayerMixin
    @Unique
    default void figura$transformBasedOnType(GeoBone bone, PoseStack poseStack, ParentType parentType) {
        // Arm Bones have to be moved to 0, as the vanilla hitting animation moves them, but we do too when copying the transforms, this fixes clipping issues
        if (parentType == ParentType.LeftShoulderPivot) {
            bone.setPosY(0.0f);
            bone.setPosZ(0.0f);
            bone.setPosX(0.0f);
            poseStack.translate(-6 / 16f, 0f, 0f);
        }  else if (parentType == ParentType.RightShoulderPivot) {
            bone.setPosY(0.0f);
            bone.setPosZ(0.0f);
            bone.setPosX(0.0f);
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
    default void figura$prepareArmorRender(PoseStack stack) {
        stack.scale(16, 16, 16);
        stack.mulPose(Axis.XP.rotationDegrees(180f));
        stack.mulPose(Axis.YP.rotationDegrees(180f));
    }
}
