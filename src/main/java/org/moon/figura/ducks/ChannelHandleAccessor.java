package org.moon.figura.ducks;

import com.mojang.blaze3d.audio.Channel;

import java.util.UUID;

public interface ChannelHandleAccessor {

    Channel getChannel();

    UUID getOwner();
    void setOwner(UUID uuid);

    String getName();
    void setName(String name);
}
