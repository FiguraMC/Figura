package org.moon.figura.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
  @Inject(method = "swing", at = @At("HEAD"))
  public void swing(InteractionHand hand, CallbackInfo ci) {
    Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
    if (avatar != null)
        avatar.armSwingEvent(hand==InteractionHand.MAIN_HAND?"MAIN_HAND":"OFF_HAND");
  }
}
