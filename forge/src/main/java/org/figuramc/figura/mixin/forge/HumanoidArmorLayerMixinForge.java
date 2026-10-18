package org.figuramc.figura.mixin.forge;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.compat.GeckoLibCompat;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.mixin.render.layers.EquipmentLayerRendererAccessor;
import org.figuramc.figura.mixin.render.layers.HumanoidArmorLayerAccessor;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.FiguraArmorPartRenderer;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(value = HumanoidArmorLayer.class, priority = 900)
public abstract class HumanoidArmorLayerMixinForge<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> extends RenderLayer<S, M> implements HumanoidArmorLayerAccessor<S, M, A> {
    @Shadow @Final private EquipmentLayerRenderer equipmentRenderer;

    @Shadow protected abstract A getArmorModel(S humanoidRenderState, EquipmentSlot equipmentSlot);

    @Shadow protected abstract net.minecraft.client.model.Model<?> getArmorModel(S state, EquipmentSlot slot, ItemStack stack, A defaultModel);

    @Shadow
    protected abstract void renderArmorPiece(PoseStack matrices, SubmitNodeCollector submitNodeCollector, ItemStack stack, EquipmentSlot armorSlot, int light, S state);

    @Unique
    private boolean figura$renderingVanillaArmor;

    @Unique
    private Avatar figura$avatar;

    public HumanoidArmorLayerMixinForge(RenderLayerParent<S, M> context) {
        super(context);
    }

