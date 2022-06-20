package org.moon.figura.mixin.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import org.moon.figura.gui.actionwheel.ActionWheel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(at = @At("HEAD"), method = "renderCrosshair", cancellable = true)
    private void renderCrosshair(PoseStack stack, CallbackInfo ci) {
        if (ActionWheel.isEnabled())
            ci.cancel();
    }
}
