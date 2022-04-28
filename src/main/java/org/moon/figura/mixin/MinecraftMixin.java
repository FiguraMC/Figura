package org.moon.figura.mixin;

import net.minecraft.client.Minecraft;
import org.moon.figura.FiguraMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(at = @At("RETURN"), method = "tick")
    private void endTick(CallbackInfo info) {
        FiguraMod.tick((Minecraft) (Object) this);
    }
}
