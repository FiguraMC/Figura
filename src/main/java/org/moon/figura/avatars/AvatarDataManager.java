package org.moon.figura.avatars;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.UUID;

public class AvatarDataManager {

    private static final HashMap<UUID, AvatarData> LOADED_AVATAR_DATA = new HashMap<>();

    //player will also attempt to load from network, if possible
    public static AvatarData loadDataForPlayer(UUID player) {
        //wip
        return LOADED_AVATAR_DATA.computeIfAbsent(player, uuid -> new AvatarData());
    }

    //tries to get data from an entity
    public static AvatarData loadData(Entity entity) {
        //load from player (fetch backend) if is a player
        if (entity instanceof PlayerEntity)
            return loadDataForPlayer(entity.getUuid());

        //otherwise, just normally load it
        return LOADED_AVATAR_DATA.computeIfAbsent(entity.getUuid(), uuid -> new AvatarData());
    }
}
