package org.figuramc.figura.mixin.fabric.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.fabric.GeckolibGeoArmorAccessor;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.RenderUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import software.bernie.geckolib3.util.EModelRenderCycle;
import software.bernie.geckolib3.util.IRenderCycle;

@Pseudo
@Mixin(value = IGeoRenderer.class, remap = false)
public interface GeckolibIGeoRendererMixin<T> {


    @Shadow void renderRecursively(GeoBone bone, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha);


    @Shadow void setCurrentRTB(MultiBufferSource bufferSource);

    @Shadow void renderEarly(T animatable, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, VertexConsumer buffer, int packedLight, int packedOverlayIn, float red, float green, float blue, float alpha);

    @Shadow void renderLate(T animatable, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha);

    @Shadow void setCurrentModelRenderCycle(IRenderCycle cycle);

    /**
     * @author UnlikePaladin
     * @reason Upstream Sponge Mixin, that is anything that's not Fabric's fork doesn't support interface injection so we have to overwrite :(
     *  The functionality is the same as geckolib's but calls our pivots first
     */
    @Overwrite
    default void render(GeoModel model, T animatable, float partialTick, RenderType type, PoseStack poseStack,
                        @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight,
                        int packedOverlay, float red, float green, float blue, float alpha) {
        setCurrentRTB(bufferSource);
        renderEarly(animatable, poseStack, partialTick, bufferSource, buffer, packedLight,
                packedOverlay, red, green, blue, alpha);

        if (bufferSource != null)
            buffer = bufferSource.getBuffer(type);

        renderLate(animatable, poseStack, partialTick, bufferSource, buffer, packedLight,
                packedOverlay, red, green, blue, alpha);

        CallbackInfo callbackInfo = new CallbackInfo("figura$renderPivots", true);
        figura$renderPivots(poseStack, animatable, model, type, bufferSource, buffer, partialTick, packedLight, packedOverlay, red, green, blue, alpha, callbackInfo);
        if (!callbackInfo.isCancelled())
        {
            // Render all top level bones
            for (GeoBone group : model.topLevelBones) {
                renderRecursively(group, poseStack, buffer, packedLight, packedOverlay, red, green, blue,
                        alpha);
            }
        }
        // Since we rendered at least once at this point, let's set the cycle to
        // repeated
        setCurrentModelRenderCycle(EModelRenderCycle.REPEATED);
    }

