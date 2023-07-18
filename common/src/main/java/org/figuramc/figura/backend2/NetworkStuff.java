package org.moon.figura.backend2;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.Badges;
import org.moon.figura.avatar.UserData;
import org.moon.figura.avatar.local.CacheAvatarLoader;
import org.moon.figura.backend2.websocket.C2SMessageHandler;
import org.moon.figura.backend2.websocket.WebsocketThingy;
import org.moon.figura.config.Configs;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.RefilledNumber;
import org.moon.figura.utils.Version;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class NetworkStuff {

    protected static final HttpClient client = HttpClient.newHttpClient();
    protected static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private static final ConcurrentLinkedQueue<Request<HttpAPI>> API_REQUESTS = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<Request<WebsocketThingy>> WS_REQUESTS = new ConcurrentLinkedQueue<>();
    private static final List<UUID> SUBSCRIPTIONS = new ArrayList<>();
    private static CompletableFuture<Void> tasks;

    private static final int RECONNECT = 6000; //5 min
    private static int authCheck = RECONNECT;

    protected static HttpAPI api;
    protected static WebsocketThingy ws;

    public static int backendStatus = 1;
    public static String disconnectedReason;

    public static boolean debug = false;

    public static int lastPing, pingsSent, pingsReceived;

    public static Version latestVersion;

    //limits
    private static final RefilledNumber
            uploadRate = new RefilledNumber(),
            downloadRate = new RefilledNumber();
    private static int maxAvatarSize = Integer.MAX_VALUE;

    public static void tick() {
        //limits
        uploadRate.tick();
        downloadRate.tick();

        //auth check
        authCheck--;
        if (authCheck <= 0) {
            authCheck = RECONNECT;

            if (!isConnected())
                reAuth();
            else if (!checkWS())
                reAuth();
            else
                checkAPI();
        }

        tickSubscriptions();

        //process requests
        if (isConnected())
            processRequests();

        //pings counter
        if (lastPing > 0 && FiguraMod.ticks - lastPing >= 20)
            lastPing = pingsSent = pingsReceived = 0;
    }

    private static void tickSubscriptions() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            unsubscribeAll();
            return;
        }

        List<UUID> unsub = new ArrayList<>(SUBSCRIPTIONS);
        for (UUID uuid : connection.getOnlinePlayerIds()) {
            unsub.remove(uuid);
            if (!SUBSCRIPTIONS.contains(uuid)) {
                SUBSCRIPTIONS.add(uuid);
                subscribe(uuid);
            }
        }

        for (UUID uuid : unsub) {
            SUBSCRIPTIONS.remove(uuid);
            unsubscribe(uuid);
        }
    }

    private static void processRequests() {
        if (!API_REQUESTS.isEmpty()) {
            Request<HttpAPI> request;
            while ((request = API_REQUESTS.poll()) != null) {
                Request<HttpAPI> finalRequest = request;
                async(() -> finalRequest.consumer.accept(api));
            }
        }

        if (!WS_REQUESTS.isEmpty()) {
            Request<WebsocketThingy> request;
            while ((request = WS_REQUESTS.poll()) != null) {
                Request<WebsocketThingy> finalRequest = request;
                async(() -> finalRequest.consumer.accept(ws));
            }
        }
    }

    protected static void async(Runnable toRun) {
        if (tasks == null || tasks.isDone()) {
            tasks = CompletableFuture.runAsync(toRun);
        } else {
            tasks.thenRun(toRun);
        }
    }

    private static boolean checkUUID(UUID id) {
        if (id.version() != 4) {
            FiguraMod.debug("Voiding request for non v4 UUID \"" + id + "\" (v" + id.version() + ")");
            return true;
        }
        return false;
    }


    // -- token -- //


    public static void auth() {
        authCheck = RECONNECT;
        AuthHandler.auth(false);
    }

    public static void reAuth() {
        authCheck = RECONNECT;
        AuthHandler.auth(true);
    }

    protected static void authSuccess(String token) {
        FiguraMod.LOGGER.info("Successfully authed with the " + FiguraMod.MOD_NAME + " auth server!");
        disconnectedReason = null;
        connect(token);
    }

    protected static void authFail(String reason) {
        FiguraMod.LOGGER.warn("Failed to auth with the " + FiguraMod.MOD_NAME + " auth server! {}", reason == null ? "" : reason);
        disconnect(reason);
    }


    // -- connection -- //


    public static void connect(String token) {
        if (isConnected())
            disconnect(null);

        backendStatus = 2;
        connectAPI(token);
        connectWS(token);
    }

    public static void disconnect(String reason) {
        backendStatus = 1;
        disconnectedReason = reason;
        disconnectAPI();
        disconnectWS();
    }


    // -- api stuff -- //


    private static void queueString(UUID owner, Function<HttpAPI, HttpRequest> request, BiConsumer<Integer, String> consumer) {
        API_REQUESTS.add(new Request<>(owner, api -> HttpAPI.runString(request.apply(api), consumer)));
    }

    private static void queueStream(UUID owner, Function<HttpAPI, HttpRequest> request, BiConsumer<Integer, InputStream> consumer) {
        API_REQUESTS.add(new Request<>(owner, api -> HttpAPI.runStream(request.apply(api), consumer)));
    }

    public static void clear(UUID requestOwner) {
        API_REQUESTS.removeIf(request -> request.owner.equals(requestOwner));
    }

    private static void responseDebug(String src, int code, String data) {
        if (debug) FiguraMod.debug("Got response of \"" + src + "\" with code " + code + ":\n\t" + data);
    }

    private static void connectAPI(String token) {
        api = new HttpAPI(token);
        checkVersion();
        setLimits();
    }

    private static void disconnectAPI() {
        api = null;
        clear(Util.NIL_UUID);
    }

    private static void checkAPI() {
        async(() -> {
            if (api == null) {
                reAuth();
                return;
            }

            HttpAPI.runString(api.checkAuth(), (code, data) -> {
                if (code != 200)
                    reAuth();
            });
        });
    }

    public static void checkVersion() {
        queueString(Util.NIL_UUID, HttpAPI::getVersion, (code, data) -> {
            responseDebug("checkVersion", code, data);
            JsonObject json = JsonParser.parseString(data).getAsJsonObject();
            latestVersion = new Version(json.get("prerelease").getAsString());

            int config = Configs.UPDATE_CHANNEL.value;
            if (config != 0) {
                Version compare = config == 1 ? new Version(json.get("release").getAsString()) : latestVersion;
                if (compare.compareTo(FiguraMod.VERSION) > 0)
                    FiguraToast.sendToast(new FiguraText("toast.new_version"), compare);
            }
        });
    }

    public static void setLimits() {
        queueString(Util.NIL_UUID, HttpAPI::getLimits, (code, data) -> {
            responseDebug("setLimits", code, data);
            JsonObject json = JsonParser.parseString(data).getAsJsonObject();

            JsonObject rate = json.getAsJsonObject("rate");
            uploadRate.set(rate.get("upload").getAsInt() * 0.95);
            downloadRate.set(rate.get("download").getAsInt() * 0.95);

            JsonObject limits = json.getAsJsonObject("limits");
            maxAvatarSize = limits.get("maxAvatarSize").getAsInt();
        });
    }

    public static void getUser(UserData user) {
        if (checkUUID(user.id))
            return;

        queueString(user.id, api -> api.getUser(user.id), (code, data) -> {
            //debug
            responseDebug("getUser", code, data);

            //error
            if (code != 200) {
                if (code == 404 && Configs.CONNECTION_TOASTS.value)
                    FiguraToast.sendToast(new FiguraText("backend.user_not_found"), FiguraToast.ToastType.ERROR);
                return;
            }

            //success

            JsonObject json = JsonParser.parseString(data).getAsJsonObject();

            //avatars
            ArrayList<Pair<String, Pair<String, UUID>>> avatars = new ArrayList<>();

            JsonArray equippedAvatars = json.getAsJsonArray("equipped");
            for (JsonElement element : equippedAvatars) {
                JsonObject entry = element.getAsJsonObject();
                UUID owner = UUID.fromString(entry.get("owner").getAsString());
                avatars.add(Pair.of(entry.get("hash").getAsString(), Pair.of(entry.get("id").getAsString(), owner)));
            }

            //badges
            JsonObject badges = json.getAsJsonObject("equippedBadges");
            Pair<BitSet, BitSet> badgesPair = Badges.emptyBadges();

            JsonArray pride = badges.getAsJsonArray("pride");
            BitSet prideSet = badgesPair.getFirst();
            for (int i = 0; i < pride.size(); i++)
                prideSet.set(i, pride.get(i).getAsInt() >= 1);

            JsonArray special = badges.getAsJsonArray("special");
            BitSet specialSet = badgesPair.getSecond();
            for (int i = 0; i < special.size(); i++)
                specialSet.set(i, special.get(i).getAsInt() >= 1);

            user.loadData(avatars, badgesPair);
        });
    }

    public static void uploadAvatar(Avatar avatar) {
        if (avatar == null || avatar.nbt == null)
            return;

        String id = avatar.id == null || true ? "avatar" : avatar.id; //TODO - profile screen

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NbtIo.writeCompressed(avatar.nbt, baos);
            queueString(Util.NIL_UUID, api -> api.uploadAvatar(id, baos.toByteArray()), (code, data) -> {
                responseDebug("uploadAvatar", code, data);

                if (code == 200) {
                    //TODO - profile screen
                    equipAvatar(List.of(Pair.of(avatar.owner, id)));
                    AvatarManager.localUploaded = true;
                }

                //feedback
                switch (code) {
                    case 200 -> FiguraToast.sendToast(new FiguraText("backend.upload_success"));
                    case 413 -> FiguraToast.sendToast(new FiguraText("backend.upload_too_big"), FiguraToast.ToastType.ERROR);
                    case 507 -> FiguraToast.sendToast(new FiguraText("backend.upload_too_many"), FiguraToast.ToastType.ERROR);
                    default -> FiguraToast.sendToast(new FiguraText("backend.upload_error"), FiguraToast.ToastType.ERROR);
                }
            });
            uploadRate.use();
            baos.close();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
    }

    public static void deleteAvatar(String avatar) {
        String id = avatar == null || true ? "avatar" : avatar; //TODO - profile screen
        queueString(Util.NIL_UUID, api -> api.deleteAvatar(id), (code, data) -> {
            responseDebug("deleteAvatar", code, data);

            switch (code) {
                case 200 -> FiguraToast.sendToast(new FiguraText("backend.delete_success"));
                case 404 -> FiguraToast.sendToast(new FiguraText("backend.avatar_not_found"), FiguraToast.ToastType.ERROR);
                default -> FiguraToast.sendToast(new FiguraText("backend.delete_error"), FiguraToast.ToastType.ERROR);
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

        queueString(Util.NIL_UUID, api -> api.setEquipped(GSON.toJson(json)), (code, data) -> {
            responseDebug("equipAvatar", code, data);
            if (code != 200 && Configs.CONNECTION_TOASTS.value)
                FiguraToast.sendToast(new FiguraText("backend.equip_error"), FiguraToast.ToastType.ERROR);
        });
    }

    public static void getAvatar(UserData target, UUID owner, String id, String hash) {
        if (checkUUID(target.id))
            return;

        queueStream(target.id, api -> api.getAvatar(owner, id), (code, stream) -> {
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
                CacheAvatarLoader.save(hash, nbt);
                target.loadAvatar(nbt);
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to load avatar for " + target.id, e);
            }
        });
        downloadRate.use();
    }


    // -- ws stuff -- //


    private static void connectWS(String token) {
        if (ws != null) ws.close();
        ws = new WebsocketThingy(token);
        ws.connect();
    }

    private static void disconnectWS() {
        if (ws != null) ws.close();
        ws = null;
    }

    private static boolean checkWS() {
        return ws != null && ws.isOpen() && backendStatus == 3;
    }

    public static void sendPing(int id, boolean sync, byte[] data) {
        if (!AvatarManager.localUploaded || !isConnected())
            return;

        try {
            ByteBuffer buffer = C2SMessageHandler.ping(id, sync, data);
            ws.send(buffer);

            pingsSent++;
            if (lastPing == 0) lastPing = FiguraMod.ticks;
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to send ping", e);
        }
    }

    private static void subscribe(UUID id) {
        if (checkUUID(id) || !checkWS())
            return;

        WS_REQUESTS.add(new Request<>(Util.NIL_UUID, client -> {
            try {
                ByteBuffer buffer = C2SMessageHandler.sub(id);
                client.send(buffer);
                if (debug) FiguraMod.debug("Subbed to " + id);
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to subscribe to " + id, e);
            }
        }));
    }

    private static void unsubscribe(UUID id) {
        if (checkUUID(id) || !checkWS())
            return;

        WS_REQUESTS.add(new Request<>(Util.NIL_UUID, client -> {
            try {
                ByteBuffer buffer = C2SMessageHandler.unsub(id);
                client.send(buffer);
                if (debug) FiguraMod.debug("Unsubbed to " + id);
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to unsubscribe to " + id, e);
            }
        }));
    }

    public static void subscribeAll() {
        for (UUID uuid : SUBSCRIPTIONS)
            subscribe(uuid);
    }

    public static void unsubscribeAll() {
        for (UUID uuid : SUBSCRIPTIONS)
            unsubscribe(uuid);
        SUBSCRIPTIONS.clear();
    }


    // -- resources stuff -- //


    private static InputStream request(HttpRequest request) throws Exception {
        HttpResponse<InputStream> response = NetworkStuff.client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        return response.body();
    }

    public static InputStream getResourcesHashes(String version) throws Exception {
        return request(HttpRequest.newBuilder(HttpAPI.getUri("/assets/" + version)).timeout(Duration.ofSeconds(15)).build());
    }

    public static InputStream getResource(String version, String resource) throws Exception {
        return request(HttpRequest.newBuilder(HttpAPI.getUri("/assets/" + version + "/" + resource)).build());
    }


    // -- global functions -- //


    public static boolean isConnected() {
        return api != null && checkWS();
    }

    public static boolean canUpload() {
        return isConnected() && uploadRate.check();
    }

    public static int getSizeLimit() {
        return isConnected() ? maxAvatarSize : Integer.MAX_VALUE;
    }


    // -- request subclass -- //


    private record Request<T>(UUID owner, Consumer<T> consumer) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o instanceof Request request && owner.equals(request.owner);
        }
    }
}
