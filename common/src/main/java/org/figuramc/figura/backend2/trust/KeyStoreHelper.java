package org.figuramc.figura.backend2.trust;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.HttpClients;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.backend2.websocket.WebsocketThingy;
import org.figuramc.figura.utils.PlatformUtils;

import javax.net.ssl.*;
import java.io.*;
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

    public static WebsocketThingy websocketWithBackendCertificates(String token) {
        FiguraMod.LOGGER.info("Initializing custom websocket");
        try {
            KeyStore keyStore = getKeyStore();

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, password);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            SSLSocketFactory factory = sslContext.getSocketFactory();
            WebsocketThingy websocketThingy = new WebsocketThingy(token);
            websocketThingy.setSocketFactory(factory);
            return websocketThingy;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | NoSuchAlgorithmException |
                 IOException | KeyManagementException e) {
            FiguraMod.LOGGER.error("Failed to load in the backend's certificates during Websocket creation!", e);
        }
        return new WebsocketThingy(token);
    }

    private static KeyStore getKeyStore() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        InputStream in = PlatformUtils.loadFileFromRoot("figurakeystore");

        // Create the socket with the custom context and keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(in, password);
        if (in != null) {
            in.close();
        }
        return keyStore;
    }
}
