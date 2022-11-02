package org.moon.figura.backend2;

import com.google.gson.*;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Badges;
import org.moon.figura.config.Config;
import org.moon.figura.gui.FiguraToast;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NetworkStuff {

    private static final HttpClient client = HttpClient.newHttpClient();
    protected static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    protected static API api;
    private static CompletableFuture<Void> tasks;

    public static int backendStatus = 1;
    public static String disconnectedReason;

    public static void init() {
        getUser(UUID.fromString("66a6c5c4-963b-4b73-a0d9-162faedd8b7f"));
    }

    public static void tick() {
        AuthHandler.tick();
    }

    protected static void async(Runnable toRun) {
        if (tasks == null || tasks.isDone()) {
            tasks = CompletableFuture.runAsync(toRun);
        } else {
            tasks.thenRun(toRun);
        }
    }

    private static void runString(Supplier<HttpRequest> request, Consumer<String> consumer) {
        async(() -> {
            if (api == null)
                return;

            try {
                HttpRequest result = request.get();
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
        });
    }

    private static void run(Supplier<HttpRequest> request, Consumer<InputStream> consumer) {
        async(() -> {
            if (api == null)
                return;

            try {
                HttpRequest result = request.get();
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
        });
    }

    private static void handleHTTPError(String error) {
        if (Config.CONNECTION_TOASTS.asBool())
            FiguraToast.sendToast(error, FiguraToast.ToastType.ERROR);
        else
            FiguraMod.LOGGER.warn(error);
    }

    private static void ensureConnection() {
        AuthHandler.auth(false);
    }

    public static void getUser(UUID id) {
        ensureConnection();
        runString(() -> api.getUser(id), s -> {
            System.out.println(s);
            JsonObject json = JsonParser.parseString(s).getAsJsonObject();

            UUID uuid = UUID.fromString(json.get("uuid").getAsString());
            JsonObject badges = json.getAsJsonObject("equippedBadges");

            JsonArray pride = badges.getAsJsonArray("pride");
            BitSet prideSet = new BitSet();
            for (int i = 0; i < pride.size(); i++)
                prideSet.set(i, pride.get(i).getAsInt() >= 1);

            JsonArray special = badges.getAsJsonArray("special");
            BitSet specialSet = new BitSet();
            for (int i = 0; i < special.size(); i++)
                specialSet.set(i, special.get(i).getAsInt() >= 1);

            Badges.load(uuid, prideSet, specialSet);
        });
    }

    public static void getLimits() {
        ensureConnection();
        runString(() -> api.getLimits(), s -> {});
    }
}
