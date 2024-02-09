package org.figuramc.figura.backend2;

import net.minecraft.client.multiplayer.ServerAddress;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.Configs;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class HttpAPI {

    private final String token;

    public HttpAPI(String token) {
        this.token = token;
    }


    // -- builders -- // 


    protected static URI getUri(String url) {
        return URI.create(getBackendAddress() + "/" + url);
    }

    protected static String getBackendAddress() {
        ServerAddress backendIP = ServerAddress.parseString(Configs.SERVER_IP.value);
        return "https://" + backendIP.getHost() + "/api";
    }

    protected HttpRequestBase header(String url) {
        return header(url, "");
    }

    protected HttpRequestBase header(String url, String requestType) {
        try {
            URI uri = new URIBuilder(getUri(url)).build();
            HttpRequestBase request;
            switch (requestType.toUpperCase()) {
                case "PUT":
                    request = new HttpPut(uri);
                    break;
                case "POST":
                    request = new HttpPost(uri);
                    break;
                case "DELETE":
                    request = new HttpDelete(uri);
                    break;
                case "TRACE":
                    request = new HttpTrace(uri);
                    break;
                case "OPTIONS":
                    request = new HttpOptions(uri);
                    break;
                case "PATCH":
                    request = new HttpPatch(uri);
                    break;
                default:
                    request = new HttpGet(uri);
                    break;
            }
            request.setHeader("user-agent", FiguraMod.MOD_NAME + "/" + FiguraMod.VERSION);
            request.setHeader("token", token);
            return request;
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Malformed URI, " + url +  ", ", e);
            return null;
        }
    }


    // -- runners -- // 


    protected static void runString(HttpRequest rq, BiConsumer<Integer, String> consumer) {
        try {
            HttpUriRequest request = (HttpUriRequest) rq;
            requestDebug(request);
            HttpResponse response = NetworkStuff.client.execute(request);
            int code = response.getStatusLine().getStatusCode();
            if (code == 401) NetworkStuff.reAuth();
            consumer.accept(code, EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            if (!e.getMessage().contains("GOAWAY received"))
                FiguraMod.LOGGER.error("", e);
        }
    }

    protected static void runStream(HttpRequest rq, BiConsumer<Integer, InputStream> consumer) {
        try {
            HttpUriRequest request = (HttpUriRequest) rq;
            requestDebug(request);
            HttpResponse response = NetworkStuff.client.execute(request);
            int code = response.getStatusLine().getStatusCode();
            if (code == 401) NetworkStuff.reAuth();
            consumer.accept(code, response.getEntity().getContent());
        } catch (Exception e) {
            if (!e.getMessage().contains("GOAWAY received"))
                FiguraMod.LOGGER.error("", e);
        }
    }


    // -- feedback -- // 


    private static void requestDebug(HttpUriRequest msg) {
        if (NetworkStuff.debug)
            FiguraMod.debug("Sent Http request:\n\t" + msg.getURI().toString() + "\n\t" + Arrays.stream(msg.getAllHeaders()).collect(Collectors.toMap(Header::getName, header -> Arrays.stream(header.getElements()).map(HeaderElement::getValue).collect(Collectors.toList()))));
    }


    // -- accessors -- // 


    // will return 200 OK if token is valid
    public HttpRequest checkAuth() {
        return header("");
    }

    public HttpRequest getUser(UUID id) {
        return header(id.toString());
    }

    public HttpRequest getLimits() {
        return header("limits");
    }

    public HttpRequest getVersion() {
        return header("version");
    }

    public HttpRequest getMotd() {
        return header("motd");
    }

    public HttpRequest getAvatar(UUID owner, String id) {
        return header(owner.toString() + '/' + id);
    }

    public HttpRequest uploadAvatar(String id, byte[] bytes) {
        HttpPut put = (HttpPut) header(id, "PUT");
        put.setHeader("Content-Type", "application/octet-stream");
        put.setEntity(new ByteArrayEntity(bytes));
        return put;
    }

    public HttpRequest deleteAvatar(String id) {
        return header(id, "DELETE");
    }

    public HttpRequest setEquipped(String json) {
        HttpPost post = (HttpPost) header("equip", "POST");
        post.setHeader("Content-Type", "application/json");
        try {
            post.setEntity(new StringEntity(json));
        } catch (UnsupportedEncodingException e) {
            FiguraMod.LOGGER.error("Failed to encode string, ", e);
        }
        return post;
    }
}
