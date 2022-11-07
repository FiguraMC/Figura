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
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class NetworkStuff {

    protected static final HttpClient client = HttpClient.newHttpClient();
    protected static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private static final LinkedList<HTTPRequest> API_REQUESTS = new LinkedList<>();
    private static final LinkedList<WSRequest> WS_REQUESTS = new LinkedList<>();
    private static CompletableFuture<Void> tasks;

    private static final int RECONNECT = 6000; //5 min
    private static int authCheck = RECONNECT;

    protected static HttpAPI api;
    protected static WebsocketThingy ws;

    public static int backendStatus = 1;
    public static String disconnectedReason;

    public static boolean debug = true; //TODO - disable

    public static int lastPing, pingsSent, pingsReceived;

    public static void tick() {
        AuthHandler.tick();

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

        //pings counter
        if (lastPing > 0 && FiguraMod.ticks - lastPing >= 20)
            lastPing = pingsSent = pingsReceived = 0;

        //process requests
        if (isConnected()) {
            if (!API_REQUESTS.isEmpty()) {
                HTTPRequest request;
                while ((request = API_REQUESTS.poll()) != null) {
                    HTTPRequest finalRequest = request;
                    async(() -> finalRequest.consumer.accept(api));
                }
            }

            if (!WS_REQUESTS.isEmpty()) {
                WSRequest request;
                while ((request = WS_REQUESTS.poll()) != null) {
                    WSRequest finalRequest = request;
                    async(() -> finalRequest.consumer.accept(ws));
                }
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
        FiguraMod.LOGGER.warn("Failed to auth with the " + FiguraMod.MOD_NAME + " auth server! {}", reason);
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
        API_REQUESTS.add(new HTTPRequest(owner, api -> api.runString(request.apply(api), consumer)));
    }

    private static void queueStream(UUID owner, Function<HttpAPI, HttpRequest> request, BiConsumer<Integer, InputStream> consumer) {
        API_REQUESTS.add(new HTTPRequest(owner, api -> api.runStream(request.apply(api), consumer)));
    }

    public static void clear(UUID requestOwner) {
        API_REQUESTS.removeIf(request -> request.owner.equals(requestOwner));
    }

    private static void responseDebug(String src, int code, String data) {
        if (debug) FiguraMod.debug("Got response of \"" + src + "\" with code " + code + ":\n\t" + data);
    }

    private static void connectAPI(String token) {
        api = new HttpAPI(client, token);
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

            api.runString(api.checkAuth(), (code, data) -> {
                if (code != 200)
                    reAuth();
            });
        });
    }

    public static void checkVersion() {
        queueString(Util.NIL_UUID, HttpAPI::getVersion, (code, data) -> {
            responseDebug("checkVersion", code, data);
            JsonObject json = JsonParser.parseString(data).getAsJsonObject();

            int config = Config.UPDATE_CHANNEL.asInt();
            if (config != 0) {
                String version = json.get(config == 1 ? "release" : "prerelease").getAsString();
                if (new Version(version).compareTo(Version.VERSION) > 0)
                    FiguraToast.sendToast(FiguraText.of("toast.new_version"), version);
            }
        });
    }

    public static void setLimits() {
        queueString(Util.NIL_UUID, HttpAPI::getLimits, (code, data) -> {
            responseDebug("setLimits", code, data);
            JsonObject json = JsonParser.parseString(data).getAsJsonObject();
            //TODO
        });
    }

    public static void getUser(UUID id) {
        queueString(id, api -> api.getUser(id), (code, data) -> {
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
            queueString(Util.NIL_UUID, api -> api.uploadAvatar(id, baos.toByteArray()), (code, data) -> {
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
        queueString(Util.NIL_UUID, api -> api.deleteAvatar(id), (code, data) -> {
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

        queueString(Util.NIL_UUID, api -> api.setEquipped(GSON.toJson(json)), (code, data) -> {
            responseDebug("equipAvatar", code, data);
            if (code != 200 && Config.CONNECTION_TOASTS.asBool())
                FiguraToast.sendToast(FiguraText.of("backend.equip_error"), FiguraToast.ToastType.ERROR);
        });
    }

    public static void getAvatar(UUID target, UUID owner, String id) {
        queueStream(Util.NIL_UUID, api -> api.getAvatar(owner, id), (code, stream) -> {
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
        return ws != null && ws.isOpen();
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

    public static void subscribe(UUID id) {
        WS_REQUESTS.add(new WSRequest(Util.NIL_UUID, client -> {
            try {
                ByteBuffer buffer = C2SMessageHandler.sub(id);
                client.send(buffer);
                if (debug) FiguraMod.debug("Subbed to " + id);
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to subscribe to " + id.toString(), e);
            }
        }));
    }

    public static void unsubscribe(UUID id) {
        WS_REQUESTS.add(new WSRequest(Util.NIL_UUID, client -> {
            try {
                ByteBuffer buffer = C2SMessageHandler.unsub(id);
                client.send(buffer);
                if (debug) FiguraMod.debug("Unsubbed to " + id);
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to unsubscribe to " + id.toString(), e);
            }
        }));
    }


    // -- global functions -- //


    public static boolean isConnected() {
        return api != null && checkWS();
    }

    public static boolean canUpload() {
        return isConnected(); //TODO - limits
    }

    public static int getSizeLimit() {
        return Integer.MAX_VALUE; //TODO - limits
    }


    // -- request subclass -- //


    private abstract static class Request {

        public final UUID owner;

        public Request(UUID owner) {
            this.owner = owner;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o instanceof Request request && owner.equals(request.owner);
        }
    }

    private static class HTTPRequest extends Request {

        Consumer<HttpAPI> consumer;

        public HTTPRequest(UUID owner,  Consumer<HttpAPI> consumer) {
            super(owner);
            this.consumer = consumer;
        }
    }

    private static class WSRequest extends Request {

        Consumer<WebsocketThingy> consumer;

        public WSRequest(UUID owner,  Consumer<WebsocketThingy> consumer) {
            super(owner);
            this.consumer = consumer;
        }
    }
}
