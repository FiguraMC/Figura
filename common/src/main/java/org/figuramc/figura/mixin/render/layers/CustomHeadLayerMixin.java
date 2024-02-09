package org.figuramc.figura.mixin.render.layers;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.SkullBlockRendererAccessor;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.utils.NbtType;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin<T extends LivingEntity, M extends EntityModel<T> & HeadedModel> extends RenderLayer<T, M> {

    public CustomHeadLayerMixin(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Inject(at = @At("HEAD"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", cancellable = true)
    private void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
        if (itemStack.getItem() instanceof ArmorItem && ((ArmorItem) itemStack.getItem()).getSlot() == EquipmentSlot.HEAD) {
            ArmorItem armorItem = (ArmorItem) itemStack.getItem();
            return;
        }

        Avatar avatar = AvatarManager.getAvatar(livingEntity);
        if (!RenderUtils.vanillaModel(avatar))
            return;

        // script hide
        if (avatar.luaRuntime != null && !avatar.luaRuntime.vanilla_model.HELMET_ITEM.checkVisible()) {
            ci.cancel();
            return;
        }

        // pivot part
        if (itemStack.getItem() instanceof BlockItem && ((BlockItem) itemStack.getItem()).getBlock() instanceof AbstractSkullBlock) {
            // fetch skull data
            GameProfile gameProfile;
            if (itemStack.hasTag()) {
                CompoundTag tag = itemStack.getTag();
                if (tag != null && tag.contains("SkullOwner", NbtType.COMPOUND.getValue()))
                    gameProfile = NbtUtils.readGameProfile(itemStack.getTag().getCompound("SkullOwner"));
                else {
                    gameProfile = null;
                }
            } else {
                gameProfile = null;
            }

            SkullBlock.Type type = ((AbstractSkullBlock) ((BlockItem) itemStack.getItem()).getBlock()).getType();

            // render!!
            if (avatar.pivotPartRender(ParentType.HelmetItemPivot, stack -> {
                float s = 19f;
                stack.scale(s, s, s);
                stack.translate(-0.5d, 0d, -0.5d);

                // set item context
                SkullBlockRendererAccessor.setItem(itemStack);
                SkullBlockRendererAccessor.setEntity(livingEntity);
                SkullBlockRendererAccessor.setRenderMode(SkullBlockRendererAccessor.SkullRenderMode.HEAD);
                SkullBlockRenderer.renderSkull(null, 0f, type, gameProfile, f, stack, multiBufferSource, i);
            })) {
                ci.cancel();
            }
        } else if (avatar.pivotPartRender(ParentType.HelmetItemPivot, stack -> {
            float s = 10f;
            stack.translate(0d, 4d, 0d);
            stack.scale(s, s, s);
            Minecraft.getInstance().getItemInHandRenderer().renderItem(livingEntity, itemStack, ItemTransforms.TransformType.HEAD, false, stack, multiBufferSource, i);
        })) {
            ci.cancel();
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/SkullBlockRenderer;renderSkull(Lnet/minecraft/core/Direction;FLnet/minecraft/world/level/block/SkullBlock$Type;Lcom/mojang/authlib/GameProfile;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
    private void renderSkull(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        SkullBlockRendererAccessor.setItem(livingEntity.getItemBySlot(EquipmentSlot.HEAD));
        SkullBlockRendererAccessor.setEntity(livingEntity);
        SkullBlockRendererAccessor.setRenderMode(SkullBlockRendererAccessor.SkullRenderMode.HEAD);
    }
}
