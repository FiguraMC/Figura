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
import org.moon.figura.gui.widgets.lists.AvatarList;
import org.moon.figura.utils.FiguraText;

import java.nio.file.Path;
import java.util.*;

/**
 * Manages all the avatars that are currently loaded in memory, and also
 * handles getting the avatars of entities. If an entity does not have a loaded avatar,
 * the AvatarManager will fetch the avatar and cache it.
 */
public class AvatarManager {

    private static final Map<UUID, Avatar> LOADED_AVATARS = Collections.synchronizedMap(new HashMap<>());
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

        //tick the avatar
        for (Avatar avatar : LOADED_AVATARS.values())
            avatar.tick();
    }

    public static void onWorldRender(float tickDelta) {
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

    // -- avatar management -- //

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

    //removes an loaded avatar
    public static void clearAvatar(UUID id) {
        if (LOADED_AVATARS.containsKey(id))
            LOADED_AVATARS.remove(id).clean();
        FETCHED_AVATARS.remove(id);

        NetworkManager.clearRequestsFor(id);
    }

    //clears ALL loaded avatars, including local
    public static void clearAllAvatars() {
        for (Avatar avatar : LOADED_AVATARS.values())
            avatar.clean();

        LOADED_AVATARS.clear();
        FETCHED_AVATARS.clear();

        localUploaded = true;
        NetworkManager.clearRequests();
        FiguraMod.LOGGER.info("Cleared all avatars");
    }

    //reloads an avatar
    public static void reloadAvatar(UUID id) {
        //first clear the avatar
        clearAvatar(id);

        //only non uploaded local needs to be manually reloaded
        //other ones will be fetched from backend on further request
        if (!localUploaded && FiguraMod.isLocal(id))
            loadLocalAvatar(LocalAvatarLoader.getLastLoadedPath());

        //send client feedback
        FiguraToast.sendToast(FiguraText.of("toast.reload"));
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
            Avatar avatar = new Avatar(id);
            LOADED_AVATARS.put(id, avatar);
            avatar.load(LocalAvatarLoader.loadAvatar(path));
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to load avatar from " + path, e);
            FiguraToast.sendToast(FiguraText.of("toast.load_error"), FiguraToast.ToastType.ERROR);
        }
    }

    //set an user's avatar
    public static void setAvatar(UUID id, CompoundTag nbt) {
        //remove local watch keys
        if (FiguraMod.isLocal(id)) {
            LocalAvatarLoader.resetWatchKeys();
            AvatarList.selectedEntry = null;
            localUploaded = true;
        }

        try {
            Avatar avatar = LOADED_AVATARS.get(id);
            if (avatar != null) avatar.load(nbt);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to set avatar for " + id, e);
        }
    }

    public static void setBadge(UUID id, int index, boolean value) {
        Avatar avatar = LOADED_AVATARS.get(id);
        if (avatar != null) avatar.badges.set(index, value);
    }

    //get avatar from the backend
    //mark as uploaded if local
    private static void fetchBackend(UUID id) {
        //already fetched :p
        if (FETCHED_AVATARS.contains(id))
            return;

        FETCHED_AVATARS.add(id);
        LOADED_AVATARS.put(id, new Avatar(id));

        //egg
        if (FiguraMod.CHEESE_DAY && (boolean) Config.EASTER_EGGS.value && LocalAvatarLoader.cheese != null) {
            setAvatar(id, LocalAvatarLoader.cheese);
            return;
        }

        NetworkManager.getAvatar(id);
    }
}
