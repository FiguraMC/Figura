package org.moon.figura.backend2;

import com.google.gson.*;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.UserData;
import org.moon.figura.config.Config;
import org.moon.figura.gui.FiguraToast;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NetworkStuff {

    private static final HttpClient client = HttpClient.newHttpClient();
    protected static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private static final LinkedList<Request> REQUEST_QUEUE = new LinkedList<>();

    protected static API api;
    private static CompletableFuture<Void> tasks;

    public static int backendStatus = 1;
    public static String disconnectedReason;

    public static void tick() {
        AuthHandler.tick();

        //requests

        //attempt to auth if not yet
        if (!REQUEST_QUEUE.isEmpty())
            ensureConnection();

        //run requests when connected
        if (api != null) {
            Request request;
            while ((request = REQUEST_QUEUE.poll()) != null)
                async(request.toRun);
        }
    }


    // -- execute -- //


    protected static void async(Runnable toRun) {
        if (tasks == null || tasks.isDone()) {
            tasks = CompletableFuture.runAsync(toRun);
        } else {
            tasks.thenRun(toRun);
        }
    }

    private static void queueString(UUID owner, Supplier<HttpRequest> request, Consumer<String> consumer) {
        REQUEST_QUEUE.add(new Request(owner, () -> {
            try {
                HttpRequest result = request.get();
                requestDebug(result);
                HttpResponse<String> response = client.send(result, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                int i = response.statusCode();
                if (i == 200) {
                    consumer.accept(response.body());
                } else {
                    handleHTTPError(response.body());
                }
            } catch (Exception e) {
                FiguraMod.LOGGER.error("", e);
            }
        }));
    }

    private static void queueStream(UUID owner, Supplier<HttpRequest> request, Consumer<InputStream> consumer) {
        REQUEST_QUEUE.add(new Request(owner, () -> {
            try {
                HttpRequest result = request.get();
                requestDebug(result);
                HttpResponse<InputStream> response = client.send(result, HttpResponse.BodyHandlers.ofInputStream());
                int i = response.statusCode();
                if (i == 200) {
                    consumer.accept(response.body());
                } else {
                    handleHTTPError(new String(response.body().readAllBytes()));
                }
            } catch (Exception e) {
                FiguraMod.LOGGER.error("", e);
            }
        }));
    }

    public static void clear(UUID owner) {
        REQUEST_QUEUE.removeIf(request -> request.owner().equals(owner));
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

    private static void responseDebug(String msg) {
        FiguraMod.debug("Got response:\n\t" + msg);
    }


    // -- functions -- //


    public static void ensureConnection() {
        AuthHandler.auth(false);
    }

    public static void getUser(UUID id) {
        queueString(id, () -> api.getUser(id), s -> {
            responseDebug(s);
            JsonObject json = JsonParser.parseString(s).getAsJsonObject();

            //id
            UUID uuid = UUID.fromString(json.get("uuid").getAsString());

            //avatars
            ArrayList<String> avatars = new ArrayList<>();

            JsonArray equippedAvatars = json.getAsJsonArray("equipped");
            for (JsonElement element : equippedAvatars)
                avatars.add(element.getAsString());

            //badges
            JsonObject badges = json.getAsJsonObject("equippedBadges");

            JsonArray pride = badges.getAsJsonArray("pride");
            BitSet prideSet = new BitSet();
            for (int i = 0; i < pride.size(); i++)
                prideSet.set(i, pride.get(i).getAsInt() >= 1);

            JsonArray special = badges.getAsJsonArray("special");
            BitSet specialSet = new BitSet();
            for (int i = 0; i < special.size(); i++)
                specialSet.set(i, special.get(i).getAsInt() >= 1);

            UserData.loadUser(uuid, avatars, prideSet, specialSet);
        });
    }

    public static void getLimits() {}


    // -- request -- //


    private record Request(UUID owner, Runnable toRun) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o instanceof Request request && owner().equals(request.owner());
        }
    }
}
