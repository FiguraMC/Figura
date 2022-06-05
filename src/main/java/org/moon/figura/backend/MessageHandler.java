package org.moon.figura.backend;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.TextUtils;

import java.util.function.Consumer;

public enum MessageHandler {

    // -- auth server messages -- //

    AUTH(json -> {
        String token = NetworkManager.GSON.toJson(json);
        NetworkManager.backend = new WebsocketManager(token);
        NetworkManager.backend.connect();
    }),
    BANNED(json -> FiguraToast.sendToast(new FiguraText("backend.banned"), FiguraToast.ToastType.ERROR)),

    // -- backend messages -- //

    SYSTEM(json -> {
        if (FiguraMod.DEBUG_MODE) {
            String message = NetworkManager.GSON.toJson(json);
            FiguraMod.sendChatMessage(TextComponent.EMPTY.copy().append(new TextComponent("-- " + FiguraMod.MOD_NAME + " backend message --\n\n").withStyle(ColorUtils.Colors.SKYE_BLUE.style)).append(message));
        }
    }),
    CONNECTED(json -> FiguraToast.sendToast(new FiguraText("backend.connected"))),
    KEEPALIVE(json -> {
        WebsocketManager backend = NetworkManager.backend;
        if (backend != null && backend.isOpen())
            backend.send(NetworkManager.GSON.toJson(json));
    }),
    TOAST(json -> {
        FiguraToast.ToastType type;
        try {
            type = FiguraToast.ToastType.valueOf(json.get("toast").getAsString().toUpperCase());
        } catch (Exception ignored) {
            type = FiguraToast.ToastType.DEFAULT;
        }

        String message = json.has("top") ? json.get("top").getAsString() : "";
        String message2 = json.has("bottom") ? json.get("bottom").getAsString() : "";
        FiguraToast.sendToast(TextUtils.tryParseJson(message), TextUtils.tryParseJson(message2), type);
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
        } catch (Exception ignored) {
            FiguraMod.LOGGER.warn("Invalid backend message type: " + type);
        }
    }
}