    @Inject(at = @At(value = "HEAD"), method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V")
    public void setAvatar(PoseStack matrices, SubmitNodeCollector submitNodeCollector, int i, S humanoidRenderState, float f, float g, CallbackInfo ci) {
        figura$avatar = AvatarManager.getAvatar(humanoidRenderState);
    }

    @Inject(at = @At("TAIL"), method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V")
    public void onRenderEnd(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S humanoidRenderState, float f, float g, CallbackInfo ci) {
        if (figura$avatar == null) return;

        figura$tryRenderArmorPart(EquipmentSlot.HEAD,  this::figura$helmetRenderer, poseStack, humanoidRenderState, submitNodeCollector, i, ParentType.HelmetPivot);
        figura$tryRenderArmorPart(EquipmentSlot.CHEST, this::figura$chestplateRenderer, poseStack, humanoidRenderState, submitNodeCollector, i, ParentType.LeftShoulderPivot, ParentType.ChestplatePivot, ParentType.RightShoulderPivot);
        figura$tryRenderArmorPart(EquipmentSlot.LEGS,  this::figura$leggingsRenderer, poseStack, humanoidRenderState, submitNodeCollector, i, ParentType.LeftLeggingPivot, ParentType.RightLeggingPivot, ParentType.LeggingsPivot);
        figura$tryRenderArmorPart(EquipmentSlot.FEET,  this::figura$bootsRenderer, poseStack, humanoidRenderState, submitNodeCollector, i, ParentType.LeftBootPivot, ParentType.RightBootPivot);
    }

    @Unique
    private void addFiguraCallbacks(EquipmentSlot equipmentSlot, A humanoidModel) {
        if (figura$avatar == null) return;
        Avatar localAvatar = figura$avatar;

        var extension = (FiguraSubmitCallBackExtension)humanoidModel;
        extension.figura$addPreRenderingCallback((poseStack, nodeCollector) -> {
            VanillaPart part = RenderUtils.partFromSlot(localAvatar, equipmentSlot);
            if (part != null) {
                part.save(humanoidModel);
                part.preTransform(humanoidModel);
                part.posTransform(humanoidModel);
            }
            return true;
        });

        extension.figura$addPostRenderingCallback(() -> {
            VanillaPart part = RenderUtils.partFromSlot(localAvatar, equipmentSlot);
            if (part != null)
                part.restore(humanoidModel);
        });
    }


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;II)V"), method = "renderArmorPiece", cancellable = true)
    public void renderArmorPieceHijack(PoseStack matrices, SubmitNodeCollector submitNodeCollector, ItemStack stack, EquipmentSlot armorSlot, int light, S state, CallbackInfo ci, @Local(name = "newModel") net.minecraft.client.model.Model<?> actualModel) {
        if (figura$avatar == null) return;

        if (!figura$renderingVanillaArmor) {
            ci.cancel();
            return;
        }
        if (actualModel instanceof HumanoidModel<?> humanoidModel) {
            figura$setPartVisibility((A) humanoidModel, armorSlot);
            addFiguraCallbacks(armorSlot, (A) humanoidModel);
        }
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "renderArmorPiece",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;II)V"))
    private <R> void figura$snapshotArmorLayers(EquipmentLayerRenderer renderer, EquipmentClientInfo.LayerType type,
            ResourceKey<EquipmentAsset> asset, net.minecraft.client.model.Model<? super R> model, R state,
            ItemStack item, PoseStack pose, SubmitNodeCollector collector, int light, int outline,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        org.figuramc.figura.model.rendering.nodeRenderer.FiguraFeatureRenderer.withModelCallbacks(
                model, state, collector, pose,
                () -> original.call(renderer, type, asset, model, state, item, pose, collector, light, outline));
    }
    @Unique
    private void figura$tryRenderArmorPart(EquipmentSlot slot, FiguraArmorPartRenderer<S, A> renderer, PoseStack vanillaPoseStack, S state, SubmitNodeCollector submitNodeCollector, int light, ParentType... parentTypes) {
        if (slot == null) return; // ?
        Integer id = state instanceof AvatarRenderState playerRenderState ? playerRenderState.id : ((FiguraEntityRenderStateExtension)state).figura$getEntityId();
        if (id == null) return;
        ItemStack itemStack = ((LivingEntity)(Minecraft.getInstance().level.getEntity(id))).getItemBySlot(slot);

        // Make sure the item in the equipment slot is actually a piece of armor
        if ((itemStack.getItem() instanceof Item armorItem && armorItem.components().has(DataComponents.EQUIPPABLE) && armorItem.components().get(DataComponents.EQUIPPABLE).slot() == slot && armorItem.components().has(DataComponents.ATTRIBUTE_MODIFIERS) && armorItem.components().get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().stream().anyMatch(attribute -> attribute.attribute() == Attributes.ARMOR))) {
            VanillaPart mainPart = RenderUtils.partFromSlot(figura$avatar, slot);
            int armorEditPermission = figura$avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT);
            if (armorEditPermission == 1 && mainPart != null && !mainPart.checkVisible()) return;

            var customModel = getArmorModel(state, slot, itemStack, getArmorModel(state, slot));
            if (!(customModel instanceof HumanoidModel<?>)) {
                figura$renderingVanillaArmor = true;
                try {
                    renderArmorPiece(vanillaPoseStack, submitNodeCollector, itemStack, slot, light, state);
                } finally {
                    figura$renderingVanillaArmor = false;
                }
                return;
            }
            A armorModel = (A) customModel;

            // Bones have to be their defaults to prevent issues with clipping
            armorModel.body.xRot = 0.0f;
            armorModel.rightLeg.z = 0.0f;
            armorModel.leftLeg.z = 0.0f;
            armorModel.rightLeg.y = 12.0f;
            armorModel.leftLeg.y = 12.0f;
            armorModel.head.y = 0.0f;
            armorModel.body.y = 0.0f;
            armorModel.leftArm.y = 2.0f;
            armorModel.rightArm.y = 2.0f;
            armorModel.leftArm.x = 5.0f;
            armorModel.rightArm.x = -5.0f;
            armorModel.leftArm.z = 0.0f;
            armorModel.rightArm.z = 0.0f;

            boolean allFailed = true;

            // Don't render armor if GeckoLib is already doing the rendering
            if (!GeckoLibCompat.armorHasCustomModel(itemStack, slot, slot == EquipmentSlot.LEGS ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS : EquipmentClientInfo.LayerType.HUMANOID)) {
                // Go through each parent type needed to render the current piece of armor
                for (ParentType parentType : parentTypes) {
                    // Skip the part if it's hidden
                    VanillaPart part = RenderUtils.pivotToPart(figura$avatar, parentType);
                    if (armorEditPermission == 1 && part != null && !part.checkVisible()) continue;
                    boolean renderedPivot = false;
                    // If the user has no permission disable pivot
                    if (armorEditPermission == 1) {
                        // Try to render the pivot part
                        renderedPivot = figura$avatar.pivotPartRender(parentType, stack -> {
                            stack.pushPose();
                            figura$prepareArmorRender(stack);
                            renderer.renderArmorPart(stack, submitNodeCollector, light, armorModel, itemStack, slot, parentType);
                            stack.popPose();
                        });
                    }
                    if (renderedPivot) {
                        allFailed = false;
                    }
                }
            }
            // As a fallback, render armor the vanilla way
            if (allFailed) {
                figura$renderingVanillaArmor = true;
                renderArmorPiece(vanillaPoseStack, submitNodeCollector, itemStack, slot, light, state);
                figura$renderingVanillaArmor = false;
            }
        }

    }

