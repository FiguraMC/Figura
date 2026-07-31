package org.figuramc.figura.fabric;

import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.server.FiguraModServer;
import org.figuramc.figura.server.FiguraPermissionNodes;
import org.figuramc.figura.server.packets.Packet;

import java.util.Optional;
import java.util.UUID;

public class FiguraServerFabric extends FiguraModServer {
    @Override
    protected void sendPacketInternal(UUID receiver, Packet packet) {
        FiguraMod.LOGGER.warn("Tried to sendPacketInternal");
    }

    @Override
    public boolean getPermission(UUID uuid, FiguraPermissionNodes permission) {
        FiguraMod.LOGGER.warn("Tried to getPermission");
        return false;
    }

    @Override
    public Optional<String> getOption(UUID uuid, FiguraPermissionNodes option) {
        FiguraMod.LOGGER.warn("Tried to getOption");
        return Optional.empty();
    }
}
