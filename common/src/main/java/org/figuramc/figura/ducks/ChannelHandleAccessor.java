package org.figuramc.figura.ducks;

import java.util.UUID;

public interface ChannelHandleAccessor {

    UUID getOwner();
    void setOwner(UUID uuid);

    String getName();
    void setName(String name);
}
