package org.figuramc.figura.backend2.trust;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketError;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.HttpClients;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.backend2.websocket.FiguraWebSocketAdapter;
import org.figuramc.figura.utils.PlatformUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

public class KeyStoreHelper {

    private static final char[] password = "figuramc".toCharArray();

    public static HttpClient httpClientWithBackendCertificates() {
        FiguraMod.LOGGER.info("Initializing custom http client");
        try {
            KeyStore keyStore = getKeyStore();

            SSLContextBuilder contextBuilder = SSLContexts.custom();
            contextBuilder.loadKeyMaterial(keyStore, password, null);
            contextBuilder.loadTrustMaterial(keyStore); // Not fully sure what the difference is, better to do both just in case
            return HttpClients.custom().setSslcontext(contextBuilder.build()).build();
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException |
                 KeyManagementException | UnrecoverableKeyException e) {
            FiguraMod.LOGGER.error("Failed to load in the backend's certificates during http client creation!", e);
        }
        return HttpClients.createDefault();
    }

    public static WebSocket websocketWithBackendCertificates(String token) throws WebSocketException {
        FiguraMod.LOGGER.info("Initializing custom websocket");
        try {
            KeyStore keyStore = getKeyStore();
            WebSocketFactory wsFactory = new WebSocketFactory();

            SSLContextBuilder contextBuilder = SSLContexts.custom();
            contextBuilder.loadKeyMaterial(keyStore, password);
            contextBuilder.loadTrustMaterial(keyStore);
            wsFactory.setSocketFactory(contextBuilder.build().getSocketFactory());
            wsFactory.setVerifyHostname(false);
            WebSocket socket = wsFactory.createSocket(FiguraWebSocketAdapter.getBackendAddress());
            socket.addListener(new FiguraWebSocketAdapter(token));
            return socket;
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | CertificateException |
                 UnrecoverableKeyException | KeyManagementException e) {
            FiguraMod.LOGGER.error("Failed to load in the backend's certificates during Websocket creation!", e);
        }
        throw new WebSocketException(WebSocketError.SOCKET_CONNECT_ERROR);
    }

    private static KeyStore getKeyStore() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        InputStream in = PlatformUtils.loadFileFromRoot("figurakeystore.jks");

        // Create the socket with the custom context and keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(in, password);
        if (in != null) {
            in.close();
        }
        return keyStore;
    }
}