    // Prepare the transformations for rendering armor on the avatar
    @Unique
    private void figura$prepareArmorRender(PoseStack stack) {
        stack.scale(16, 16, 16);
        stack.rotate(Axis.XP.rotationDegrees(180f));
        stack.rotate(Axis.YP.rotationDegrees(180f));
    }

    @Unique
    private void figura$helmetRenderer(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, A model, ItemStack itemStack, EquipmentSlot armorSlot, ParentType parentType) {
        if (parentType == ParentType.HelmetPivot) {
            figura$renderArmorPart(model.head, poseStack, submitNodeCollector, light, itemStack, armorSlot);
            figura$renderArmorPart(model.hat, poseStack, submitNodeCollector, light, itemStack, armorSlot);
        }
    }

    @Unique
    private void figura$chestplateRenderer(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, A model, ItemStack itemStack, EquipmentSlot armorSlot, ParentType parentType) {
        if (parentType == ParentType.ChestplatePivot) {
            figura$renderArmorPart(model.body, poseStack, submitNodeCollector, light, itemStack, armorSlot);
        }

        if (parentType == ParentType.LeftShoulderPivot) {
            poseStack.pushPose();
            poseStack.translate(-6 / 16f, 0f, 0f);
            figura$renderArmorPart(model.leftArm, poseStack, submitNodeCollector, light, itemStack, armorSlot);
            poseStack.popPose();
        }

        if (parentType == ParentType.RightShoulderPivot) {
            poseStack.pushPose();
            poseStack.translate(6 / 16f, 0f, 0f);
            figura$renderArmorPart(model.rightArm, poseStack, submitNodeCollector, light, itemStack, armorSlot);
            poseStack.popPose();
        }
    }

    @Unique
    private void figura$leggingsRenderer(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, A model, ItemStack itemStack, EquipmentSlot armorSlot, ParentType parentType) {
        if (parentType == ParentType.LeggingsPivot) {
            poseStack.pushPose();
            poseStack.translate(0, -12 / 16f, 0);
            figura$renderArmorPart(model.body, poseStack, submitNodeCollector, light, itemStack, armorSlot);
            poseStack.popPose();
        }

        if (parentType == ParentType.LeftLeggingPivot) {
            poseStack.pushPose();
            poseStack.translate(-2 / 16f, -12 / 16f, 0);
            figura$renderArmorPart(model.leftLeg, poseStack, submitNodeCollector, light, itemStack, armorSlot);
            poseStack.popPose();
        }

        if (parentType == ParentType.RightLeggingPivot) {
            poseStack.pushPose();
            poseStack.translate(2 / 16f, -12 / 16f, 0);
            figura$renderArmorPart(model.rightLeg, poseStack, submitNodeCollector, light, itemStack, armorSlot);
            poseStack.popPose();
        }
    }

    @Unique
    private void figura$bootsRenderer(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, A model, ItemStack itemStack, EquipmentSlot armorSlot, ParentType parentType) {
        if (parentType == ParentType.LeftBootPivot) {
            poseStack.pushPose();
            poseStack.translate(-2 / 16f, -24 / 16f, 0);
            figura$renderArmorPart(model.leftLeg, poseStack, submitNodeCollector, light, itemStack, armorSlot);
            poseStack.popPose();
        }

        if (parentType == ParentType.RightBootPivot) {
            poseStack.pushPose();
            poseStack.translate(2 / 16f, -24 / 16f, 0);
            figura$renderArmorPart(model.rightLeg, poseStack, submitNodeCollector, light, itemStack, armorSlot);
            poseStack.popPose();
        }
    }


