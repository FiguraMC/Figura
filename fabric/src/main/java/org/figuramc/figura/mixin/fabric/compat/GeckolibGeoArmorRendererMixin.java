package org.figuramc.figura.mixin.fabric.compat;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.fabric.GeckolibGeoArmorAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderer.geo.GeoArmorRenderer;

@Pseudo
@Mixin(value = GeoArmorRenderer.class, remap = false)
public abstract class GeckolibGeoArmorRendererMixin<T extends ArmorItem & IAnimatable> implements GeckolibGeoArmorAccessor {
    @Unique
    private Avatar figura$avatar;

    @Inject(method = "setCurrentItem", at = @At(value = "HEAD"))
    private void figura$prepAvatar(LivingEntity entity, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel model, CallbackInfoReturnable<GeoArmorRenderer<T>> cir){
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
    @Accessor("widthScale")
    public abstract float figura$getScaleWidth();

    @Override
    @Accessor("heightScale")
    public abstract float figura$getScaleHeight();

    @Override
    @Accessor("armorSlot")
    public abstract EquipmentSlot figura$getSlot();

    @Override
    @Accessor("modelProvider")
    public abstract AnimatedGeoModel<?> figura$getAnimatedModelProvider();
}