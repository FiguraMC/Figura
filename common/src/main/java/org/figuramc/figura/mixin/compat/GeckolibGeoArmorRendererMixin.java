package org.figuramc.figura.mixin.compat;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
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
import software.bernie.geckolib.renderer.GeoArmorRenderer;

@Pseudo
@Mixin(value = GeoArmorRenderer.class, remap = false)
public abstract class GeckolibGeoArmorRendererMixin implements GeckolibGeoArmorAccessor {
    @Unique
    private Avatar figura$avatar;

    @Inject(method = "prepForRender", at = @At(value = "HEAD"))
    private void figura$prepAvatar(Entity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> baseModel, CallbackInfo ci){
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
}