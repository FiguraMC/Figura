package org.figuramc.figura.mixin.input;

import com.mojang.blaze3d.platform.InputConstants;
import org.figuramc.figura.lua.docs.FiguraListDocs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InputConstants.Type.class)
public class InputConstantsTypeMixin {

    @Inject(at = @At("HEAD"), method = "addKey")
    private static void addKey(InputConstants.Type type, String translationKey, int keyCode, CallbackInfo ci) {
        FiguraListDocs.KEYBINDS.add(translationKey);
    }
}
