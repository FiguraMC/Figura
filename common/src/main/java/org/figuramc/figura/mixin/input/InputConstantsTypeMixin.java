package org.figuramc.figura.mixin.input;

import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.figuramc.figura.ducks.FiguraKeyStorage.allKeys;

@Mixin(InputConstants.Type.class)
public class InputConstantsTypeMixin {

    @Inject(at = @At("HEAD"), method = "addKey")
    private static void addKey(InputConstants.Type type, String translationKey, int keyCode, CallbackInfo ci) {
        allKeys.add(translationKey);
    }
}
