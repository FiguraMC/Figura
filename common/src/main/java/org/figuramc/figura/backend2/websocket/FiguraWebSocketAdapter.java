package org.figuramc.figura.backend2.websocket;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.utils.FiguraText;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FiguraWebSocketAdapter extends WebSocketAdapter {
    public static final Map<Integer, String> ERROR_CODES = new HashMap<Integer, String>() {{
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

    public FiguraWebSocketAdapter(String token) {
        this.token = token;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        FiguraMod.LOGGER.info("Connecting to " + FiguraMod.MOD_NAME + " ws backend (" + getBackendAddress() + ")");
        super.onConnected(websocket, headers);
        try {
            websocket.sendBinary(C2SMessageHandler.auth(token).array());
        } catch (Exception e) {
            handleClose(-1, e.getMessage());
        }
    }

    public static String getBackendAddress() {
        ServerAddress backendIP = ServerAddress.parseString(Configs.SERVER_IP.value);
        return "wss://" + backendIP.getHost() + "/ws";
    }

    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
        super.onBinaryMessage(websocket, binary);
        if (NetworkStuff.debug)
            FiguraMod.debug("Received raw ws message of " + binary.length + "b");
        try {
            S2CMessageHandler.handle(ByteBuffer.wrap(binary));
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to handle ws message", e);
        }
    }


    @Override
    public void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) throws Exception {
        super.onSendingHandshake(websocket, requestLine, headers);
    }

    @Override
    public void onTextMessage(WebSocket websocket, byte[] data) {
        //Figura ignores string messages apparently because they don't exist in the backend v2
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
        super.handleCallbackError(websocket, cause);
        FiguraMod.LOGGER.error("Failed to handle ws message", cause);
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean remote) throws Exception {
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, remote);
        String reason = serverCloseFrame.getCloseReason();
        if (reason == null)
            reason = "Unknown!";
        int code = serverCloseFrame.getCloseCode();
        reason = reason.trim().isEmpty() ? ERROR_CODES.getOrDefault(code, "Unknown") : reason;
        FiguraMod.LOGGER.info("Closed connection: " + reason + ", Code: " + code + ", Remote: " + remote);

        handleClose(code, reason + (FiguraMod.debugModeEnabled() ? "\n\nCode: " + code + "\nRemote: " + remote : ""));
    }

    @Override
    public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if (NetworkStuff.debug && frame.getPayload() != null)
            FiguraMod.debug("Sent raw ws message of " + frame.getPayload().length + "b");
        super.onSendingFrame(websocket, frame);
    }

    private void handleClose(int code, String reason) {
        if (Configs.CONNECTION_TOASTS.value)
            FiguraToast.sendToast(FiguraText.of("backend.disconnected"), FiguraToast.ToastType.ERROR);

        NetworkStuff.disconnect(reason);

        if (code == 4000)
            NetworkStuff.reAuth();
    }
}
