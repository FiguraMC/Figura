package org.moon.figura.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.math.vector.FiguraVec3;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "getEyePosition()Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"), cancellable = true)
    private void getEyePosition(CallbackInfoReturnable<Vec3> cir) {
        figura$offsetEyePos(cir);
    }

    @Inject(method = "getEyePosition(F)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"), cancellable = true)
    private void getEyePosition(float tickDelta, CallbackInfoReturnable<Vec3> cir) {
        figura$offsetEyePos(cir);
    }

    @Intrinsic
    private void figura$offsetEyePos(CallbackInfoReturnable<Vec3> cir) {
        Avatar avatar = AvatarManager.getAvatar((Entity) (Object) this);
        if (avatar == null || avatar.luaRuntime == null)
            return;
        FiguraVec3 vec = avatar.luaRuntime.renderer.eyeOffset;
        if (vec != null) cir.setReturnValue(cir.getReturnValue().add(vec.asVec3()));
    }
}
