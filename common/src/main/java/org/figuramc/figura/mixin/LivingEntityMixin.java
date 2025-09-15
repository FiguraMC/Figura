package org.figuramc.figura.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.permissions.Permissions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isUsingItem()Z"), method = "triggerItemUseEffects", cancellable = true)
    private void triggerItemUseEffects(ItemStack stack, int particleCount, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatar(this);
        if (avatar != null && avatar.useItemEvent(ItemStackAPI.verify(stack), stack.getUseAnimation().name(), particleCount))
            ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "handleDamageEvent", cancellable = true)
    private void handleDamageEvent(DamageSource source, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatar(this);
        if (avatar != null) {
            if(!avatar.damageEvent(
                    source.typeHolder().unwrapKey().get().location().toString(),
                    EntityAPI.wrap(source.getEntity()),
                    EntityAPI.wrap(source.getDirectEntity()),
                    source.getSourcePosition() != null ? FiguraVec3.fromVec3(source.getSourcePosition()) : null
                )) return;
            if (avatar.permissions.get(Permissions.CANCEL_DAMAGE) >= 1) {
                avatar.noPermissions.remove(Permissions.CANCEL_DAMAGE);
                ci.cancel();
                return;
            }
            avatar.noPermissions.add(Permissions.CANCEL_DAMAGE);
                
            
        }
    }
}
