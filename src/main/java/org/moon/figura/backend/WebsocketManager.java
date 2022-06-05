package org.moon.figura.backend;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.Config;

import java.net.URI;

public class WebsocketManager extends WebSocketClient {

    private final String authToken;

    public WebsocketManager(String authToken) {
        super(URI.create(getBackendAddress()));
        this.authToken = authToken;
    }

    private static String getBackendAddress() {
        return "ws://" + Config.BACKEND.value + ":" + NetworkManager.BACKEND_PORT;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        FiguraMod.LOGGER.info("Connecting to " + getBackendAddress());
        send(authToken);
    }

    @Override
    public void onMessage(String message) {
        FiguraMod.LOGGER.info("Received message: " + message);
        MessageHandler.handleMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        NetworkManager.backend = null;
        System.out.println("Closed connection: " + reason);
        System.out.println("Code: " + code + " remote: " + remote);
    }

    @Override
    public void onError(Exception ex) {
        NetworkManager.backend = null;
    }

    @Override
    public void send(String text) {
        FiguraMod.LOGGER.info("Sending message: " + text);
        super.send(text);
    }
}
