package org.moon.figura.avatars;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.providers.LocalAvatarLoader;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.utils.FiguraText;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

/**
 * Manages all the avatars that are currently loaded in memory, and also
 * handles getting the avatars of entities. If an entity does not have a loaded avatar,
 * the AvatarManager will fetch the avatar and cache it.
 */
public class AvatarManager {

    private static final HashMap<UUID, Avatar> LOADED_AVATARS = new HashMap<>();
    public static boolean localUploaded = true; //init as true :3
    public static boolean panic = false;

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

        //otherwise, just normally load it
        return LOADED_AVATARS.get(uuid);
    }

    //removes an loaded avatar
    public static void clearAvatar(UUID id) {
        if (LOADED_AVATARS.containsKey(id)) {
            LOADED_AVATARS.get(id).clean();
            LOADED_AVATARS.remove(id);
        }
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
        FiguraToast.sendToast(new FiguraText("toast.reload"));
    }

    public static void tickLoadedAvatars() {
        if (panic)
            return;

        for (Avatar avatar : LOADED_AVATARS.values())
            avatar.onTick();
    }

    //load the local player avatar
    public static void loadLocalAvatar(Path path) {
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
            }
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to load avatar from " + path, e);
        }
    }

    //get avatar from the backend
    //mark as uploaded if local
    private static void fetchBackend(UUID id) {
        //TODO
    }
}
