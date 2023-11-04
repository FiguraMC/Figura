package org.figuramc.figura.lua.api.net;

import net.minecraft.network.chat.Component;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.data.FiguraFuture;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.permissions.Permissions;
import org.luaj.vm2.LuaError;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@LuaWhitelist
@LuaTypeDoc(value = "socket_api", name = "SocketAPI")
public class SocketAPI {
    private final NetworkingAPI parent;

    public SocketAPI(NetworkingAPI parent) {
        this.parent = parent;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "socket_api.open",
            overloads = @LuaMethodOverload(
                    argumentNames = {"host", "port"},
                    argumentTypes = {String.class, Integer.class},
                    returnType = FiguraFuture.class
            )
    )
    public FiguraFuture<FiguraSocket> open(@LuaNotNil String host, @LuaNotNil Integer port) {
        try {
            parent.securityCheck(host);
        } catch (NetworkingAPI.LinkNotAllowedException e) {
            parent.error(NetworkingAPI.LogSource.SOCKET, Component.literal("Tried to establish connection to not allowed host %s".formatted(host)));
            throw e.luaError;
        }
        int maxSockets = parent.owner.permissions.get(Permissions.MAX_SOCKETS);
        if (parent.owner.openSockets.size() > maxSockets)
            throw new LuaError("You can't open more than %s sockets".formatted(maxSockets));
        FiguraFuture<FiguraSocket> future = new FiguraFuture<>();
        CompletableFuture.supplyAsync(() -> {
            try {
                FiguraSocket socket = new FiguraSocket(host, port, parent.owner);
                parent.owner.openSockets.add(socket);
                parent.log(NetworkingAPI.LogSource.SOCKET, Component.literal("Established connection to host %s".formatted(host)));
                return socket;
            } catch (IOException e) {
                throw new LuaError(e);
            }
        }).whenCompleteAsync(future::handle);
        return future;
    }

    @Override
    public String toString() {
        return "SocketAPI";
    }
}
