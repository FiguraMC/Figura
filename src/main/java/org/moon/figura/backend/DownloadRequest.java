package org.moon.figura.backend;

import com.google.gson.JsonObject;

import java.util.UUID;

public abstract class DownloadRequest {

    @Override
    public abstract boolean equals(Object o);
    public abstract Runnable function();

    public static class AvatarRequest extends DownloadRequest {

        private final UUID owner;
        private final String id;

        public AvatarRequest(UUID owner, String id) {
            this.owner = owner;
            this.id = id;
        }

        public Runnable function() {
            return () -> {
                if (!NetworkManager.hasBackend())
                    return;

                JsonObject json = new JsonObject();
                json.addProperty("type", "download");
                json.addProperty("owner", owner.toString());
                json.addProperty("id", id);

                NetworkManager.sendMessage(NetworkManager.GSON.toJson(json));
            };
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o instanceof AvatarRequest request && owner.equals(request.owner) && id.equals(request.id);
        }
    }

    public static class UserRequest extends DownloadRequest {

        private final UUID id;

        public UserRequest(UUID id) {
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
            return o instanceof UserRequest request && id.equals(request.id);
        }
    }
}
