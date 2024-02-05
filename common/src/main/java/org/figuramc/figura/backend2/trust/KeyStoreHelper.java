package org.figuramc.figura.backend2.trust;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketError;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.backend2.websocket.FiguraWebSocketAdapter;
import org.figuramc.figura.utils.PlatformUtils;

import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class KeyStoreHelper {

    private static final char[] password = "figuramc".toCharArray();

    public static WebSocket websocketWithBackendCertificates(String token) throws WebSocketException {
        FiguraMod.LOGGER.info("Initializing custom websocket");
        try {
            KeyStore keyStore = getKeyStore();
            WebSocketFactory wsFactory = new WebSocketFactory();
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, password);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keyStore);

            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            wsFactory.setSocketFactory(context.getSocketFactory());
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
