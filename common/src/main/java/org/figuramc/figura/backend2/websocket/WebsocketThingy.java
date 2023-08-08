package org.figuramc.figura.backend2.websocket;

import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.utils.FiguraText;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class WebsocketThingy extends WebSocketClient {

    public static final Map<Integer, String> ERROR_CODES = new HashMap<>() {{
        put(1000, "Normal Closure");
        put(1001, "Going Away");
        put(1002, "Protocol Error");
        put(1003, "Unsupported Data");
        put(1005, "No Status Received");
        put(1006, "Abnormal Closure");
        put(1007, "Invalid Frame Payload Data");
        put(1008, "Policy Violation");
        put(1009, "Message Too Big");
        put(1010, "Mandatory Ext.");
        put(1011, "Internal Error");
        put(1012, "Service Restart");
        put(1013, "Try Again Later");
        put(1014, "Bad Gateway");
        put(1015, "TLS Handshake");
        put(3000, "Unauthorized");
        put(4000, "Re-Auth");
        put(4001, "Banned");
        put(4002, "Too Many Connections");
    }};

    private final String token;

    public WebsocketThingy(String token) {
        super(URI.create(getBackendAddress()));
        this.token = token;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        FiguraMod.LOGGER.info("Connecting to " + FiguraMod.MOD_NAME + " ws backend (" + getBackendAddress() + ")");
        try {
            send(C2SMessageHandler.auth(token));
        } catch (Exception e) {
            handleClose(-1, e.getMessage());
        }
    }

    @Override
    public void onMessage(String message) {
        // nope
        // backend v2 do not have string messages
        // this method is just an illusion
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        if (NetworkStuff.debug)
            FiguraMod.debug("Received raw ws message of " + bytes.remaining() + "b");
        try {
            S2CMessageHandler.handle(bytes);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to handle ws message", e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        reason = reason.isBlank() ? ERROR_CODES.getOrDefault(code, "Unknown") : reason;
        FiguraMod.LOGGER.info("Closed connection: " + reason + ", Code: " + code + ", Remote: " + remote);

        handleClose(code, reason + (FiguraMod.debugModeEnabled() ? "\n\nCode: " + code + "\nRemote: " + remote : ""));
    }

    @Override
    public void onError(Exception e) {
        FiguraMod.LOGGER.warn("", e);
        handleClose(-1, e.getMessage());
    }

    @Override
    public void send(ByteBuffer bytes) {
        if (NetworkStuff.debug)
            FiguraMod.debug("Sent raw ws message of " + bytes.remaining() + "b");
        super.send(bytes);
    }

    private static String getBackendAddress() {
        ServerAddress backendIP = ServerAddress.parseString(Configs.SERVER_IP.value);
        return "wss://" + backendIP.getHost() + "/ws";
    }

    private void handleClose(int code, String reason) {
        if (Configs.CONNECTION_TOASTS.value)
            FiguraToast.sendToast(FiguraText.of("backend.disconnected"), FiguraToast.ToastType.ERROR);

        NetworkStuff.disconnect(reason);

        if (code == 4000)
            NetworkStuff.reAuth();
    }
}
