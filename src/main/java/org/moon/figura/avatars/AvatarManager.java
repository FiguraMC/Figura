package org.moon.figura.avatars;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

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
        //wip
        return LOADED_AVATARS.computeIfAbsent(player, uuid -> new Avatar("name", "author", "version"));
    }

    //tries to get data from an entity
    public static Avatar getAvatarFor(Entity entity) {
        //load from player (fetch backend) if is a player
        if (entity instanceof PlayerEntity)
            return getAvatarForPlayer(entity.getUuid());

        //otherwise, just normally load it
        return LOADED_AVATARS.computeIfAbsent(entity.getUuid(), uuid -> new Avatar("name", "author", "version"));
    }
}
