package org.moon.figura.backend;

import com.google.gson.JsonObject;

import java.util.UUID;

public class DownloadRequest {

    public final UUID id;

    public DownloadRequest(UUID id) {
        this.id = id;
    }

    public Runnable function() {
        return () -> {
            if (!NetworkManager.hasBackend())
                return;

            JsonObject json = new JsonObject();
            json.addProperty("type", "getUser");
            json.addProperty("uuid", id.toString());

            NetworkManager.sendMessage(NetworkManager.GSON.toJson(json));
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof DownloadRequest request && id.equals(request.id);
    }

    // -- sub classes -- //

    public static class AvatarRequest extends DownloadRequest {

        public final String avatarID;

        public AvatarRequest(UUID id, String avatarID) {
            super(id);
            this.avatarID = avatarID;
        }

        @Override
        public Runnable function() {
            return () -> {
                if (!NetworkManager.hasBackend())
                    return;

                JsonObject json = new JsonObject();
                json.addProperty("type", "download");
                json.addProperty("owner", id.toString());
                json.addProperty("id", avatarID);

                NetworkManager.sendMessage(NetworkManager.GSON.toJson(json));
            };
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o instanceof AvatarRequest request && id.equals(request.id) && avatarID.equals(request.avatarID);
        }
    }
}
