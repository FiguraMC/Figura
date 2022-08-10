package org.moon.figura.avatars;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.providers.LocalAvatarLoader;
import org.moon.figura.backend.NetworkManager;
import org.moon.figura.config.Config;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.widgets.lists.AvatarList;
import org.moon.figura.utils.FiguraText;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Manages all the avatars that are currently loaded in memory, and also
 * handles getting the avatars of entities. If an entity does not have a loaded avatar,
 * the AvatarManager will fetch the avatar and cache it.
 */
public class AvatarManager {

    private static final Map<UUID, Avatar> LOADED_AVATARS = new ConcurrentHashMap<>();
    private static final Set<UUID> FETCHED_AVATARS = new HashSet<>();

    private static final Queue<AvatarIOEvent> EVENT_QUEUE = new ConcurrentLinkedQueue<>();

    public static boolean localUploaded = true; //init as true :3
    public static boolean panic = false;

    // -- avatar events -- //

    public static void tickLoadedAvatars() {
        if (panic)
            return;

        //unload avatars from disconnected players
        //needs to actually be an event, otherwise some things like skulls will try to download the avatar every tick
        /*
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        Set<UUID> toRemove = new HashSet<>();
        for (UUID id : LOADED_AVATARS.keySet()) {
            if (connection != null && connection.getPlayerInfo(id) == null)
                toRemove.add(id);
        }

        for (UUID id : toRemove)
            clearAvatar(id);
        */

        //tick the avatar
        for (Avatar avatar : LOADED_AVATARS.values())
            avatar.tick();
    }

    public static void onWorldRender(float tickDelta) {
        while (!EVENT_QUEUE.isEmpty()) {
            AvatarIOEvent event = EVENT_QUEUE.poll();
            event.type.consumer.accept(event);
        }

        if (panic)
            return;

        for (Avatar avatar : LOADED_AVATARS.values())
            avatar.worldRenderEvent(tickDelta);
    }

    public static void afterWorldRender(float tickDelta) {
        if (panic)
            return;

        for (Avatar avatar : LOADED_AVATARS.values())
            avatar.postWorldRenderEvent(tickDelta);
    }

    public static void applyAnimations() {
        if (panic)
            return;

        for (Avatar avatar : LOADED_AVATARS.values())
            avatar.applyAnimations();
    }

    public static void clearAnimations() {
        if (panic)
            return;

        for (Avatar avatar : LOADED_AVATARS.values())
            avatar.clearAnimations();
    }

    // -- avatar getters -- //

    //player will also attempt to load from network, if possible
    public static Avatar getAvatarForPlayer(UUID player) {
        if (panic || Minecraft.getInstance().level == null)
            return null;

        if (!LOADED_AVATARS.containsKey(player))
            fetchBackend(player);

        return LOADED_AVATARS.get(player);
    }

    //tries to get data from an entity
    public static Avatar getAvatar(Entity entity) {
        if (panic || Minecraft.getInstance().level == null)
            return null;

        UUID uuid = entity.getUUID();

        //load from player (fetch backend) if is a player
        if (entity instanceof Player)
            return getAvatarForPlayer(uuid);

        //TODO
        //otherwise, returns the avatar from the entity pool (cem)
        return null;
    }

    // -- avatar management -- //

    //removes an loaded avatar
    public static void clearAvatar(UUID id) {
        EVENT_QUEUE.add(new AvatarIOEvent(id, AvatarIOEvent.EventType.CLEAR));
        NetworkManager.clearRequestsFor(id);
        NetworkManager.unsubscribe(id);
    }

    //clears ALL loaded avatars, including local
    public static void clearAllAvatars() {
        for (UUID id : LOADED_AVATARS.keySet())
            EVENT_QUEUE.add(new AvatarIOEvent(id, AvatarIOEvent.EventType.CLEAR));

        localUploaded = true;
        NetworkManager.clearRequests();
        FiguraMod.LOGGER.info("Cleared all avatars");
    }

