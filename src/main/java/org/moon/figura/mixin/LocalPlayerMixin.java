package org.moon.figura.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.moon.figura.FiguraMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LocalPlayer.class, priority = 999)
public class LocalPlayerMixin {

	@Inject(at = @At("HEAD"), method = "commandUnsigned", cancellable = true)
	private void sendUnsignedCommand(String command, CallbackInfoReturnable<Boolean> cir) {
		if (command.startsWith(FiguraMod.MOD_ID))
			cir.setReturnValue(false);
	}
}
