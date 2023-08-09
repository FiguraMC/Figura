package org.figuramc.figura.lua.api.entity;

import net.minecraft.world.entity.player.Player;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.HostAPI;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@LuaWhitelist
@LuaTypeDoc(
        name = "ViewerAPI",
        value = "viewer"
)
public class ViewerAPI extends PlayerAPI {

    public ViewerAPI(Player entity) {
        super(entity);
    }

    private HostAPI host() {
        Avatar avatar = AvatarManager.getAvatarForPlayer(entityUUID);
        if (avatar == null || avatar.luaRuntime == null)
            return null;
        return avatar.luaRuntime.host;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_attack_charge")
    public float getAttackCharge() {
        HostAPI host = host();
        return host != null ? host.getAttackCharge() : 0f;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.is_jumping")
    public boolean isJumping() {
        HostAPI host = host();
        return host != null && host.isJumping();
    }

    @LuaWhitelist
    @LuaMethodDoc("host.is_flying")
    public boolean isFlying() {
        HostAPI host = host();
        return host != null && host.isFlying();
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_reach_distance")
    public double getReachDistance() {
        HostAPI host = host();
        return host != null ? host.getReachDistance() : 0;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_air")
    public int getAir() {
        HostAPI host = host();
        return host != null ? host.getAir() : 0;
    }

    @LuaWhitelist
    @LuaMethodDoc("host.get_status_effects")
    public List<Map<String, Object>> getStatusEffects() {
        HostAPI host = host();
        return host != null ? host.getStatusEffects() : new ArrayList<>();
    }
}
