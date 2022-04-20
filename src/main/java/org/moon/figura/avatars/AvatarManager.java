package org.moon.figura.avatars;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.UUID;

/**
 * Manages all the avatars that are currently loaded in memory, and also
 * handles getting the avatars of entities. If an entity does not have a loaded avatar,
 * the AvatarManager will fetch the avatar and cache it.
 */
public class AvatarManager {

    private static final HashMap<UUID, Avatar> LOADED_AVATARS = new HashMap<>();

    //player will also attempt to load from network, if possible
    public static Avatar getAvatarForPlayer(UUID player) {
        return LOADED_AVATARS.get(player);
    }

    //tries to get data from an entity
    public static Avatar getAvatar(Entity entity) {
        UUID uuid = entity.getUUID();

        //load from player (fetch backend) if is a player
        if (entity instanceof Player)
            return getAvatarForPlayer(uuid);

        //otherwise, just normally load it
        return LOADED_AVATARS.get(uuid);
    }
}
