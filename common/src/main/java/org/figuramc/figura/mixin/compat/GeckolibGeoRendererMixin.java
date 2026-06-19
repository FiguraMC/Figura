package org.figuramc.figura.mixin.compat;

import org.spongepowered.asm.mixin.*;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;

//TODO :FIX THIS
@Pseudo
@Mixin(value = GeoRenderer.class, remap = false)
public interface GeckolibGeoRendererMixin<R extends GeoRenderState> {

/*    @Shadow
    void renderRecursively(R renderState, PoseStack poseStack, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, int packedLight, int packedOverlay, int renderColor);
*/
    /**
     * @author UnlikePaladin
     * @reason Upstream Sponge Mixin, that is anything that's not Fabric's fork doesn't support interface injection so we have to overwrite :(
     *  The functionality is the same as geckolib's but calls our pivots first
     */
  /*  @Overwrite
    default void actuallyRender(R renderState, PoseStack poseStack, BakedGeoModel model, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, int packedLight, int packedOverlay, int renderColor) {
        if (buffer == null) {
            if (renderType == null)
                return;

            buffer = bufferSource.getBuffer(renderType);
        }

        CallbackInfo callbackInfo = new CallbackInfo("figura$renderPivots", true);
        figura$renderPivots(renderState, poseStack, model, renderType, bufferSource, buffer, isReRender, packedLight, packedOverlay, renderColor, callbackInfo);
        if (callbackInfo.isCancelled())
            return;

        for (GeoBone group : model.topLevelBones()) {
            renderRecursively(renderState, poseStack, group, renderType, bufferSource, buffer, isReRender, packedLight, packedOverlay, renderColor);
        }
    }

    @Unique
    default <T extends HumanoidRenderState & GeoRenderState> void figura$renderPivots(R ogState, PoseStack poseStack, BakedGeoModel bakedGeoModel, RenderType renderType, MultiBufferSource multiBufferSource, VertexConsumer vertexConsumer, boolean isReRender, int packedLight, int packedOverlay, int color, CallbackInfo ci){
        boolean allFailed = true;
        // If the renderer is an armor renderer and the avatar is not null
        if (this instanceof GeoArmorRenderer && ((GeckolibGeoArmorAccessor) this).figura$getAvatar() != null) {
            T casted = (T) ogState;
            GeoArmorRenderer armorRenderer = (GeoArmorRenderer) this;
            EquipmentSlot slot = casted.getGeckolibData(DataTickets.EQUIPMENT_SLOT);
            if (slot == null) return; // ?
            Avatar avatar = ((GeckolibGeoArmorAccessor)armorRenderer).figura$getAvatar();
            // Check the user can edit the model
            if (avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) < 1) return;
            GeoModel<?> model = armorRenderer.getGeoModel();
            GeckolibGeoArmorAccessor armorAccessor = (GeckolibGeoArmorAccessor) armorRenderer;
            // Render the pivot depending on the current slot
            switch (slot) {
                case HEAD:
                    allFailed = figura$renderPivot(ogState, armorRenderer, avatar, ParentType.HelmetPivot, armorAccessor.figura$getHeadBone(), renderType, multiBufferSource, vertexConsumer, isReRender, packedLight, packedOverlay, color);
                    if (allFailed)
                        renderRecursively(ogState, poseStack, armorAccessor.figura$getHeadBone(), renderType, multiBufferSource, vertexConsumer, isReRender, packedLight, packedOverlay, color);
                    break;
                case CHEST:
                    allFailed = figura$renderPivot(ogState, armorRenderer, avatar, ParentType.ChestplatePivot, armorAccessor.figura$getBodyBone(), renderType, multiBufferSource, vertexConsumer, isReRender, packedLight, packedOverlay, color);
                    if (allFailed)
                        renderRecursively(ogState, poseStack, armorAccessor.figura$getBodyBone(), renderType, multiBufferSource, vertexConsumer, isReRender, packedLight, packedOverlay, color);
                    allFailed = figura$renderPivot(ogState, armorRenderer, avatar, ParentType.LeftShoulderPivot, armorAccessor.figura$getLeftArmBone(), renderType, multiBufferSource, vertexConsumer, isReRender, packedLight, packedOverlay, color);
                    if (allFailed)
                        renderRecursively(ogState, poseStack, armorAccessor.figura$getLeftArmBone(), renderType, multiBufferSource, vertexConsumer, isReRender, packedLight, packedOverlay, color);
                    allFailed = figura$renderPivot(ogState, armorRenderer, avatar, ParentType.RightShoulderPivot, armorAccessor.figura$getRightArmBone(), renderType, multiBufferSource, vertexConsumer, isReRender, packedLight, packedOverlay, color);
                    if (allFailed)
                        renderRecursively(ogState, poseStack, armorAccessor.figura$getRightArmBone(), renderType, multiBufferSource, vertexConsumer, isReRender, packedLight, packedOverlay, color);
                    break;
                case LEGS:
                    allFailed = figura$renderPivot(ogState, armorRenderer, avatar, ParentType.LeftLeggingPivot, armorAccessor.figura$getLeftLegBone(), renderType, multiBufferSource, vertexConsumer, isReRender, packedLight, packedOverlay, color);
                    if (allFailed)
                        renderRecursively(ogState, poseStack, armorAccessor.figura$getLeftLegBone(), renderType, multiBufferSource, vertexConsumer, isReRender, packedLight, packedOverlay, color);
                    figura$renderPivot(ogState, armorRenderer, avatar, ParentType.RightLeggingPivot, armorAccessor.figura$getRightLegBone(), renderType, multiBufferSource, vertexConsumer, isReRender, packedLight, packedOverlay, color);
                    if (allFailed)
                        renderRecursively(ogState, poseStack, armorAccessor.figura$getRightLegBone(), renderType, multiBufferSource, vertexConsumer, isReRender, packedLight, packedOverlay, color);
                    break;
                case FEET:
                    allFailed = figura$renderPivot(ogState, armorRenderer, avatar, ParentType.LeftBootPivot, armorAccessor.figura$getLeftBootBone(), renderType, multiBufferSource, vertexConsumer, isReRender, packedLight, packedOverlay, color);
                    if (allFailed)
                        renderRecursively(ogState, poseStack, armorAccessor.figura$getLeftBootBone(), renderType, multiBufferSource, vertexConsumer, isReRender, packedLight, packedOverlay, color);
                    allFailed = figura$renderPivot(ogState, armorRenderer, avatar, ParentType.RightBootPivot, armorAccessor.figura$getRightBootBone(), renderType, multiBufferSource, vertexConsumer, isReRender, packedLight, packedOverlay, color);
                    if (allFailed)
                        renderRecursively(ogState, poseStack, armorAccessor.figura$getRightBootBone(), renderType, multiBufferSource, vertexConsumer, isReRender, packedLight, packedOverlay, color);
                    break;
                default:
                    break;
            }
            ci.cancel();
        }
    }

    // Returns true if the pivot failed to render, false if it was successful to match HumanoidArmorLayerMixin
    @Unique
    default <T extends HumanoidRenderState & GeoRenderState> boolean figura$renderPivot(R renderState, GeoArmorRenderer armorRenderer, Avatar avatar, ParentType parentType, GeoBone geoBone, RenderType renderType, MultiBufferSource multiBufferSource, VertexConsumer vertexConsumer, boolean isReRender, int packedLight, int packedOverlay, int color) {
        if (geoBone == null)
            return true;

        int armorEditPermission = avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT);
        // Returns successfully but skips rendering if the part is hidden
        VanillaPart part = RenderUtils.pivotToPart(avatar, parentType);
        if (armorEditPermission == 1 && part != null && !part.checkVisible())
            return false;

        // If the user has no permission disable pivots
        if (armorEditPermission != 1)
            return true;

        return !avatar.pivotPartRender(parentType, stack -> {
            geoBone.setRotX(0);
            geoBone.setRotY(0);
            geoBone.setRotZ(0);

            stack.pushPose();
            figura$prepareArmorRender(stack);
            figura$transformBasedOnType(geoBone, stack, parentType);

            ((GeckolibGeoArmorAccessor)armorRenderer).figura$setEntityRenderTranslations(stack.last().pose());

            stack.pushPose();
            BakedGeoModel model = armorRenderer.getGeoModel().getBakedModel(armorRenderer.getGeoModel().getModelResource(renderState));
            T casted = (T) renderState;
            ((GeoArmorRenderer<?, T>) armorRenderer).scaleModelForRender(casted, ((GeckolibGeoArmorAccessor) armorRenderer).figura$getScaleWidth(), ((GeckolibGeoArmorAccessor) armorRenderer).figura$getScaleHeight(), stack, model, isReRender);

            stack.translate(0, 24 / 16f, 0);
            stack.scale(-1, -1, 1);

            ((GeckolibGeoArmorAccessor)armorRenderer).figura$setModelRenderTranslations(stack.last().pose());
            renderRecursively(renderState, stack, geoBone, renderType, multiBufferSource, vertexConsumer, isReRender, packedLight, packedOverlay, color);
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
    }*/
}
