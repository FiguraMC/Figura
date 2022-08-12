package org.moon.figura.binary_backend;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.moon.figura.FiguraMod;
import org.moon.figura.backend.NetworkManager;
import org.moon.figura.binary_backend.handlers.MessageHandlerV0;
import org.moon.figura.binary_backend.handlers.NewMessageHandler;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class BackendConnection extends WebSocketClient {

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
        put(4001, "Banned ^.^");
        put(4002, "Too Many Connections");
    }};

    public final NewMessageHandler messageHandler = MessageHandlerV0.get();

    public BackendConnection(URI serverUri) {
        super(serverUri);
    }


    @Override
    public void onOpen(ServerHandshake handshakedata) {
//        String backendAddress = NewNetworkManager.getBackendAddress();
//        FiguraMod.LOGGER.info("Connecting to " + FiguraMod.MOD_NAME + " ws backend (" + backendAddress + ")");
//        send(NetworkManager.authToken);
    }

    @Override
    public void onMessage(String message) {
        FiguraMod.LOGGER.info("Received string message:\"" + message + "\"");
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        FiguraMod.LOGGER.info("Received binary message.");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }
}
