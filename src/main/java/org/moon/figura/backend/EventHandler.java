package org.moon.figura.backend;

import com.google.gson.JsonObject;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;

import java.util.Base64;
import java.util.UUID;
import java.util.function.BiConsumer;

public enum EventHandler {

    UPLOAD((owner, json) -> {
        if (FiguraMod.isLocal(owner))
            AvatarManager.localUploaded = true;
        else
            AvatarManager.reloadAvatar(owner);

        //re-sub
        NetworkManager.subscribe(owner);
    }),
    EQUIP((owner, json) -> {

    }),
    PING((owner, json) -> {
        Avatar avatar = AvatarManager.getLoadedAvatar(owner);
        if (avatar == null)
            return;

        int id = json.get("name").getAsInt();
        String data = json.get("data").getAsString();

        avatar.runPing(id, Base64.getDecoder().decode(data.getBytes()));
        NetworkManager.pingsReceived++;
        if (NetworkManager.lastPing == 0) NetworkManager.lastPing = FiguraMod.ticks;
    }),
    DELETE((owner, json) -> {
        if (!FiguraMod.isLocal(owner) || AvatarManager.localUploaded)
            AvatarManager.clearAvatar(owner);
    });

    private final BiConsumer<UUID, JsonObject> consumer;

    EventHandler(BiConsumer<UUID, JsonObject> consumer) {
        this.consumer = consumer;
    }

    public static void readEvent(UUID owner, JsonObject event) {
        try {
            if (!event.has("type"))
                throw new Exception();
        } catch (Exception ignored) {
            FiguraMod.LOGGER.warn("Invalid backend event: " + NetworkManager.GSON.toJson(event));
            return;
        }

        String type = event.get("type").getAsString().toUpperCase();
        try {
            EventHandler handler = EventHandler.valueOf(type);
            handler.consumer.accept(owner, event);
        } catch (Exception e) {
            FiguraMod.LOGGER.warn("Invalid backend event type: " + type, e);
        }
    }
}
