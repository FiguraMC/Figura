package org.figuramc.figura.mixin.compat;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.GeckolibGeoArmorAccessor;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.geckolib.animatable.GeoItem;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.base.GeoRenderState;

@Pseudo
@Mixin(value = GeoArmorRenderer.class, remap = false)
public abstract class GeckolibGeoArmorRendererMixin<T extends Item & GeoItem> implements GeckolibGeoArmorAccessor {
    @Unique
    private Avatar figura$avatar;

    @Inject(method = "captureDefaultRenderState(Lnet/minecraft/world/item/Item;Lsoftware/bernie/geckolib/renderer/GeoArmorRenderer$RenderData;Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;F)Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;", at = @At(value = "HEAD"))
    private <R extends HumanoidRenderState & GeoRenderState> void figura$prepAvatar(T animatable, GeoArmorRenderer.RenderData renderData, R renderState, float partialTick, CallbackInfoReturnable<R> cir){
        Entity entity = renderData.entity();
        if (entity != null)
            figura$avatar = AvatarManager.getAvatar(entity);
        else {
            figura$avatar = null;
        }
    }

    @Override
    @Unique
    public Avatar figura$getAvatar() {
        return figura$avatar;
    }

    @Override
    @Accessor("entityRenderTranslations")
    public abstract void figura$setEntityRenderTranslations(Matrix4f matrix4f);

    @Override
    @Accessor("modelRenderTranslations")
    public abstract void figura$setModelRenderTranslations(Matrix4f matrix4f);

    @Override
    @Accessor("scaleWidth")
    public abstract float figura$getScaleWidth();

    @Override
    @Accessor("scaleHeight")
    public abstract float figura$getScaleHeight();

    @Override
    @Accessor("headBone")
    public abstract GeoBone figura$getHeadBone();

    @Override
    @Accessor("leftLegBone")
    public abstract GeoBone figura$getLeftLegBone();

    @Override
    @Accessor("rightLegBone")
    public abstract GeoBone figura$getRightLegBone();

    @Override
    @Accessor("leftArmBone")
    public abstract GeoBone figura$getLeftArmBone();

    @Override
    @Accessor("rightArmBone")
    public abstract GeoBone figura$getRightArmBone();

    @Override
    @Accessor("bodyBone")
    public abstract GeoBone figura$getBodyBone();

    @Override
    @Accessor("leftBootBone")
    public abstract GeoBone figura$getLeftBootBone();

    @Override
    @Accessor("rightBootBone")
    public abstract GeoBone figura$getRightBootBone();
}