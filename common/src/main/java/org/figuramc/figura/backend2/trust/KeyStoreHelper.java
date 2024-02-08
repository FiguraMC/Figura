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

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

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
            return socket;
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException |
                 UnrecoverableKeyException | KeyManagementException e) {
            FiguraMod.LOGGER.error("Failed to load in the backend's certificates during Websocket creation!", e);
            NetworkStuff.disconnect("Failed to load certificates for the backend :c");
        }
        throw new WebSocketException(WebSocketError.SOCKET_CONNECT_ERROR);
    }

    // Derived from https://github.com/MinecraftForge/Installer/blob/1.x/src/main/java/net/minecraftforge/installer/FixSSL.java
    private static KeyStore getKeyStore() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        final KeyStore jdkKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        Path ksPath = Paths.get(System.getProperty("java.home"),"lib", "security", "cacerts");
        jdkKeyStore.load(Files.newInputStream(ksPath), "changeit".toCharArray());
        final Map<String, Certificate> jdkTrustStore = Collections.list(jdkKeyStore.aliases()).stream().collect(Collectors.toMap(a -> a, (String alias) -> {
            try {
                return jdkKeyStore.getCertificate(alias);
            } catch (KeyStoreException e) {
                FiguraMod.LOGGER.error("Could not find default certificates!", e);
            }
            return null;
        }));


        InputStream in = PlatformUtils.loadFileFromRoot("figurakeystore.jks");
        KeyStore figuraKeyStore = KeyStore.getInstance("JKS");
        figuraKeyStore.load(in, password);
        if (in != null) {
            in.close();
        }
        final Map<String, Certificate> figuraTrustStore = Collections.list(figuraKeyStore.aliases()).stream().collect(Collectors.toMap(a -> a, (String alias) -> {
            try {
                return figuraKeyStore.getCertificate(alias);
            } catch (KeyStoreException e) {
                FiguraMod.LOGGER.error("Could not find default certificates!", e);
            }
            return null;
        }));

        final KeyStore mergedTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        mergedTrustStore.load(null, new char[0]);
        for (Map.Entry<String, Certificate> entry : jdkTrustStore.entrySet()) {
            mergedTrustStore.setCertificateEntry(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String , Certificate> entry : figuraTrustStore.entrySet()) {
            mergedTrustStore.setCertificateEntry(entry.getKey(), entry.getValue());
        }

        return mergedTrustStore;
    }
}