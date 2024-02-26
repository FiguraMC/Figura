package org.figuramc.figura.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.LivingEntityExtension;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityExtension {

    @Shadow public float animationSpeed;

    @Shadow public float animationPosition;

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isUsingItem()Z"), method = "triggerItemUseEffects", cancellable = true)
    private void triggerItemUseEffects(ItemStack stack, int particleCount, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatar(this);
        if (avatar != null && avatar.useItemEvent(ItemStackAPI.verify(stack), stack.getUseAnimation().name(), particleCount))
            ci.cancel();
    }

    @Unique
    @Override
    public void figura$updateWalkAnimation(float f) {
        this.animationSpeed += (f - this.animationSpeed) * 0.4f;
        this.animationPosition += this.animationSpeed;
    }
}
