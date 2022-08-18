package org.moon.figura.binary_backend_wip;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.moon.figura.FiguraMod;
import org.moon.figura.binary_backend_wip.handlers.MessageHandlerV0;
import org.moon.figura.binary_backend_wip.packets.AbstractPacket;
import org.moon.figura.binary_backend_wip.packets.server2client.S2CConnectedPacket;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.utils.FiguraText;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class BackendConnection extends WebSocketClient {

    private ConnectionInfo connectionInfo = new ConnectionInfo();
    public final NewMessageHandler messageHandler = MessageHandlerV0.get();

    public BackendConnection(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        String backendAddress = NewNetworkManager.getBackendAddress();
        FiguraMod.LOGGER.info("Connecting to " + FiguraMod.MOD_NAME + " ws backend (" + backendAddress + ")");

    }

    @Override
    public void onMessage(String message) {
        FiguraMod.LOGGER.info("Received string message:\"" + message + ".\" Ignoring, string messages are deprecated.");
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        FiguraMod.LOGGER.info("Received binary message (" + bytes.remaining() + " bytes)");
        messageHandler.acceptMessage(bytes);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (reason.isBlank())
            reason = connectionInfo.getError(code);
        FiguraMod.LOGGER.info("Closed connection: " + reason);
        FiguraMod.LOGGER.info("Code: " + code + ", Remote: " + remote);
        FiguraToast.sendToast(FiguraText.of("backend.disconnected"), reason, FiguraToast.ToastType.ERROR);
//        messageHandler.handleClose(code);
    }

    public void send(String text) {
        throw new UnsupportedOperationException("Cannot send String, only byte[]");
    }

    public void sendPacket(AbstractPacket packet) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            packet.write(dos);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Packet could not be written to DataOutputStream", e);
        }
        send(baos.toByteArray());
    }

    @Override
    public void onError(Exception ex) {
        FiguraMod.LOGGER.warn("", ex);
        FiguraToast.sendToast(FiguraText.of("backend.disconnected"), FiguraToast.ToastType.ERROR);
    }

    public void createConnectionInfo(S2CConnectedPacket packet) {
        connectionInfo = new ConnectionInfo();
    }

    public static class ConnectionInfo {
        private final Map<Integer, String> errorCodes = new HashMap<>();

        public ConnectionInfo() {
            putDefaultErrorCodes();
        }

        private void putDefaultErrorCodes() {
            errorCodes.put(1000, "Normal Closure");
            errorCodes.put(1001, "Going Away");
            errorCodes.put(1002, "Protocol Error");
            errorCodes.put(1003, "Unsupported Data");
            errorCodes.put(1005, "No Status Received");
            errorCodes.put(1006, "Abnormal Closure");
            errorCodes.put(1007, "Invalid Frame Payload Data");
            errorCodes.put(1008, "Policy Violation");
            errorCodes.put(1009, "Message Too Big");
            errorCodes.put(1010, "Mandatory Ext.");
            errorCodes.put(1011, "Internal Error");
            errorCodes.put(1012, "Service Restart");
            errorCodes.put(1013, "Try Again Later");
            errorCodes.put(1014, "Bad Gateway");
            errorCodes.put(1015, "TLS Handshake");
            errorCodes.put(3000, "Unauthorized");
            errorCodes.put(4000, "Re-Auth");
            errorCodes.put(4001, "Banned ^.^");
            errorCodes.put(4002, "Too Many Connections");
        }

        public void putErrorCode(int code, String str) {
            errorCodes.put(code, str);
        }

        public String getError(int code) {
            return errorCodes.getOrDefault(code, "Unknown");
        }

    }
}
