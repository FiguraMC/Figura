package org.moon.figura.backend;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.avatars.Badges;
import org.moon.figura.config.Config;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.Version;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.BitSet;
import java.util.UUID;
import java.util.function.Consumer;

public enum MessageHandler {

    // -- auth server messages -- //

    AUTH(json -> {
        NetworkManager.authToken = NetworkManager.GSON.toJson(json);
        NetworkManager.closeBackend();
        NetworkManager.openBackend();
    }),

    // -- backend messages -- //

    SYSTEM(json -> {
        if (FiguraMod.DEBUG_MODE || (json.has("force") && json.get("force").getAsBoolean())) {
            String message = json.get("message").getAsString();
            FiguraMod.sendChatMessage(Component.empty().append(Component.literal("-- " + FiguraMod.MOD_NAME + " backend message --\n\n").withStyle(ColorUtils.Colors.SKYE_BLUE.style)).append(message));
        }
    }),
    CONNECTED(json -> {
        NetworkManager.backendStatus = 3;

        if (Config.CONNECTION_TOASTS.asBool())
            FiguraToast.sendToast(FiguraText.of("backend.connected"));

        JsonObject limits = json.get("limits").getAsJsonObject();
        WebsocketManager backend = NetworkManager.backend;

        backend.maxAvatarSize = limits.get("maxAvatarSize").getAsInt();
        backend.maxAvatars = limits.get("maxAvatars").getAsInt();

        backend.pingSize.set(limits.get("pingSize").getAsFloat());
        backend.pingRate.set(limits.get("pingRate").getAsFloat());

        backend.equip.set(limits.get("equip").getAsFloat());
        backend.upload.set(limits.get("upload").getAsFloat());
        backend.download.set(limits.get("download").getAsFloat());

        int config = Config.UPDATE_CHANNEL.asInt();
        if (config != 0) {
            try {
                String key = config == 1 ? "latestRelease" : "latestPreRelease";
                String version = json.get(key).getAsString();
                if (Version.of(version).compareTo(Version.VERSION) > 0)
                    FiguraToast.sendToast(FiguraText.of("toast.new_version"), version);
            } catch (Exception ignored) {}
        }
    }),
    KEEPALIVE(json -> NetworkManager.sendMessage(NetworkManager.GSON.toJson(json))),
    TOAST(json -> {
        FiguraToast.ToastType type;
        try {
            type = FiguraToast.ToastType.valueOf(json.get("toast").getAsString().toUpperCase());
        } catch (Exception ignored) {
            type = FiguraToast.ToastType.DEFAULT;
        }

        String message = json.has("top") ? json.get("top").getAsString() : "";
        String message2 = json.has("bottom") ? json.get("bottom").getAsString() : "";

        if (message.isBlank() && message2.isBlank())
            return;

        if (json.has("raw") && json.get("raw").getAsBoolean())
            FiguraToast.sendToast(TextUtils.tryParseJson(message), TextUtils.tryParseJson(message2), type);
        else
            FiguraToast.sendToast(message.isBlank() ? "" : FiguraText.of("backend." + message), message2.isBlank() ? "" : FiguraText.of("backend." + message2), type);
    }),
    AVATAR(json -> {
        json = json.get("avatar").getAsJsonObject();

        UUID owner = UUID.fromString(json.get("owner").getAsString());

        String avatar = json.get("data").getAsString();
        byte[] bytes = Base64.getDecoder().decode(avatar.getBytes());

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            AvatarManager.setAvatar(owner, NbtIo.readCompressed(bais));
            NetworkManager.subscribe(owner);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
    }),
    USERINFO(json -> {
        if (json.get("user").isJsonNull())
            return;

        json = json.getAsJsonObject("user");
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
    }),
    EVENT(json -> {
        UUID owner = UUID.fromString(json.get("uuid").getAsString());
        JsonObject event = json.getAsJsonObject("event");
        EventHandler.readEvent(owner, event);
    }),
    RESPONSE(json -> {
        try {
            Response response = Response.valueOf(json.get("data").getAsString().toUpperCase());
            FiguraToast.sendToast(response.TITLE, response.SUBTITLE, response.TYPE);
        } catch (Exception ignored) {}
    });

    // -- fields -- //

    private final Consumer<JsonObject> consumer;

    MessageHandler(Consumer<JsonObject> consumer) {
        this.consumer = consumer;
    }

    // -- methods -- //

    public static void handleMessage(String message) {
        JsonObject json;
        try {
             json = JsonParser.parseString(message).getAsJsonObject();
             if (!json.has("type"))
                 throw new Exception();
        } catch (Exception ignored) {
            FiguraMod.LOGGER.warn("Invalid backend message: " + message);
            return;
        }

        String type = json.get("type").getAsString().toUpperCase();
        try {
            MessageHandler handler = MessageHandler.valueOf(type);
            handler.consumer.accept(json);
        } catch (Exception e) {
            FiguraMod.LOGGER.warn("Invalid backend message type: " + type, e);
        }
    }
}
