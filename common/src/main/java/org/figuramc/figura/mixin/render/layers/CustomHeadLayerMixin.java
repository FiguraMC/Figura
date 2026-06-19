package org.figuramc.figura.mixin.render.layers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.figuramc.figura.ducks.FiguraItemStackRenderStateExtension;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.figuramc.figura.ducks.SkullBlockRendererAccessor;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin<S extends LivingEntityRenderState, M extends EntityModel<S> & HeadedModel> extends RenderLayer<S, M> {

    public CustomHeadLayerMixin(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Shadow @Final private Function<SkullBlock.Type, SkullModelBase> skullModels;

    @Shadow
    protected abstract RenderType resolveSkullRenderType(LivingEntityRenderState livingEntityRenderState, SkullBlock.Type type);

    @Unique
    private Avatar avatar;

    @Inject(at = @At("HEAD"), method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V", cancellable = true)
    private void render(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S entityState, float f, float g, CallbackInfo ci) {
        ItemStackRenderState itemStackState = entityState.headItem;
        // check for armor attributes :3
        if (((FiguraItemStackRenderStateExtension)itemStackState).figura$getItemStack() == null || (((FiguraItemStackRenderStateExtension)itemStackState).figura$getItemStack().getItem() instanceof Item armorItem && armorItem.components().has(DataComponents.EQUIPPABLE) && armorItem.components().get(DataComponents.EQUIPPABLE).slot() == EquipmentSlot.HEAD && armorItem.components().has(DataComponents.ATTRIBUTE_MODIFIERS) && armorItem.components().get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().stream().anyMatch(attribute -> attribute.attribute() == Attributes.ARMOR)))
            return;

        ItemStack itemStack = ((FiguraItemStackRenderStateExtension)itemStackState).figura$getItemStack();
        avatar = AvatarManager.getAvatar(entityState);
        if (!RenderUtils.vanillaModel(avatar))
            return;

        // script hide
        if (avatar.luaRuntime != null && !avatar.luaRuntime.vanilla_model.HELMET_ITEM.checkVisible()) {
            ci.cancel();
            return;
        }

        // pivot part
        if (entityState.wornHeadType != null) {
            SkullBlock.Type type = entityState.wornHeadType;
            SkullModelBase skullModelBase = this.skullModels.apply(type);
            RenderType renderType = resolveSkullRenderType(entityState, type);

            // render!!
            if (avatar.pivotPartRender(ParentType.HelmetItemPivot, stack -> {
                float s = 19f;
                stack.scale(s, s, s);
                stack.translate(-0.5d, 0d, -0.5d);

                // set item context
                SkullBlockRendererAccessor.setItem(itemStack);
                Integer id = ((FiguraEntityRenderStateExtension)entityState).figura$getEntityId();
                if (id != null)
                    SkullBlockRendererAccessor.setEntity(Minecraft.getInstance().level.getEntity(id));
                SkullBlockRendererAccessor.setRenderMode(SkullBlockRendererAccessor.SkullRenderMode.HEAD);
                SkullBlockRenderer.submitSkull(f, stack, submitNodeCollector, i, skullModelBase,
                        renderType, entityState.outlineColor, null);
            })) {
                ci.cancel();
            }
        } else if (avatar.pivotPartRender(ParentType.HelmetItemPivot, stack -> {
            float s = 10f;
            stack.translate(0d, 4d, 0d);
            stack.scale(s, s, s);
            ItemTransform transform = ((FiguraItemStackRenderStateExtension) itemStackState).figura$getItemTransform();

            boolean shouldSubmitVanilla = avatar.itemRenderEvent(ItemStackAPI.verify(((FiguraItemStackRenderStateExtension)itemStackState).figura$getItemStack()), ((FiguraItemStackRenderStateExtension)itemStackState).figura$getDisplayContext().name(), FiguraVec3.fromVec3f(transform.translation()), FiguraVec3.of(transform.rotation().z(), transform.rotation().y(), transform.rotation().x()), FiguraVec3.fromVec3f(transform.scale()), ((FiguraItemStackRenderStateExtension) itemStackState).figura$isLeftHanded(), stack, submitNodeCollector, entityState.lightCoords, OverlayTexture.NO_OVERLAY);
            if (shouldSubmitVanilla)
                entityState.headItem.submit(poseStack, submitNodeCollector, i, OverlayTexture.NO_OVERLAY, entityState.outlineColor);
        })) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"))
    private void figuraItemEvent(ItemStackRenderState instance, PoseStack matrices, SubmitNodeCollector submitNodeCollector, int i, int j, int k, Operation<Void> original, @Local(argsOnly = true) S entityState) {
        ItemTransform transform =  ((FiguraItemStackRenderStateExtension) instance).figura$getItemTransform();
        boolean callOriginal = avatar == null || !avatar.itemRenderEvent(ItemStackAPI.verify(((FiguraItemStackRenderStateExtension)instance).figura$getItemStack()), ((FiguraItemStackRenderStateExtension)instance).figura$getDisplayContext().name(), FiguraVec3.fromVec3f(transform.translation()), FiguraVec3.of(transform.rotation().z(), transform.rotation().y(), transform.rotation().x()), FiguraVec3.fromVec3f(transform.scale()), ((FiguraItemStackRenderStateExtension) instance).figura$isLeftHanded(), matrices, submitNodeCollector, i, j);
        if (callOriginal)
            original.call(instance, matrices, submitNodeCollector, i, j, k);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/SkullBlockRenderer;submitSkull(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/model/object/skull/SkullModelBase;Lnet/minecraft/client/renderer/rendertype/RenderType;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"), method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V")
    private void renderSkull(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S livingEntityRenderState, float f, float g, CallbackInfo ci) {
        ItemStack stack = ((FiguraItemStackRenderStateExtension)livingEntityRenderState.headItem).figura$getItemStack();
        if (stack == null) return;
        SkullBlockRendererAccessor.setItem(stack);
        Integer id = ((FiguraEntityRenderStateExtension)livingEntityRenderState).figura$getEntityId();
        if (id != null && Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(id) != null)
            SkullBlockRendererAccessor.setEntity(Minecraft.getInstance().level.getEntity(id));
        SkullBlockRendererAccessor.setRenderMode(SkullBlockRendererAccessor.SkullRenderMode.HEAD);
    }
}