    // Similar to vanilla's renderArmorModel, but it renders each part individually, instead of the whole model at once.
    // Could be optimized by calculating the tint, overlays, and trims beforehand instead of re-calculating for each ModelPart, but it's not super important.
    @Unique
    private void figura$renderArmorPart(ModelPart modelPart, PoseStack poseStack, SubmitNodeCollector nodeCollector, int light, ItemStack itemStack, EquipmentSlot armorSlot) {
        boolean hasGlint = itemStack.hasFoil();

        modelPart.visible = true;
        modelPart.xRot = 0;
        modelPart.yRot = 0;
        modelPart.zRot = 0;
        EquipmentClientInfo.LayerType layerType = this.figura$usesInnerModel(armorSlot) ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS : EquipmentClientInfo.LayerType.HUMANOID;
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);

        if (equippable == null)
            return;

        Optional<ResourceKey<EquipmentAsset>> location = equippable.assetId();
        if (location.isEmpty())
            return;

        List<EquipmentClientInfo.Layer> list = ((EquipmentLayerRendererAccessor)this.equipmentRenderer).figura$getAssetsManager().get(location.get()).getLayers(layerType);

        int i = DyedItemColor.getOrDefault(itemStack, 0);
        int order = 0;

        for(EquipmentClientInfo.Layer layer : list) {
            int k = EquipmentLayerRendererAccessor.figura$getColorForLayer(layer, i);

            if (k != 0) {
                Identifier normalArmorResource = ((EquipmentLayerRendererAccessor)this.equipmentRenderer).layerTextureLookup().apply(new EquipmentLayerRenderer.LayerTextureKey(layerType, layer));
                ((FiguraSubmitCallBackExtension) (Object) modelPart).figura$addPreRenderingCallback((collector, pose) -> true);
                RenderType armorType = hasGlint ? RenderTypes.armorCutoutNoCullGlint(normalArmorResource)
                        : RenderTypes.armorCutoutNoCull(normalArmorResource);
                nodeCollector.order(order++).submitModelPart(modelPart, poseStack, armorType, light, OverlayTexture.NO_OVERLAY, null, k, 0);
                hasGlint = false;
            }
        }

        ArmorTrim trim = itemStack.get(DataComponents.TRIM);
        if (trim != null) {
            net.minecraft.client.resources.palette.PalettedTextureManager.Handle textureAtlasSprite = ((EquipmentLayerRendererAccessor)equipmentRenderer).trimTextureLookup()
                    .apply(new EquipmentLayerRenderer.TrimTextureKey(trim, layerType, ((EquipmentLayerRendererAccessor)equipmentRenderer).figura$getAssetsManager().get(location.get())));

            RenderType renderType = RenderTypes.armorTrim(textureAtlasSprite.textureLocation(), trim.pattern().value().decal());
            ((FiguraSubmitCallBackExtension) (Object) modelPart).figura$addPreRenderingCallback((collector, pose) -> true);
            nodeCollector.order(order++).submitModelPart(modelPart, poseStack, renderType, light, OverlayTexture.NO_OVERLAY, textureAtlasSprite, -1, 0);
            if (itemStack.hasFoil()) {
                ((FiguraSubmitCallBackExtension) (Object) modelPart).figura$addPreRenderingCallback((collector, pose) -> true);
                nodeCollector.order(order).submitModelPart(modelPart, poseStack, RenderTypes.trimmedArmorGlint(),
                        light, OverlayTexture.NO_OVERLAY, null, -1, 0);
            }
        }
    }

    @Unique
    protected void figura$setPartVisibility(A bipedModel, EquipmentSlot slot) {
        bipedModel.head.visible = false;
        bipedModel.hat.visible = false;
        bipedModel.body.visible = false;
        bipedModel.leftArm.visible = false;
        bipedModel.rightArm.visible = false;
        bipedModel.leftLeg.visible = false;
        bipedModel.rightLeg.visible = false;
        switch (slot) {
            case HEAD:
                bipedModel.head.visible = true;
                bipedModel.hat.visible = true;
                break;
            case CHEST:
                bipedModel.body.visible = true;
                bipedModel.rightArm.visible = true;
                bipedModel.leftArm.visible = true;
                break;
            case LEGS:
                bipedModel.body.visible = true;
                bipedModel.rightLeg.visible = true;
                bipedModel.leftLeg.visible = true;
                break;
            case FEET:
                bipedModel.rightLeg.visible = true;
                bipedModel.leftLeg.visible = true;
        }
    }
}
