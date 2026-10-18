package org.figuramc.figura.mixin.compat;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.item.Item;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Pseudo
@Mixin(value = GeoArmorRenderer.class, remap = false)
public abstract class GeckolibGeoArmorRendererMixin<T extends Item & GeoItem> {
    @Unique
    private static final DataTicket<Avatar> FIGURA_AVATAR = DataTicket.create("figura:armor_avatar", Avatar.class);

    @Inject(method = "captureDefaultRenderState(Lnet/minecraft/world/item/Item;Lcom/geckolib/renderer/GeoArmorRenderer$RenderData;Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;F)V", at = @At("TAIL"))
    private <R extends HumanoidRenderState & GeoRenderState> void figura$prepAvatar(T animatable, GeoArmorRenderer.RenderData renderData, R renderState, float partialTick, CallbackInfo ci) {
        // Armor renderers are shared; keep the wearer on the individual render state.
        Avatar avatar = renderData.entity() == null ? null : AvatarManager.getAvatar(renderData.entity());
        renderState.getDataMap().remove(FIGURA_AVATAR);
        if (avatar != null)
            renderState.addGeckolibData(FIGURA_AVATAR, avatar);
    }

    @WrapOperation(method = "submitRenderTasks", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitCustomGeometry(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/SubmitNodeCollector$CustomGeometryRenderer;)V"))
    private <R extends HumanoidRenderState & GeoRenderState> void figura$submitArmorPivots(
            OrderedSubmitNodeCollector collector, PoseStack poseStack, RenderType renderType,
            SubmitNodeCollector.CustomGeometryRenderer nativeRenderer, Operation<Void> original,
            @Local(argsOnly = true) RenderPassInfo<R> pass) {
        Avatar avatar = pass.getGeckolibData(FIGURA_AVATAR);
        if (AvatarManager.panic || avatar == null || !avatar.loaded || avatar.renderer == null
                || avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) != 1) {
            original.call(collector, poseStack, renderType, nativeRenderer);
            return;
        }

        var slot = pass.getGeckolibData(GeoArmorRenderer.CURRENT_SLOT);
        VanillaPart mainPart = RenderUtils.partFromSlot(avatar, slot);
        if (mainPart != null && !mainPart.checkVisible())
            return;

        var armorRenderer = (GeoArmorRenderer<T, R>) (Object) this;
        int light = pass.packedLight(), overlay = pass.packedOverlay(), color = pass.renderColor();
        List<Consumer<VertexConsumer>> parts = new ArrayList<>();
        boolean changed = false;
        for (GeoArmorRenderer.ArmorSegment segment : armorRenderer.getSegmentsForSlot(pass.renderState(), slot)) {
            GeoBone bone = pass.model().getBone(armorRenderer.getBoneNameForSegment(pass.renderState(), segment)).orElse(null);
            if (bone == null)
                continue;

            ParentType parent = switch (segment) {
                case HEAD -> ParentType.HelmetPivot;
                case CHEST -> ParentType.ChestplatePivot;
                case LEFT_ARM -> ParentType.LeftShoulderPivot;
                case RIGHT_ARM -> ParentType.RightShoulderPivot;
                case LEFT_LEG -> ParentType.LeftLeggingPivot;
                case RIGHT_LEG -> ParentType.RightLeggingPivot;
                case LEFT_FOOT -> ParentType.LeftBootPivot;
                case RIGHT_FOOT -> ParentType.RightBootPivot;
            };
            VanillaPart part = RenderUtils.pivotToPart(avatar, parent);
            if (part != null && !part.checkVisible()) {
                changed = true;
                continue;
            }

            // The avatar's pivot queue and global PoseStack may be reused before GPU playback.
            List<PoseStack.Pose> pivots = new ArrayList<>();
            avatar.pivotPartRender(parent, stack -> pivots.add(stack.last().copy()));
            if (pivots.isEmpty()) {
                parts.add(vertices -> bone.positionAndRender(pass, vertices, light, overlay, color));
            } else {
                changed = true;
                for (PoseStack.Pose pivot : pivots)
                    parts.add(vertices -> figura$renderPivot(pass, bone, parent, pivot, vertices, light, overlay, color));
            }
        }
        if (!changed) {
            original.call(collector, poseStack, renderType, nativeRenderer);
            return;
        }

        SubmitNodeCollector.CustomGeometryRenderer renderer = (pose, vertices) -> {
            PoseStack stack = pass.poseStack();
            stack.pushPose();
            try {
                stack.last().set(pose);
                pass.renderPosed(() -> parts.forEach(part -> part.accept(vertices)));
            } finally {
                stack.popPose();
            }
        };
        original.call(collector, poseStack, renderType, renderer);
    }

    @Unique
    private static <R extends GeoRenderState> void figura$renderPivot(RenderPassInfo<R> pass, GeoBone bone,
            ParentType parent, PoseStack.Pose pivot, VertexConsumer vertices, int light, int overlay, int color) {
        PoseStack stack = pass.poseStack();
        PoseStack.Pose preRenderPose = pass.getPreRenderMatrixPose().copy();
        PoseStack.Pose modelRenderPose = pass.getModelRenderMatrixPose().copy();
        BoneSnapshot previous = bone.frameSnapshot;
        BoneSnapshot snapshot = BoneSnapshot.create(bone);
        if (previous != null) {
            snapshot.setScale(previous.getScaleX(), previous.getScaleY(), previous.getScaleZ());
            snapshot.setTranslation(previous.getTranslateX(), previous.getTranslateY(), previous.getTranslateZ());
            snapshot.skipRender(previous.isHidden()).skipChildrenRender(previous.areChildrenHidden());
        }
        // Figura already supplies the root rotation; keep child animation and root scale intact.
        snapshot.setRotation(-bone.baseRotX(), -bone.baseRotY(), -bone.baseRotZ());
        if (parent == ParentType.LeftShoulderPivot || parent == ParentType.RightShoulderPivot)
            snapshot.setTranslation(0, 0, 0);

        stack.pushPose();
        try {
            stack.last().set(pivot);
            stack.scale(16, 16, 16);
            stack.rotateDegrees(Axis.XP, 180);
            stack.rotateDegrees(Axis.YP, 180);
            switch (parent) {
                case LeftShoulderPivot -> stack.translate(-6 / 16f, 0, 0);
                case RightShoulderPivot -> stack.translate(6 / 16f, 0, 0);
                case LeftLeggingPivot -> stack.translate(-2 / 16f, -12 / 16f, 0);
                case RightLeggingPivot -> stack.translate(2 / 16f, -12 / 16f, 0);
                case LeftBootPivot -> stack.translate(-2 / 16f, -24 / 16f, 0);
                case RightBootPivot -> stack.translate(2 / 16f, -24 / 16f, 0);
                default -> {}
            }
            pass.getPreRenderMatrixPose().set(stack.last());
            pass.renderer().scaleModelForRender(pass, 1, 1);
            pass.renderer().adjustRenderPose(pass);
            pass.captureModelRenderPose();
            snapshot.apply();
            bone.positionAndRender(pass, vertices, light, overlay, color);
        } finally {
            bone.frameSnapshot = previous;
            pass.getPreRenderMatrixPose().set(preRenderPose);
            pass.getModelRenderMatrixPose().set(modelRenderPose);
            stack.popPose();
        }
    }
}
