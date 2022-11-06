package org.moon.figura.backend2.websocket;

import org.moon.figura.FiguraMod;
import org.moon.figura.backend2.NetworkStuff;
import org.moon.figura.config.Config;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.utils.FiguraText;

import java.nio.ByteBuffer;

public class S2CMessageHandler {

    public static final byte
        AUTH = 0,
        PING = 1,
        EVENT = 2,
        TOAST = 3,
        CHAT = 4;

    public static void handle(ByteBuffer bytes) {
        if (!bytes.hasRemaining())
            return;

        byte b = bytes.get();
        FiguraMod.debug("Got ws message of type: " + b);

        switch (b) {
            case AUTH -> auth();
        }
    }

    private static void auth() {
        FiguraMod.LOGGER.info("Connected to " + FiguraMod.MOD_NAME + " ws backend");
        NetworkStuff.backendStatus = 3;
        if (Config.CONNECTION_TOASTS.asBool())
            FiguraToast.sendToast(FiguraText.of("backend.connected"));
    }

}
