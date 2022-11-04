package org.moon.figura.backend2;

import org.moon.figura.FiguraMod;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.UUID;
import org.moon.figura.avatar.Avatar;

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
                .header("user-agent", FiguraMod.MOD_NAME + "/" + FiguraMod.VERSION)
                .header("token", token);
    }

    public HttpRequest getUser(UUID id) {
        return header(id.toString()).build();
    }

    public HttpRequest getLimits() {
        return header("limits").build();
    }

    public HttpRequest getMotd() { // can go unused if you dont want this
        return header("motd").build(); // returns a string which we can set from the figura admin ui and, for example, display it in the figura wardrobe, above the avatar
    }

    public HttpRequest checkAuth() { // can go unused, i implemented it just in case
        return header("").build(); // will return 200 OK if token is valid
    }

    public HttpRequest getAvatar(UUID owner, String id) {
        return header(owner.toString() + '/' + id).build(); //TODO: store result and apply to player
    }

    public HttpRequest uploadAvatar(String id, Avatar avatar) {
        return header(id).PUT(null).build(); //TODO: needs BodyPublisher in PUT() function that sends the raw nbt data of an avatar, idk how to do it, sorry
    }

    public HttpRequest deleteAvatar(String id) {
        return header(id).DELETE().build();
    }
}
