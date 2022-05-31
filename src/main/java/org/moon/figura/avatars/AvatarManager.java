package org.moon.figura.avatars;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.providers.LocalAvatarLoader;
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
    private static final Set<UUID> PLAYER_AVATARS = new HashSet<>();
    private static final Set<UUID> FETCHED_AVATARS = new HashSet<>();

    public static boolean localUploaded = true; //init as true :3
    public static boolean panic = false;

    // -- avatar events -- //

    public static void tickLoadedAvatars() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (panic || connection == null)
            return;

        //remove disconnected player avatars
        PLAYER_AVATARS.removeIf(id -> {
            if (connection.getPlayerInfo(id) != null)
                return false;

            LOADED_AVATARS.remove(id).clean(); //if loaded avatar is null, something is very wrong
            FiguraMod.LOGGER.debug("Removed avatar for " + id);
            return true;
        });

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

        //otherwise, just normally load it
        return LOADED_AVATARS.get(uuid);
    }

    //clear entity data when unloaded
    public static void entityUnload(Entity entity) {
        //player avatars are kept until the player disconnects
        if (!(entity instanceof Player))
            clearAvatar(entity.getUUID());
    }

    //removes an loaded avatar
    public static void clearAvatar(UUID id) {
        if (LOADED_AVATARS.containsKey(id))
            LOADED_AVATARS.remove(id).clean();
        PLAYER_AVATARS.remove(id);
        FETCHED_AVATARS.remove(id);
    }

    //clears ALL loaded avatars, including local
    public static void clearAllAvatars() {
        for (Avatar avatar : LOADED_AVATARS.values())
            avatar.clean();

        LOADED_AVATARS.clear();
        PLAYER_AVATARS.clear();
        FETCHED_AVATARS.clear();
        localUploaded = true;
        FiguraMod.LOGGER.debug("Cleared all avatars");
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
                PLAYER_AVATARS.add(id);
                return true;
            }
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to load avatar from " + path, e);
            FiguraToast.sendToast(new FiguraText("toast.load_error"), FiguraToast.ToastType.ERROR);
        }
        return false;
    }

    //set an user's avatar
    public static void setAvatar(UUID id, CompoundTag nbt, boolean player) {
        if (id.compareTo(FiguraMod.getLocalPlayerUUID()) == 0)
            //force local loading to remove watch keys and flag as not uploaded
            loadLocalAvatar(null);

        LOADED_AVATARS.put(id, new Avatar(nbt, id));
        if (player) PLAYER_AVATARS.add(id);
    }

    //get avatar from the backend
    //mark as uploaded if local
    private static void fetchBackend(UUID id) {
        //already fetched :p
        if (id == null || FETCHED_AVATARS.contains(id))
            return;

        if (id.compareTo(FiguraMod.getLocalPlayerUUID()) == 0)
            //force local loading to remove watch keys and flag as not uploaded
            loadLocalAvatar(null);

        //egg
        if (FiguraMod.CHEESE_DAY && (boolean) Config.EASTER_EGGS.value) {
            LOADED_AVATARS.put(id, new Avatar(LocalAvatarLoader.CHEESE, id));
            PLAYER_AVATARS.add(id);
            return;
        }

        //TODO - then we really fetch the backend
    }
}
