package org.figuramc.figura.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.figuramc.figura.FiguraMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientPacketListener.class, priority = 999)
public class ClientPacketListenerMixin {

    @Inject(at = @At("HEAD"), method = "sendUnsignedCommand", cancellable = true)
    private void sendUnsignedCommand(String command, CallbackInfoReturnable<Boolean> cir) {
        if (command.startsWith(FiguraMod.MOD_ID))
            cir.setReturnValue(false);
    }
}
