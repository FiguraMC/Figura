package org.figuramc.figura.mixin.render.layers.elytra;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.mixin.render.layers.EquipmentLayerRendererAccessor;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.PlatformUtils;
import org.figuramc.figura.utils.RenderUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(WingsLayer.class)
public abstract class ElytraLayerMixin<T extends LivingEntity, S extends HumanoidRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {

    public ElytraLayerMixin(RenderLayerParent<S, M> context) {
        super(context);
    }

    @Shadow @Final private EquipmentLayerRenderer equipmentRenderer;

    @Unique
    private VanillaPart vanillaPart;
    @Unique
    private Avatar figura$avatar;

    @Unique
    private boolean renderedPivot;

    @Inject(at = @At(value = "HEAD"), method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V")
    public void setAvatar(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S humanoidRenderState, float f, float g, CallbackInfo ci) {
        figura$avatar = AvatarManager.getAvatar(humanoidRenderState);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/ResourceLocation;II)V", shift = At.Shift.BEFORE),
            method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V")
    public void onRender(PoseStack pose, SubmitNodeCollector submitNodeCollector, int light, S humanoidRenderState, float f, float g, CallbackInfo ci, @Local ElytraModel elytraModel) {
        vanillaPart = null;
        if (figura$avatar == null)
            return;

        Avatar avatar = figura$avatar;
        if (avatar.luaRuntime != null) {
            VanillaPart part = figura$avatar.luaRuntime.vanilla_model.ELYTRA;
            part.save(elytraModel);
            if (figura$avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1) {
                vanillaPart = part;
                vanillaPart.preTransform(elytraModel);
            }
        }
        FiguraSubmitCallBackExtension submitCallBackExtension = (FiguraSubmitCallBackExtension) elytraModel;

        submitCallBackExtension.figura$setPreRenderingCallback(((multiBufferSource, poseStack) -> {
            Integer id = humanoidRenderState instanceof AvatarRenderState playerRenderState ? playerRenderState.id : ((FiguraEntityRenderStateExtension)humanoidRenderState).figura$getEntityId();
            if (id != null)
                avatar.elytraRender(Minecraft.getInstance().level.getEntity(id), multiBufferSource, poseStack, light, ((FiguraEntityRenderStateExtension)humanoidRenderState).figura$getTickDelta(), elytraModel);

            if (vanillaPart != null)
                vanillaPart.posTransform(elytraModel);

            return true;
        }));
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/ResourceLocation;II)V"), method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", cancellable = true)
    public void cancelVanillaPart(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, S humanoidRenderState, float f, float g, CallbackInfo ci, @Local ElytraModel elytraModel) {
        if (vanillaPart != null)
            vanillaPart.restore(elytraModel);
        renderedPivot = true;
        submitElytraPivot(humanoidRenderState, poseStack, submitNodeCollector, light, elytraModel);
        if (renderedPivot) {
            poseStack.popPose();
            ci.cancel();
        }
    }

