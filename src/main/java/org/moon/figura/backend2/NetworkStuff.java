package org.moon.figura.backend2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.UserData;
import org.moon.figura.config.Config;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.Version;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NetworkStuff {

    protected static final HttpClient client = HttpClient.newHttpClient();

    private static final LinkedList<Request> REQUEST_QUEUE = new LinkedList<>();

    protected static API api;
    private static CompletableFuture<Void> tasks;

    public static int backendStatus = 1;
    public static String disconnectedReason;

    public static int lastPing, pingsSent, pingsReceived;

    public static void tick() {
        AuthHandler.tick();

        //requests
        if (hasBackend() && !REQUEST_QUEUE.isEmpty()) {
            async(() -> {
                if (!hasBackend()) //cursed
                    return;

                Request request;
                while ((request = REQUEST_QUEUE.poll()) != null)
                    async(request.toRun);
            });
        }

        //pings counter
        if (lastPing > 0 && FiguraMod.ticks - lastPing >= 20)
            lastPing = pingsSent = pingsReceived = 0;
    }


    // -- execute -- //


    protected static void async(Runnable toRun) {
        if (tasks == null || tasks.isDone()) {
            tasks = CompletableFuture.runAsync(toRun);
        } else {
            tasks.thenRun(toRun);
        }
    }

    private static void queue(Supplier<HttpRequest> request) {
        REQUEST_QUEUE.add(new Request(Util.NIL_UUID, () -> api.run(request.get())));
    }

    private static void queueString(UUID owner, Supplier<HttpRequest> request, Consumer<String> consumer) {
        REQUEST_QUEUE.add(new Request(owner, () -> api.runString(request.get(), consumer)));
    }

    private static void queueStream(UUID owner, Supplier<HttpRequest> request, Consumer<InputStream> consumer) {
        REQUEST_QUEUE.add(new Request(owner, () -> api.runStream(request.get(), consumer)));
    }

    public static void clear(UUID owner) {
        REQUEST_QUEUE.removeIf(request -> request.owner().equals(owner));
    }


    // -- feedback -- //


    private static void responseDebug(String msg) {
        FiguraMod.debug("Got response:\n\t" + msg);
    }


    // -- functions -- //


    public static boolean hasBackend() {
        return api != null;
    }

    public static void ensureConnection() {
        AuthHandler.auth(false);
    }

    public static void reAuth() {
        AuthHandler.auth(true);
    }

    public static void checkAuth() {
        async(() -> {
            if (!hasBackend()) {
                AuthHandler.auth(true);
                return;
            }

            try {
                HttpResponse<Void> response = client.send(api.checkAuth(), HttpResponse.BodyHandlers.discarding());
                if (response.statusCode() != 200)
                    AuthHandler.auth(true);
            } catch (Exception e) {
                FiguraMod.LOGGER.error("", e);
            }
        });
    }

    public static void setLimits() {
        ensureConnection();
        async(() -> {
            if (!hasBackend())
                return;

            api.runString(api.getLimits(), s -> {
                responseDebug(s);
                JsonObject json = JsonParser.parseString(s).getAsJsonObject();
                //TODO
            });
        });
    }

    public static void checkVersion() {
        ensureConnection();
        async(() -> {
            if (!hasBackend())
                return;

            api.runString(api.getVersion(), s -> {
                responseDebug(s);
                JsonObject json = JsonParser.parseString(s).getAsJsonObject();

                int config = Config.UPDATE_CHANNEL.asInt();
                if (config != 0) {
                    String version = json.get(config == 1 ? "release" : "prerelease").getAsString();
                    if (new Version(version).compareTo(Version.VERSION) > 0)
                        FiguraToast.sendToast(FiguraText.of("toast.new_version"), version);
                }
            });
        });
    }

    public static boolean canUpload() {
        return true; //TODO - limits
    }

    public static int getSizeLimit() {
        return Integer.MAX_VALUE; //TODO - limits
    }


    // -- user functions -- //


    public static void getUser(UUID id) {
        queueString(id, () -> api.getUser(id), s -> {
            responseDebug(s);
            JsonObject json = JsonParser.parseString(s).getAsJsonObject();

            //id
            UUID uuid = UUID.fromString(json.get("uuid").getAsString());

            //avatars
            ArrayList<Pair<String, UUID>> avatars = new ArrayList<>();

            JsonArray equippedAvatars = json.getAsJsonArray("equipped");
            for (JsonElement element : equippedAvatars) {
                JsonObject entry = element.getAsJsonObject();
                UUID owner = UUID.fromString(entry.get("owner").getAsString());
                avatars.add(Pair.of(entry.get("id").getAsString(), owner));
            }

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

            UserData.loadUser(uuid, avatars, Pair.of(prideSet, specialSet));
        });
    }

    public static void uploadAvatar(Avatar avatar) {
        if (avatar == null || avatar.nbt == null)
            return;

        String id = avatar.id == null || true ? "avatar" : avatar.id; //TODO - profile screen

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NbtIo.writeCompressed(avatar.nbt, baos);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            queue(() -> api.uploadAvatar(id, () -> bais));

            //bais is automatically closed
            baos.close();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
    }

    public static void deleteAvatar(String avatar) {
        String id = avatar == null || true ? "avatar" : avatar; //TODO - profile screen
        queue(() -> api.deleteAvatar(id));
    }

    public static void getAvatar(UUID target, UUID owner, String id) {
        queueStream(Util.NIL_UUID, () -> api.getAvatar(owner, id), stream -> {
            try {
                CompoundTag nbt = NbtIo.readCompressed(stream);
                AvatarManager.setAvatar(target, nbt);
            } catch (Exception e) {
                FiguraMod.LOGGER.error("", e);
            }
        });
    }

    public static void sendPing(int id, boolean sync, byte[] data) { //TODO - events
        pingsSent++;
        if (lastPing == 0) lastPing = FiguraMod.ticks;
    }


    // -- request -- //


    private record Request(UUID owner, Runnable toRun) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o instanceof Request request && owner().equals(request.owner());
        }
    }
}
