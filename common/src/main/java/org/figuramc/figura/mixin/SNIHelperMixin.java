package org.figuramc.figura.mixin;

import org.figuramc.figura.FiguraMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.net.Socket;

@Mixin(targets = "com.neovisionaries.ws.client.SNIHelper")
public class SNIHelperMixin {
    @Unique
    private static final String SNI_FIXED_AFTER = "1.8.0_141";

    @Inject(method = "setServerNames(Ljava/net/Socket;[Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Ljavax/net/ssl/SSLSocket;getSSLParameters()Ljavax/net/ssl/SSLParameters;"), cancellable = true, remap = false)
    private static void fixMissingSNI(Socket socket, String[] hostnames, CallbackInfo ci) {
        String version = System.getProperty("java.version");

        if (version.startsWith("1.8.0_") && version.compareTo(SNI_FIXED_AFTER) < 0) {
            FiguraMod.LOGGER.info("Old Java version (" + version + ") detected, fixing missing SNI");
            try {
                Method setHost = socket.getClass().getMethod("setHost", String.class);
                setHost.invoke(socket, hostnames[0]);
            } catch (Exception e) {
                FiguraMod.LOGGER.error("SNI fix failed!", e);
            }
            ci.cancel();
        }
    }
}
