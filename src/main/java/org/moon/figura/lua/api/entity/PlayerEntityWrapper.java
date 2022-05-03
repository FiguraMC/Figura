package org.moon.figura.lua.api.entity;

import net.minecraft.world.entity.player.Player;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
public class PlayerEntityWrapper extends LivingEntityWrapper<Player> {

    public PlayerEntityWrapper(Player wrapped) {
        super(wrapped);
    }

    @LuaWhitelist
    public static Integer getFood(PlayerEntityWrapper entity) {
        LuaUtils.nullCheck("getFood", "entity", entity);
        if (!exists(entity)) throw new LuaRuntimeException("Entity does not exist!");
        return entity.getEntity().getFoodData().getFoodLevel();
    }

}
