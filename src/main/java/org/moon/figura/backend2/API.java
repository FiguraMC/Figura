package org.moon.figura.backend2;

import org.moon.figura.FiguraMod;
import org.moon.figura.config.Config;
import org.moon.figura.gui.FiguraToast;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class API {

    private final String token;

    public API(String token) {
        this.token = token;
    }


    // -- builders -- //


    private static URI getUri(String url) {
        //TODO - from config
        return URI.create("https://figura.moonlight-devs.org/api/" + url);
    }

    protected HttpRequest.Builder header(String url) {
        return HttpRequest
                .newBuilder(getUri(url))
                .header("user-agent", FiguraMod.MOD_NAME + "/" + FiguraMod.VERSION)
                .header("token", token);
    }


    // -- runners -- //


    public void run(HttpRequest request) {
        try {
            requestDebug(request);
            HttpResponse<String> response = NetworkStuff.client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) handleHTTPError(response.body());
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
    }

    public void runString(HttpRequest request, Consumer<String> consumer) {
        try {
            requestDebug(request);
            HttpResponse<String> response = NetworkStuff.client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                consumer.accept(response.body());
            } else {
                handleHTTPError(response.body());
            }
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
    }

    public void runStream(HttpRequest request, Consumer<InputStream> consumer) {
        try {
            requestDebug(request);
            HttpResponse<InputStream> response = NetworkStuff.client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() == 200) {
                consumer.accept(response.body());
            } else {
                handleHTTPError(new String(response.body().readAllBytes()));
            }
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
    }


    // -- feedback -- //


    private static void handleHTTPError(String error) {
        if (Config.CONNECTION_TOASTS.asBool())
            FiguraToast.sendToast(error, FiguraToast.ToastType.ERROR);
        else
            FiguraMod.LOGGER.warn(error);
    }

    private static void requestDebug(HttpRequest msg) {
        FiguraMod.debug( "Sent Http request:\n\t" + msg.uri().toString() + "\n\t" + msg.headers().map().toString());
    }


    // -- accessors -- //


    // will return 200 OK if token is valid
    public HttpRequest checkAuth() {
        return header("").build();
    }

    public HttpRequest getUser(UUID id) {
        return header(id.toString()).build();
    }

    public HttpRequest getLimits() {
        return header("limits").build();
    }

    public HttpRequest getVersion() {
        return header("version").build();
    }

    public HttpRequest getMotd() {
        return header("motd").build();
    }

    public HttpRequest getAvatar(UUID owner, String id) {
        return header(owner.toString() + '/' + id).build();
    }

    public HttpRequest uploadAvatar(String id, Supplier<InputStream> stream) {
        return header(id).PUT(HttpRequest.BodyPublishers.ofInputStream(stream)).build();
    }

    public HttpRequest deleteAvatar(String id) {
        return header(id).DELETE().build();
    }
}
