package org.moon.figura.backend2;

import com.google.gson.*;
import org.moon.figura.avatar.Badges;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NetworkStuff {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private static API api;

    public static void init() {
        api = new API("fakeToken"); //TODO

        //temp

        getUser(UUID.fromString("66a6c5c4-963b-4b73-a0d9-162faedd8b7f"));
    }

    private static void runString(HttpRequest request, Consumer<String> consumer) {
        try {
            CompletableFuture<HttpResponse<String>> result = client.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            result.thenAccept(response -> consumer.accept(response.body()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void run(HttpRequest request, Consumer<InputStream> consumer) {
        try {
            CompletableFuture<HttpResponse<InputStream>> result = client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream());
            result.thenAccept(response -> consumer.accept(response.body()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getUser(UUID id) {
        runString(api.getUser(id), s -> {
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
}
