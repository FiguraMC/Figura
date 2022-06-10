package org.moon.figura.backend;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.TextUtils;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Consumer;

public enum MessageHandler {

    // -- auth server messages -- //

    AUTH(json -> {
        NetworkManager.authToken = NetworkManager.GSON.toJson(json);
        NetworkManager.closeBackend();
        NetworkManager.openBackend();
    }),
    BANNED(json -> {
        NetworkManager.banned = true;
        FiguraToast.sendToast(FiguraText.of("backend.banned"), FiguraToast.ToastType.ERROR);
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
        FiguraToast.sendToast(FiguraText.of("backend.connected"));

        JsonObject limits = json.get("limits").getAsJsonObject();
        WebsocketManager backend = NetworkManager.backend;

        backend.avatarSize = limits.get("avatarSize").getAsInt();
        backend.maxAvatars = limits.get("maxAvatars").getAsInt();

        backend.equip = limits.get("equip").getAsFloat();
        backend.upload = limits.get("upload").getAsFloat();
        backend.download = limits.get("download").getAsFloat();
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
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
    });

    // -- fields -- //

    private final Consumer<JsonObject> consumer;

    MessageHandler(Consumer<JsonObject> consumer) {
        this.consumer = consumer;
    }

    // -- methods -- //

    public static void handleMessage(String message) {
        JsonObject json = JsonParser.parseString(message).getAsJsonObject();

        if (!json.has("type")) {
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
