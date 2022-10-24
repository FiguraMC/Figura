package org.moon.figura.backend2;

import org.moon.figura.FiguraMod;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.UUID;

public class API {

    private final String token;

    public API(String token) {
        this.token = token;
    }

    private static URI getUri(String url) {
        //TODO - from config
        return URI.create("https://figura.moonlight-devs.org/api/" + url);
    }

    private HttpRequest.Builder header(String url) {
        return HttpRequest
                .newBuilder(getUri(url))
                .header("identification", FiguraMod.getLocalPlayerUUID().toString())
                .header("user-agent", FiguraMod.MOD_NAME + "/" + FiguraMod.VERSION)
                .header("authorization", token);
    }

    public HttpRequest getUser(UUID id) {
        return header(id.toString()).build();
    }
}
