package org.moon.figura.avatars;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.providers.LocalAvatarLoader;
import org.moon.figura.backend.NetworkManager;
import org.moon.figura.config.Config;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.utils.FiguraText;

import java.nio.file.Path;
import java.util.*;

/**
 * Manages all the avatars that are currently loaded in memory, and also
 * handles getting the avatars of entities. If an entity does not have a loaded avatar,
 * the AvatarManager will fetch the avatar and cache it.
 */
public class AvatarManager {

    private static final HashMap<UUID, Avatar> LOADED_AVATARS = new HashMap<>();
    private static final Set<UUID> FETCHED_AVATARS = new HashSet<>();

    public static boolean localUploaded = true; //init as true :3
    public static boolean panic = false;

    // -- avatar events -- //

    public static void tickLoadedAvatars() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (panic || connection == null)
            return;

        //unload avatars from disconnected players
        Set<UUID> toRemove = new HashSet<>();
        for (UUID id : LOADED_AVATARS.keySet()) {
            if (connection.getPlayerInfo(id) == null)
                toRemove.add(id);
        }

        for (UUID id : toRemove)
            clearAvatar(id);

        //actual tick event

        for (Avatar avatar : LOADED_AVATARS.values())
            avatar.onTick();
    }

    public static void onWorldRender(float tickDelta) {
        if (panic)
            return;

        for (Avatar avatar : LOADED_AVATARS.values())
            avatar.worldRenderEvent(tickDelta);
    }

    public static void afterWorldRender() {
        if (panic)
            return;

        for (Avatar avatar : LOADED_AVATARS.values())
            avatar.endWorldRenderEvent();
    }

    // -- avatar management -- //

    //player will also attempt to load from network, if possible
    public static Avatar getAvatarForPlayer(UUID player) {
        if (panic) return null;

        if (!LOADED_AVATARS.containsKey(player))
            fetchBackend(player);

        return LOADED_AVATARS.get(player);
    }

    //tries to get data from an entity
    public static Avatar getAvatar(Entity entity) {
        if (panic) return null;

        UUID uuid = entity.getUUID();

        //load from player (fetch backend) if is a player
        if (entity instanceof Player)
            return getAvatarForPlayer(uuid);

        //TODO
        //otherwise, returns the avatar from the entity pool (cem)
        return null;
    }

    //removes an loaded avatar
    public static void clearAvatar(UUID id) {
        if (LOADED_AVATARS.containsKey(id))
            LOADED_AVATARS.remove(id).clean();
        FETCHED_AVATARS.remove(id);
    }

    //clears ALL loaded avatars, including local
    public static void clearAllAvatars() {
        for (Avatar avatar : LOADED_AVATARS.values())
            avatar.clean();

        LOADED_AVATARS.clear();
        FETCHED_AVATARS.clear();

        localUploaded = true;
        FiguraMod.LOGGER.info("Cleared all avatars");
    }

    //reloads an avatar
    public static void reloadAvatar(UUID id) {
        //first clear the avatar
        clearAvatar(id);

        //only non uploaded local needs to be manually reloaded
        //other ones will be fetched from backend on further request
        if (!localUploaded && id.compareTo(FiguraMod.getLocalPlayerUUID()) == 0)
            loadLocalAvatar(LocalAvatarLoader.getLastLoadedPath());

        //send client feedback
        FiguraToast.sendToast(FiguraText.of("toast.reload"));
    }

    //load the local player avatar
    //returns true if an avatar was actually loaded
    public static boolean loadLocalAvatar(Path path) {
        //clear
        UUID id = FiguraMod.getLocalPlayerUUID();
        clearAvatar(id);

        //mark as not uploaded
        localUploaded = false;

        //load
        try {
            CompoundTag nbt = LocalAvatarLoader.loadAvatar(path);
            if (nbt != null) {
                LOADED_AVATARS.put(id, new Avatar(nbt, id));
                return true;
            }
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to load avatar from " + path, e);
            FiguraToast.sendToast(FiguraText.of("toast.load_error"), FiguraToast.ToastType.ERROR);
        }
        return false;
    }

    //set an user's avatar
    public static void setAvatar(UUID id, CompoundTag nbt) {
        //if local, remove local watch keys and mark as uploaded
        if (id.compareTo(FiguraMod.getLocalPlayerUUID()) == 0) {
            LocalAvatarLoader.resetWatchKeys();
            localUploaded = true;
        }

        LOADED_AVATARS.put(id, new Avatar(nbt, id));
    }

    //get avatar from the backend
    //mark as uploaded if local
    private static void fetchBackend(UUID id) {
        //already fetched :p
        if (id == null || FETCHED_AVATARS.contains(id))
            return;

        //egg
        if (FiguraMod.CHEESE_DAY && (boolean) Config.EASTER_EGGS.value && LocalAvatarLoader.cheese != null) {
            setAvatar(id, LocalAvatarLoader.cheese);
            return;
        }

        if (NetworkManager.getAvatar(id))
            FETCHED_AVATARS.add(id);
    }
}