    @Unique
    default void figura$renderPivots(PoseStack poseStack, T geoAnimatable, GeoModel model, RenderType renderType, MultiBufferSource multiBufferSource, VertexConsumer vertexConsumer, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha, CallbackInfo ci){
        boolean allFailed = true;

        // If the renderer is an armor renderer and the avatar is not null
        if (this instanceof GeoArmorRenderer<?> && ((GeckolibGeoArmorAccessor) this).figura$getAvatar() != null) {
            GeoArmorRenderer<?> armorRenderer = (GeoArmorRenderer<?>) this;
            if (((GeckolibGeoArmorAccessor)armorRenderer).figura$getSlot() == null) return; // ?
            Avatar avatar = ((GeckolibGeoArmorAccessor)armorRenderer).figura$getAvatar();
            AnimatedGeoModel animatedGeoModel = ((GeckolibGeoArmorAccessor) armorRenderer).figura$getAnimatedModelProvider();

            // Check the user can edit the model
            VanillaPart part = RenderUtils.partFromSlot(avatar, ((GeckolibGeoArmorAccessor) armorRenderer).figura$getSlot());
            if (avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1 && part != null && !part.checkVisible()) return;

            // Render the pivot depending on the current slot
            switch (((GeckolibGeoArmorAccessor)armorRenderer).figura$getSlot()) {
                case HEAD:
                    if (armorRenderer.headBone != null ) {
                        GeoBone headBone = (GeoBone) animatedGeoModel.getBone(armorRenderer.headBone);
                        allFailed = figura$renderPivot(armorRenderer, avatar, ParentType.HelmetPivot, geoAnimatable, headBone, renderType, multiBufferSource, vertexConsumer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                        if (allFailed)
                            renderRecursively(headBone, poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                    }
                    break;
                case CHEST:
                    if (armorRenderer.bodyBone != null ) {
                        GeoBone bodyBone = (GeoBone) animatedGeoModel.getBone(armorRenderer.bodyBone);
                        allFailed = figura$renderPivot(armorRenderer, avatar, ParentType.ChestplatePivot, geoAnimatable, bodyBone, renderType, multiBufferSource, vertexConsumer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                        if (allFailed)
                            renderRecursively(bodyBone, poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                    }
                    if (armorRenderer.leftArmBone != null ) {
                        GeoBone leftArmBone = (GeoBone) animatedGeoModel.getBone(armorRenderer.leftArmBone);
                        allFailed = figura$renderPivot(armorRenderer, avatar, ParentType.LeftShoulderPivot, geoAnimatable, leftArmBone, renderType, multiBufferSource, vertexConsumer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                        if (allFailed)
                            renderRecursively(leftArmBone, poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                    }
                    if (armorRenderer.rightArmBone != null) {
                        GeoBone rightArmBone = (GeoBone) animatedGeoModel.getBone(armorRenderer.rightArmBone);
                        allFailed = figura$renderPivot(armorRenderer, avatar, ParentType.RightShoulderPivot, geoAnimatable, rightArmBone, renderType, multiBufferSource, vertexConsumer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                        if (allFailed)
                            renderRecursively(rightArmBone, poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                    }
                    break;
                case LEGS:
                    if (armorRenderer.leftLegBone != null ) {
                        GeoBone leftLegBone = (GeoBone) animatedGeoModel.getBone(armorRenderer.leftLegBone);
                        allFailed = figura$renderPivot(armorRenderer, avatar, ParentType.LeftLeggingPivot, geoAnimatable, leftLegBone, renderType, multiBufferSource, vertexConsumer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                        if (allFailed)
                            renderRecursively(leftLegBone, poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                    }
                    if (armorRenderer.rightLegBone != null ) {
                        GeoBone rightLegBone = (GeoBone) animatedGeoModel.getBone(armorRenderer.rightLegBone);
                        allFailed = figura$renderPivot(armorRenderer, avatar, ParentType.RightLeggingPivot, geoAnimatable, rightLegBone, renderType, multiBufferSource, vertexConsumer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                        if (allFailed)
                            renderRecursively(rightLegBone, poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                    }
                    break;
                case FEET:
                    if (armorRenderer.leftBootBone != null ) {
                        GeoBone leftBootBone = (GeoBone) animatedGeoModel.getBone(armorRenderer.leftBootBone);
                        allFailed = figura$renderPivot(armorRenderer, avatar, ParentType.LeftBootPivot, geoAnimatable, leftBootBone, renderType, multiBufferSource, vertexConsumer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                        if (allFailed)
                            renderRecursively(leftBootBone, poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                    }
                    if (armorRenderer.rightBootBone != null ) {
                        GeoBone rightBootBone = (GeoBone) animatedGeoModel.getBone(armorRenderer.rightBootBone);
                        allFailed = figura$renderPivot(armorRenderer, avatar, ParentType.RightBootPivot, geoAnimatable, rightBootBone, renderType, multiBufferSource, vertexConsumer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                        if (allFailed)
                            renderRecursively(rightBootBone, poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                    }

                    break;
                default:
                    break;
            }
            ci.cancel();
        }
    }

    // Returns the true if the pivot failed to render to match HumanoidArmorLayerMixin
    @Unique
    default boolean figura$renderPivot(GeoArmorRenderer armorRenderer, Avatar avatar, ParentType parentType, T geoAnimatable, GeoBone geoBone, RenderType renderType, MultiBufferSource multiBufferSource, VertexConsumer vertexConsumer, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (geoBone == null)
            return true;

        return !avatar.pivotPartRender(parentType, stack -> {
            geoBone.setRotationX(0);
            geoBone.setRotationY(0);
            geoBone.setRotationZ(0);

            stack.pushPose();
            figura$prepareArmorRender(stack);
            figura$transformBasedOnType(geoBone, stack, parentType);

            stack.pushPose();

            stack.translate(0, 24 / 16f, 0);
            stack.scale(-1, -1, 1);

            renderRecursively(geoBone, stack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            stack.popPose();
            stack.popPose();
        });
    }

    // Based on the values from HumanoidArmorLayerMixin
    @Unique
    default void figura$transformBasedOnType(GeoBone bone, PoseStack poseStack, ParentType parentType) {
        // Arm Bones have to be moved to 0, as the vanilla hitting animation moves them, but we do too when copying the transforms, this fixes clipping issues
        if (parentType == ParentType.LeftShoulderPivot) {
            bone.setPositionY(0.0f);
            bone.setPositionZ(0.0f);
            bone.setPositionX(0.0f);
            poseStack.translate(-6 / 16f, 0f, 0f);
        }  else if (parentType == ParentType.RightShoulderPivot) {
            bone.setPositionY(0.0f);
            bone.setPositionZ(0.0f);
            bone.setPositionX(0.0f);
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
        stack.mulPose(Vector3f.XP.rotationDegrees(180f));
        stack.mulPose(Vector3f.YP.rotationDegrees(180f));
    }
}
