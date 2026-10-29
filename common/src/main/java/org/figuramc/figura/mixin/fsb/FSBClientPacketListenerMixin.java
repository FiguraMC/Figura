package org.figuramc.figura.mixin.fsb;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.figuramc.figura.fsb_client.FSBClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class FSBClientPacketListenerMixin {
    @Inject(method = "handleLogin", at = @At("HEAD"))
    private void onLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
        FSBClient.newSession((ClientPacketListener) (Object) this);
    }

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void onDisconnect(Component reason, CallbackInfo ci) {
        FSBClient.terminateSession((ClientPacketListener) (Object) this);
    }
}
