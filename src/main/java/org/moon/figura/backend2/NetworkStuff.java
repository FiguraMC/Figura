package org.moon.figura.backend2;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.UserData;
import org.moon.figura.backend2.websocket.C2SMessageHandler;
import org.moon.figura.backend2.websocket.WebsocketThingy;
import org.moon.figura.config.Config;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.Version;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class NetworkStuff {

    protected static final HttpClient client = HttpClient.newHttpClient();
    protected static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private static final LinkedList<Request> REQUEST_QUEUE = new LinkedList<>();
    private static CompletableFuture<Void> tasks;

    protected static String token;
    protected static HttpAPI api;
    protected static WebsocketThingy backend;

    public static int backendStatus = 1;
    public static String disconnectedReason;
    public static boolean debug = true; //TODO - disable

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

    private static void queueString(UUID owner, Supplier<HttpRequest> request, BiConsumer<Integer, String> consumer) {
        REQUEST_QUEUE.add(new Request(owner, () -> api.runString(request.get(), consumer)));
    }

    private static void queueStream(UUID owner, Supplier<HttpRequest> request, BiConsumer<Integer, InputStream> consumer) {
        REQUEST_QUEUE.add(new Request(owner, () -> api.runStream(request.get(), consumer)));
    }

    public static void clear(UUID owner) {
        REQUEST_QUEUE.removeIf(request -> request.owner().equals(owner));
        unsubscribe(owner);
    }


    // -- feedback -- //


    private static void responseDebug(String src, int code, String data) {
        if (debug) FiguraMod.debug("Got response of \"" + src + "\" with code " + code + ":\n\t" + data);
    }


    // -- internal functions -- //


    public static boolean hasBackend() {
        return api != null && backend != null && backend.isOpen();
    }

    public static void closeBackend() {
        if (backend == null)
            return;

        backend.close();
        backend = null;
    }

    public static void openBackend() {
        if (backend != null)
            backend.close();

        if (token == null)
            return;

        backendStatus = 2;
        backend = new WebsocketThingy(token);
        backend.connect();
    }

    public static void ensureConnection() {
        AuthHandler.auth(false);
    }

    public static void reAuth() {
        AuthHandler.auth(true);
    }

    public static void checkAuth() {
        async(() -> {
            if (api == null) {
                AuthHandler.auth(true);
                return;
            }

            try {
                HttpResponse<Void> response = client.send(api.checkAuth(), HttpResponse.BodyHandlers.discarding());
                if (response.statusCode() != 200) {
                    AuthHandler.auth(true);
                } else if (backend == null || !backend.isOpen()) {
                    openBackend();
                }
            } catch (Exception e) {
                FiguraMod.LOGGER.error("", e);
            }
        });
    }

    public static void setLimits() {
        ensureConnection();
        async(() -> {
            if (api == null)
                return;

            api.runString(api.getLimits(), (code, data) -> {
                responseDebug("setLimits", code, data);
                JsonObject json = JsonParser.parseString(data).getAsJsonObject();
                //TODO
            });
        });
    }

    public static void checkVersion() {
        ensureConnection();
        async(() -> {
            if (api == null)
                return;

            api.runString(api.getVersion(), (code, data) -> {
                responseDebug("checkVersion", code, data);
                JsonObject json = JsonParser.parseString(data).getAsJsonObject();

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


    // -- api functions -- //


    public static void getUser(UUID id) {
        queueString(id, () -> api.getUser(id), (code, data) -> {
            //debug
            responseDebug("getUser", code, data);

            //error
            if (code != 200) {
                if (code == 404 && Config.CONNECTION_TOASTS.asBool())
                    FiguraToast.sendToast(FiguraText.of("backend.user_not_found"), FiguraToast.ToastType.ERROR);
                return;
            }

            //success

            JsonObject json = JsonParser.parseString(data).getAsJsonObject();

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
            subscribe(uuid);
        });
    }

    public static void uploadAvatar(Avatar avatar) {
        if (avatar == null || avatar.nbt == null)
            return;

        String id = avatar.id == null || true ? "avatar" : avatar.id; //TODO - profile screen

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NbtIo.writeCompressed(avatar.nbt, baos);
            queueString(Util.NIL_UUID, () -> api.uploadAvatar(id, baos.toByteArray()), (code, data) -> {
                responseDebug("uploadAvatar", code, data);

                if (!Config.CONNECTION_TOASTS.asBool())
                    return;

                switch (code) {
                    case 200 -> {
                        FiguraToast.sendToast(FiguraText.of("backend.upload_success"));
                        equipAvatar(List.of(Pair.of(avatar.owner, id))); //TODO - profile screen
                        AvatarManager.localUploaded = true; //TODO ^
                    }
                    case 413 -> FiguraToast.sendToast(FiguraText.of("backend.upload_too_big"), FiguraToast.ToastType.ERROR);
                    case 507 -> FiguraToast.sendToast(FiguraText.of("backend.upload_too_many"), FiguraToast.ToastType.ERROR);
                    default -> FiguraToast.sendToast(FiguraText.of("backend.upload_error"), FiguraToast.ToastType.ERROR);
                }
            });
            baos.close();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
    }

    public static void deleteAvatar(String avatar) {
        String id = avatar == null || true ? "avatar" : avatar; //TODO - profile screen
        queueString(Util.NIL_UUID, () -> api.deleteAvatar(id), (code, data) -> {
            responseDebug("deleteAvatar", code, data);

            if (!Config.CONNECTION_TOASTS.asBool())
                return;

            switch (code) {
                case 200 -> FiguraToast.sendToast(FiguraText.of("backend.delete_success"));
                case 404 -> FiguraToast.sendToast(FiguraText.of("backend.avatar_not_found"), FiguraToast.ToastType.ERROR);
                default -> FiguraToast.sendToast(FiguraText.of("backend.delete_error"), FiguraToast.ToastType.ERROR);
            }
        });
    }

    public static void equipAvatar(List<Pair<UUID, String>> avatars) {
        JsonArray json = new JsonArray();

        for (Pair<UUID, String> avatar : avatars) {
            JsonObject obj = new JsonObject();
            obj.addProperty("owner", avatar.getFirst().toString());
            obj.addProperty("id", avatar.getSecond());
            json.add(obj);
        }

        queueString(Util.NIL_UUID, () -> api.setEquipped(GSON.toJson(json)), (code, data) -> {
            responseDebug("equipAvatar", code, data);
            if (code != 200 && Config.CONNECTION_TOASTS.asBool())
                FiguraToast.sendToast(FiguraText.of("backend.equip_error"), FiguraToast.ToastType.ERROR);
        });
    }

    public static void getAvatar(UUID target, UUID owner, String id) {
        queueStream(Util.NIL_UUID, () -> api.getAvatar(owner, id), (code, stream) -> {
            String s;
            try {
                s = code == 200 ? "<avatar data>" : new String(stream.readAllBytes());
            } catch (Exception e) {
                s = e.getMessage();
            }
            responseDebug("getAvatar", code, s);

            //on error
            if (code != 200)
                return;

            //success
            try {
                CompoundTag nbt = NbtIo.readCompressed(stream);
                AvatarManager.setAvatar(target, nbt);
            } catch (Exception e) {
                FiguraMod.LOGGER.error("", e);
            }
        });
    }


    // -- ws functions -- //


    public static void sendPing(int id, byte sync, byte[] data) {
        if (!AvatarManager.localUploaded || !hasBackend())
            return;

        try {
            ByteBuffer buffer = C2SMessageHandler.ping(id, sync, data);
            backend.send(buffer);

            pingsSent++;
            if (lastPing == 0) lastPing = FiguraMod.ticks;
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to send ping", e);
        }
    }

    public static void subscribe(UUID id) {
        if (!hasBackend())
            return;

        try {
            ByteBuffer buffer = C2SMessageHandler.sub(id);
            backend.send(buffer);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to subscribe to " + id.toString(), e);
        }
    }

    public static void unsubscribe(UUID id) {
        if (!hasBackend())
            return;

        try {
            ByteBuffer buffer = C2SMessageHandler.unsub(id);
            backend.send(buffer);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to unsubscribe to " + id.toString(), e);
        }
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
