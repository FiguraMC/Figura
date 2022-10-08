package org.moon.figura.mixin.render.layers;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.model.ParentType;
import org.moon.figura.trust.TrustContainer;
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

    @Shadow @Final private ItemInHandRenderer itemInHandRenderer;

    @Shadow @Final private Map<SkullBlock.Type, SkullModelBase> skullModels;

    @Inject(at = @At("HEAD"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", cancellable = true)
    private void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
        if (itemStack == null || (itemStack.getItem() instanceof ArmorItem armorItem && armorItem.getSlot() == EquipmentSlot.HEAD))
            return;

        Avatar avatar = AvatarManager.getAvatar(livingEntity);
        if (avatar == null || avatar.trust.get(TrustContainer.Trust.VANILLA_MODEL_EDIT) == 0)
            return;

        //script hide
        if (avatar.luaRuntime != null && !avatar.luaRuntime.vanilla_model.HELMET_ITEM.getVisible()) {
            ci.cancel();
            return;
        }

        //pivot part
        if (itemStack.getItem() instanceof BlockItem block && block.getBlock() instanceof AbstractSkullBlock) {
            //fetch skull data
            GameProfile gameProfile = null;
            if (itemStack.hasTag()) {
                CompoundTag tag = itemStack.getTag();
                if (tag != null && tag.contains("SkullOwner", Tag.TAG_COMPOUND))
                    gameProfile = NbtUtils.readGameProfile(itemStack.getTag().getCompound("SkullOwner"));
            }

            SkullBlock.Type type = ((AbstractSkullBlock) ((BlockItem) itemStack.getItem()).getBlock()).getType();
            SkullModelBase skullModelBase = this.skullModels.get(type);
            RenderType renderType = SkullBlockRenderer.getRenderType(type, gameProfile);

            //render!!
            if (avatar.pivotPartRender(ParentType.HelmetItemPivot, stack -> {
                float s = 19f;
                stack.scale(s, s, s);
                stack.translate(-0.5d, 0d, -0.5d);
                SkullBlockRenderer.renderSkull(null, 0f, f, stack, multiBufferSource, i, skullModelBase, renderType);
            })) {
                ci.cancel();
            }
        } else if (avatar.pivotPartRender(ParentType.HelmetItemPivot, stack -> {
            float s = 10f;
            stack.translate(0d, 4d, 0d);
            stack.scale(s, s, s);
            this.itemInHandRenderer.renderItem(livingEntity, itemStack, ItemTransforms.TransformType.HEAD, false, stack, multiBufferSource, i);
        })) {
            ci.cancel();
        }
    }
}
