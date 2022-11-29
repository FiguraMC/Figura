package org.moon.figura.avatar;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.local.LocalAvatarLoader;
import org.moon.figura.backend2.NetworkStuff;
import org.moon.figura.gui.widgets.lists.AvatarList;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all the avatars that are currently loaded in memory, and also
 * handles getting the avatars of entities. If an entity does not have a loaded avatar,
 * the AvatarManager will fetch the avatar and cache it.
 */
public class AvatarManager {

    private static final Map<UUID, UserData> LOADED_USERS = new ConcurrentHashMap<>();
    private static final Set<UUID> FETCHED_USERS = new HashSet<>();

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
        for (UserData user : LOADED_USERS.values()) {
            for (Avatar avatar : user.getAvatars())
                avatar.tick();
        }
    }

    public static void onWorldRender(float tickDelta) {
        if (panic)
            return;

        for (UserData user : LOADED_USERS.values()) {
            for (Avatar avatar : user.getAvatars())
                avatar.render(tickDelta);
        }
    }

    public static void afterWorldRender(float tickDelta) {
        if (panic)
            return;

        for (UserData user : LOADED_USERS.values()) {
            for (Avatar avatar : user.getAvatars())
                avatar.postWorldRenderEvent(tickDelta);
        }
    }

    public static void applyAnimations() {
        if (panic)
            return;

        for (UserData user : LOADED_USERS.values()) {
            for (Avatar avatar : user.getAvatars())
                avatar.applyAnimations();
        }
    }

    public static void clearAnimations() {
        if (panic)
            return;

        for (UserData user : LOADED_USERS.values()) {
            for (Avatar avatar : user.getAvatars())
                avatar.clearAnimations();
        }
    }

    // -- avatar getters -- //

    //player will also attempt to load from network, if possible
    public static Avatar getAvatarForPlayer(UUID player) {
        if (panic || Minecraft.getInstance().level == null)
            return null;

        fetchBackend(player);

        UserData user = LOADED_USERS.get(player);
        return user == null ? null : user.getMainAvatar();
    }

    //tries to get data from an entity
    public static Avatar getAvatar(Entity entity) {
        if (panic || Minecraft.getInstance().level == null)
            return null;

        UUID uuid = entity.getUUID();

        //load from player (fetch backend) if is a player
        if (entity instanceof Player)
            return getAvatarForPlayer(uuid);

        //TODO - CEM
        //otherwise, returns the avatar from the entity pool (cem)
        return null;
    }

    //get a loaded avatar without fetching backend or creating a new one
    public static Avatar getLoadedAvatar(UUID owner) {
        if (panic || Minecraft.getInstance().level == null)
            return null;

        UserData user = LOADED_USERS.get(owner);
        return user == null ? null : user.getMainAvatar();
    }

    //get all main loaded avatars
    public static List<Avatar> getLoadedAvatars() {
        List<Avatar> list = new ArrayList<>();
        for (UserData user : LOADED_USERS.values()) {
            Avatar avatar = user.getMainAvatar();
            if (avatar != null && avatar.nbt != null)
                list.add(avatar);
        }
        return list;
    }

    // -- avatar management -- //

    //removes an loaded avatar
    public static void clearAvatars(UUID id) {
        FETCHED_USERS.remove(id);

        UserData user = LOADED_USERS.get(id);
        if (user != null) user.clear();

        NetworkStuff.clear(id);
        NetworkStuff.unsubscribe(id);
        FiguraMod.debug("Cleared avatars of " + id);
    }

    //clears ALL loaded avatars, including local
    public static void clearAllAvatars() {
        for (UUID id : LOADED_USERS.keySet())
            clearAvatars(id);

        localUploaded = true;
        AvatarList.selectedEntry = null;
        FiguraMod.LOGGER.info("Cleared all avatars");
    }

    //reloads an avatar
    public static void reloadAvatar(UUID id) {
        if (!localUploaded && FiguraMod.isLocal(id))
            loadLocalAvatar(LocalAvatarLoader.getLastLoadedPath());
        else
            clearAvatars(id);
    }

    //load the local player avatar
    public static void loadLocalAvatar(Path path) {
        UUID id = FiguraMod.getLocalPlayerUUID();

        //clear
        clearAvatars(id);
        FETCHED_USERS.add(id);

        //load
        UserData user = LOADED_USERS.computeIfAbsent(id, UserData::new);
        LocalAvatarLoader.loadAvatar(path, user);

        //mark as not uploaded
        localUploaded = false;
    }

    //set an user's avatar
    public static void setAvatar(UUID id, CompoundTag nbt) {
        try {
            UserData user = LOADED_USERS.computeIfAbsent(id, UserData::new);
            user.clear();
            user.loadAvatar(nbt);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to set avatar for " + id, e);
        }
    }

    //get avatar from the backend
    private static void fetchBackend(UUID id) {
        if (FETCHED_USERS.contains(id))
            return;

        FETCHED_USERS.add(id);

        UserData user = LOADED_USERS.computeIfAbsent(id, UserData::new);

        FiguraMod.debug("Getting userdata for " + id);
        NetworkStuff.getUser(user);
    }

    // -- badges -- //

    public static Pair<BitSet, BitSet> getBadges(UUID id) {
        UserData user = LOADED_USERS.get(id);
        return user == null ? null : user.getBadges();
    }
}
