package org.figuramc.figura.mixin.fabric;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
public abstract class HumanoidArmorLayerMixinFabric<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> extends RenderLayer<S, M> implements HumanoidArmorLayerAccessor<S, M, A> {
    @Shadow @Final private EquipmentLayerRenderer equipmentRenderer;

    @Shadow protected abstract A getArmorModel(S humanoidRenderState, EquipmentSlot equipmentSlot);

    @Shadow
    protected abstract void renderArmorPiece(PoseStack matrices, SubmitNodeCollector submitNodeCollector, ItemStack stack, EquipmentSlot armorSlot, int light, S state);

    @Unique
    private boolean figura$renderingVanillaArmor;

    @Unique
    private Avatar figura$avatar;

    public HumanoidArmorLayerMixinFabric(RenderLayerParent<S, M> context) {
        super(context);
    }

    @Inject(at = @At(value = "HEAD"), method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V")
    public void setAvatar(PoseStack matrices, SubmitNodeCollector submitNodeCollector, int i, S humanoidRenderState, float f, float g, CallbackInfo ci) {
        figura$avatar = AvatarManager.getAvatar(humanoidRenderState);
    }

    @Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 3, target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V"), method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V")
    public void onRenderEnd(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S humanoidRenderState, float f, float g, CallbackInfo ci) {
        if (figura$avatar == null) return;

        figura$tryRenderArmorPart(EquipmentSlot.HEAD,  this::figura$helmetRenderer, poseStack, humanoidRenderState, submitNodeCollector, i, ParentType.HelmetPivot);
        figura$tryRenderArmorPart(EquipmentSlot.CHEST, this::figura$chestplateRenderer, poseStack, humanoidRenderState, submitNodeCollector, i, ParentType.LeftShoulderPivot, ParentType.ChestplatePivot, ParentType.RightShoulderPivot);
        figura$tryRenderArmorPart(EquipmentSlot.LEGS,  this::figura$leggingsRenderer, poseStack, humanoidRenderState, submitNodeCollector, i, ParentType.LeftLeggingPivot, ParentType.RightLeggingPivot, ParentType.LeggingsPivot);
        figura$tryRenderArmorPart(EquipmentSlot.FEET,  this::figura$bootsRenderer, poseStack, humanoidRenderState, submitNodeCollector, i, ParentType.LeftBootPivot, ParentType.RightBootPivot);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;usesInnerModel(Lnet/minecraft/world/entity/EquipmentSlot;)Z"), method = "renderArmorPiece")
    public void onRenderArmorPiece(PoseStack matrices, SubmitNodeCollector submitNodeCollector, ItemStack stack, EquipmentSlot equipmentSlot, int light, S state, CallbackInfo ci) {
        if (figura$avatar == null) return;
        A humanoidModel = this.getArmorModel(state, equipmentSlot);

        VanillaPart part = RenderUtils.partFromSlot(figura$avatar, equipmentSlot);
        if (part != null) {
            part.save(humanoidModel);
            part.preTransform(humanoidModel);
            part.posTransform(humanoidModel);
        }
    }


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;usesInnerModel(Lnet/minecraft/world/entity/EquipmentSlot;)Z", shift = At.Shift.AFTER), method = "renderArmorPiece")
    public void renderArmorPieceHijack(PoseStack matrices, SubmitNodeCollector submitNodeCollector, ItemStack stack, EquipmentSlot armorSlot, int light, S state, CallbackInfo ci, @Local A humanoidModel) {
        if (figura$avatar != null && figura$renderingVanillaArmor) {
            return;
        }
        figura$setPartVisibility(humanoidModel, armorSlot);
    }


    @Inject(at = @At("RETURN"), method = "renderArmorPiece")
    public void postRenderArmorPiece(PoseStack matrices, SubmitNodeCollector submitNodeCollector, ItemStack stack, EquipmentSlot equipmentSlot, int light, S state, CallbackInfo ci) {
        A humanoidModel = this.getArmorModel(state, equipmentSlot);
        if (figura$avatar == null) return;

        VanillaPart part = RenderUtils.partFromSlot(figura$avatar, equipmentSlot);
        if (part != null)
            part.restore(humanoidModel);
    }

    @Unique
    private void figura$tryRenderArmorPart(EquipmentSlot slot, FiguraArmorPartRenderer<S, A> renderer, PoseStack vanillaPoseStack, S state, SubmitNodeCollector submitNodeCollector, int light, ParentType... parentTypes) {
        if (slot == null) return; // ?
        Integer id = state instanceof AvatarRenderState playerRenderState ? playerRenderState.id : ((FiguraEntityRenderStateExtension)state).figura$getEntityId();
        if (id == null) return;
        ItemStack itemStack = ((LivingEntity)(Minecraft.getInstance().level.getEntity(id))).getItemBySlot(slot);

        // Make sure the item in the equipment slot is actually a piece of armor
        if ((itemStack.getItem() instanceof Item armorItem && armorItem.components().has(DataComponents.EQUIPPABLE) && armorItem.components().get(DataComponents.EQUIPPABLE).slot() == slot && armorItem.components().has(DataComponents.ATTRIBUTE_MODIFIERS) && armorItem.components().get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().stream().anyMatch(attribute -> attribute.attribute() == Attributes.ARMOR))) {
            A armorModel = getArmorModel(state, slot);

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
            VanillaPart mainPart = RenderUtils.partFromSlot(figura$avatar, slot);
            int armorEditPermission = figura$avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT);
            if (armorEditPermission == 1 && mainPart != null && !mainPart.checkVisible()) return;

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
        stack.mulPose(Axis.XP.rotationDegrees(180f));
        stack.mulPose(Axis.YP.rotationDegrees(180f));
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
        EquipmentClientInfo.LayerType layerType = this.usesInnerModel(armorSlot) ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS : EquipmentClientInfo.LayerType.HUMANOID;
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);

        if (equippable == null)
            return;

        Optional<ResourceKey<EquipmentAsset>> location = equippable.assetId();
        if (location.isEmpty())
            return;

        List<EquipmentClientInfo.Layer> list = ((EquipmentLayerRendererAccessor)this.equipmentRenderer).figura$getAssetsManager().get(location.get()).getLayers(layerType);

        int i = itemStack.is(ItemTags.DYEABLE) ? DyedItemColor.getOrDefault(itemStack, -6265536) : -1;
        int order = 0;

        for(EquipmentClientInfo.Layer layer : list) {
            int k = EquipmentLayerRendererAccessor.getColorForLayer(layer, i);

            if (k != 0) {
                ResourceLocation normalArmorResource = ((EquipmentLayerRendererAccessor)this.equipmentRenderer).layerTextureLookup().apply(new EquipmentLayerRenderer.LayerTextureKey(layerType, layer));
                nodeCollector.order(order++).submitModelPart(modelPart, poseStack, RenderType.armorCutoutNoCull(normalArmorResource), light, OverlayTexture.NO_OVERLAY, null, 0, null);
                if (hasGlint)
                    nodeCollector.order(order++).submitModelPart(modelPart, poseStack, RenderType.armorEntityGlint(), light, OverlayTexture.NO_OVERLAY, null, 0, null);
                hasGlint = false;
            }
        }

        ArmorTrim trim = itemStack.get(DataComponents.TRIM);
        if (trim != null) {
            TextureAtlasSprite textureAtlasSprite = ((EquipmentLayerRendererAccessor)equipmentRenderer).trimSpriteLookup()
                    .apply(new EquipmentLayerRenderer.TrimSpriteKey(trim, layerType, location.get()));

            RenderType renderType = Sheets.armorTrimsSheet(trim.pattern().value().decal());
            nodeCollector.order(order).submitModelPart(modelPart, poseStack, renderType, light, OverlayTexture.NO_OVERLAY, textureAtlasSprite, -1, null);
        }
    }

    @Unique
    protected void figura$setPartVisibility(A bipedModel, EquipmentSlot slot) {
        bipedModel.setAllVisible(false);
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