    public void submitElytraPivot(S state, PoseStack poseStack, SubmitNodeCollector nodeCollector, int light, ElytraModel elytraModel) {

        ItemStack itemStack = state.chestEquipment;
        if (!itemStack.is(Items.ELYTRA) && !PlatformUtils.isModLoaded("origins")) {
            return;
        }
        Avatar figura$Avatar = figura$avatar;
        if (figura$Avatar != null && figura$Avatar.luaRuntime != null && figura$Avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1 && figura$Avatar.luaRuntime.vanilla_model.ELYTRA.checkVisible()) {
            // Try to render the pivot part
            elytraModel.setupAnim(state);

            NodeCollectorExtension nodeCollectorExtension = (NodeCollectorExtension) nodeCollector;
            nodeCollectorExtension.submitFiguraModel(figura$Avatar, state, (avatar, renderState, bufferSource) -> {
                Integer id = renderState instanceof AvatarRenderState playerRenderState ? playerRenderState.id : ((FiguraEntityRenderStateExtension)renderState).figura$getEntityId();
                if (id != null)
                    avatar.elytraRender(Minecraft.getInstance().level.getEntity(id), bufferSource, poseStack, light, ((FiguraEntityRenderStateExtension)renderState).figura$getTickDelta(), elytraModel);

                if (vanillaPart != null)
                    vanillaPart.posTransform(elytraModel);

                return null;
            });

            ResourceLocation playerTexture =  RenderUtils.getPlayerSkinTexture((WingsLayer<?, ?>) (Object)this, state);

            VanillaPart part = RenderUtils.pivotToPart(figura$Avatar, ParentType.LeftElytraPivot);
            if (part != null && part.checkVisible()) {
                boolean leftWing = figura$Avatar.pivotPartRender(ParentType.LeftElytraPivot, stack -> {
                    stack.pushPose();
                    stack.scale(16, 16, 16);
                    stack.mulPose(Axis.XP.rotationDegrees(180f));
                    stack.mulPose(Axis.YP.rotationDegrees(180f));
                    stack.translate(0.0f, 0.0f, 0.125f);
                    figura$submitElytraPart(((ElytraModelAccessor)elytraModel).getLeftWing(), stack, nodeCollector, light, state.outlineColor, itemStack, playerTexture);
                    stack.popPose();
                });
                if (!leftWing) {
                    figura$submitElytraPart(((ElytraModelAccessor)elytraModel).getLeftWing(), poseStack, nodeCollector, light, state.outlineColor, itemStack, playerTexture);
                }
            }
            part = RenderUtils.pivotToPart(figura$Avatar, ParentType.RightElytraPivot);
            if (part != null && part.checkVisible()) {
                    boolean rightWing = figura$Avatar.pivotPartRender(ParentType.RightElytraPivot, stack -> {
                    stack.pushPose();
                    stack.scale(16, 16, 16);
                    stack.mulPose(Axis.XP.rotationDegrees(180f));
                    stack.mulPose(Axis.YP.rotationDegrees(180f));
                    stack.translate(0.0f, 0.0f, 0.125f);
                    figura$submitElytraPart(((ElytraModelAccessor)elytraModel).getRightWing(), stack, nodeCollector, light, state.outlineColor, itemStack, playerTexture);
                    stack.popPose();
                });
                if (!rightWing) {
                    figura$submitElytraPart(((ElytraModelAccessor)elytraModel).getRightWing(), poseStack, nodeCollector, light, state.outlineColor, itemStack, playerTexture);
                }
            }
        } else renderedPivot = figura$Avatar != null && figura$Avatar.luaRuntime != null && figura$Avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1 && !figura$Avatar.luaRuntime.vanilla_model.ELYTRA.checkVisible();
    }

    // rewritten to work with mojang's shiny new layer system
    @Unique
    private void figura$submitElytraPart(ModelPart modelPart, PoseStack poseStack, SubmitNodeCollector nodeCollector, int light, int outlineColor, ItemStack itemStack, @Nullable ResourceLocation playerLocation) {
        boolean hasGlint = itemStack.hasFoil();

        EquipmentClientInfo.LayerType layerType = EquipmentClientInfo.LayerType.WINGS;
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
                ResourceLocation normalArmorResource = layer.usePlayerTexture() && playerLocation != null ? playerLocation : ((EquipmentLayerRendererAccessor)this.equipmentRenderer).layerTextureLookup().apply(new EquipmentLayerRenderer.LayerTextureKey(layerType, layer));
                nodeCollector.order(order++).submitModelPart(modelPart, poseStack, RenderType.armorCutoutNoCull(normalArmorResource), light, OverlayTexture.NO_OVERLAY, null, outlineColor, null);
                if (hasGlint)
                    nodeCollector.order(order++).submitModelPart(modelPart, poseStack, RenderType.armorEntityGlint(), light, OverlayTexture.NO_OVERLAY, null, outlineColor, null);
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
}
