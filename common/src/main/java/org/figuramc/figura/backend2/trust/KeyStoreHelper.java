package org.figuramc.figura.backend2.trust;

import com.neovisionaries.ws.client.*;
import net.minecraft.client.multiplayer.ServerAddress;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.HttpClients;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.backend2.websocket.FiguraWebSocketAdapter;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.utils.PlatformUtils;
import org.luaj.vm2.ast.Str;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
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
            contextBuilder.useProtocol("TLSv1.2");
            contextBuilder.loadKeyMaterial(keyStore, password);
            contextBuilder.loadTrustMaterial(keyStore);
            SSLContext context = contextBuilder.build();
            wsFactory.setSocketFactory(context.getSocketFactory());
            wsFactory.setSSLSocketFactory(context.getSocketFactory());
            wsFactory.setSSLContext(context);
            String serverName = ServerAddress.parseString(Configs.SERVER_IP.value).getHost();
            wsFactory.setServerName(serverName);
            WebSocket socket = wsFactory.createSocket(FiguraWebSocketAdapter.getBackendAddress());
            socket.addListener(new FiguraWebSocketAdapter(token));
            socket.removeProtocol("TLSv1");
            socket.removeProtocol("TLSv1.1");
            socket.clearProtocols();
            socket.addProtocol("TLSv1.2");
            socket.addExtension(WebSocketExtension.PERMESSAGE_DEFLATE);
            Socket sock = socket.getConnectedSocket();
            if (sock instanceof SSLSocket) {
                ((SSLSocket)sock).setEnabledProtocols(new String[]{"TLSv1.2"});
                Method getHost = sock.getClass().getDeclaredMethod("getHost");
                getHost.setAccessible(true);
                Object obj = getHost.invoke(sock);
                System.out.println("IMPORTANT HOSTNAME: " + ((String) obj));
                Method setHostname = sock.getClass().getDeclaredMethod("setHost", String.class);
                setHostname.setAccessible(true);
                setHostname.invoke(sock, serverName);
            }
            return socket;
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException |
                 UnrecoverableKeyException | KeyManagementException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            FiguraMod.LOGGER.error("Failed to load in the backend's certificates during Websocket creation!", e);
            NetworkStuff.disconnect("Failed to load certificates for the backend :c");
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