    //reloads an avatar
    public static void reloadAvatar(UUID id) {
        EVENT_QUEUE.add(new AvatarIOEvent(id, AvatarIOEvent.EventType.RELOAD));
    }

    //load the local player avatar
    public static void loadLocalAvatar(Path path) {
        UUID id = FiguraMod.getLocalPlayerUUID();
        EVENT_QUEUE.add(new AvatarIOEvent(id, path));

        NetworkManager.clearRequestsFor(id);
        NetworkManager.unsubscribe(id);

        //mark as not uploaded
        localUploaded = false;
    }

    //set an user's avatar
    public static void setAvatar(UUID id, CompoundTag nbt) {
        EVENT_QUEUE.add(new AvatarIOEvent(id, nbt));
    }

    //get avatar from the backend
    private static void fetchBackend(UUID id) {
        //already fetched :p
        if (FETCHED_AVATARS.contains(id))
            return;

        FETCHED_AVATARS.add(id);

        //egg
        if (FiguraMod.CHEESE_DAY && (boolean) Config.EASTER_EGGS.value && LocalAvatarLoader.cheese != null) {
            setAvatar(id, LocalAvatarLoader.cheese);
            return;
        }

        NetworkManager.getAvatar(id);
    }

    private static class AvatarIOEvent {

        public final UUID owner;
        public final EventType type;
        public final CompoundTag nbt;
        public final Path path;

        public AvatarIOEvent(UUID owner, EventType type) {
            this(owner, type, null, null);
        }

        public AvatarIOEvent(UUID owner, Path path) {
            this(owner, EventType.LOAD_LOCAL, null, path);
        }

        public AvatarIOEvent(UUID owner, CompoundTag nbt) {
            this(owner, EventType.SET, nbt, null);
        }

        private AvatarIOEvent(UUID owner, EventType type, CompoundTag nbt, Path path) {
            this.owner = owner;
            this.type = type;
            this.nbt = nbt;
            this.path = path;
        }

        public enum EventType {
            CLEAR(event -> {
                UUID id = event.owner;
                if (LOADED_AVATARS.containsKey(id))
                    LOADED_AVATARS.remove(id).clean();
                FETCHED_AVATARS.remove(id);
            }),
            SET(event -> {
                UUID id = event.owner;

                //remove local watch keys
                if (FiguraMod.isLocal(id)) {
                    LocalAvatarLoader.resetWatchKeys();
                    AvatarList.selectedEntry = null;
                    localUploaded = true;
                }

                try {
                    Avatar avatar = new Avatar(id);
                    LOADED_AVATARS.put(id, avatar);
                    avatar.load(event.nbt);
                } catch (Exception e) {
                    FiguraMod.LOGGER.error("Failed to set avatar for " + id, e);
                }
            }),
            LOAD_LOCAL(event -> {
                UUID id = event.owner;
                Path path = event.path;

                //clear
                CLEAR.consumer.accept(event);

                //load
                try {
                    Avatar avatar = new Avatar(id);
                    LOADED_AVATARS.put(id, avatar);
                    avatar.load(LocalAvatarLoader.loadAvatar(path));
                } catch (Exception e) {
                    FiguraMod.LOGGER.error("Failed to load avatar from " + path, e);
                    FiguraToast.sendToast(FiguraText.of("toast.load_error"), FiguraText.of("toast.load_error_l2"), FiguraToast.ToastType.ERROR);
                }
            }),
            RELOAD(event -> {
                if (!localUploaded && FiguraMod.isLocal(event.owner))
                    LOAD_LOCAL.consumer.accept(new AvatarIOEvent(event.owner, LocalAvatarLoader.getLastLoadedPath()));
                else
                    CLEAR.consumer.accept(event);
            });

            public final Consumer<AvatarIOEvent> consumer;

            EventType(Consumer<AvatarIOEvent> consumer) {
                this.consumer = consumer;
            }
        }
    }
}
