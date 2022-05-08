package org.moon.figura.lua.api.entity;

import net.minecraft.world.entity.player.Player;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.UUID;

@LuaWhitelist
@LuaTypeDoc(
        name = "Player",
        description = "player"
)
public class PlayerEntityWrapper extends LivingEntityWrapper<Player> {

    public PlayerEntityWrapper(UUID uuid) {
        super(uuid);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = PlayerEntityWrapper.class,
                    argumentNames = "entity",
                    returnType = Integer.class
            ),
            description = "player.get_food"
    )
    public static Integer getFood(PlayerEntityWrapper entity) {
        LuaUtils.nullCheck("getFood", "entity", entity);
        if (!exists(entity)) throw new LuaRuntimeException("Entity does not exist!");
        return entity.getEntity().getFoodData().getFoodLevel();
    }

}
