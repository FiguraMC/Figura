package org.figuramc.figura.lua.api;

import net.minecraft.client.Minecraft;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.data.FiguraBuffer;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaFunction;

import java.util.HashMap;
import java.util.UUID;

@LuaWhitelist
@LuaTypeDoc(
        name = "ServerPacketsAPI",
        value = "server_packets"
)
public class ServerPacketsAPI {
    private final HashMap<String, LuaFunction> listeners = new HashMap<>();
    private final HashMap<Integer, String> idMap = new HashMap<>();
    private final boolean isHost;
    private final Avatar owner;

    public ServerPacketsAPI(Avatar owner) {
        isHost = owner.isHost;
        this.owner = owner;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {String.class, FiguraBuffer.class},
                    argumentNames = {"id", "data"}
            ),
            value = "server_packets.send_packet"
    )
    public void sendPacket(@LuaNotNil String id, FiguraBuffer data) {
        FiguraMod.stub();
    }

    @LuaWhitelist
    public Object __index(String id) {
        return listeners.get(id);
    }

    @LuaWhitelist
    public void __newindex(@LuaNotNil String id, LuaFunction listener) {
        if (!isHost) return;
        if (listener == null) {
            listeners.remove(id);
            idMap.remove(id.hashCode());
        }
        else {
            listeners.put(id, listener);
            idMap.put(id.hashCode(), id);
        }
    }

    private LuaFunction getListener(int id) {
        return listeners.get(idMap.get(id));
    }

    public static void handlePacket(int id, byte[] data) {
        UUID localPlayerUUID = Minecraft.getInstance().player.getUUID();
        Avatar avatar = AvatarManager.getLoadedAvatar(localPlayerUUID);
        if (avatar != null) {
            var runtime = avatar.luaRuntime;
            LuaFunction listener = runtime.serverPackets.getListener(id);
            if (listener != null) {
                FiguraBuffer buffer = new FiguraBuffer(avatar);
                buffer.writeBytes(data);
                buffer.setPosition(0);
                runtime.run(listener, avatar.tick, buffer);
            }
        }
    }
}
