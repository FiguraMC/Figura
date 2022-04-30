package org.moon.figura.lua.api.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.moon.figura.lua.LuaUtils;
import org.moon.figura.lua.LuaWhitelist;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
public class LivingEntityWrapper<T extends LivingEntity> extends EntityWrapper<T> {

    public LivingEntityWrapper(T wrapped) {
        super(wrapped);
    }

    @LuaWhitelist
    public static <T extends LivingEntity> Double getBodyYaw(LivingEntityWrapper<T> entity, Float delta) {
        LuaUtils.nullCheck("getBodyYaw", "entity", entity);
        if (!exists(entity)) throw new LuaRuntimeException("Entity does not exist!");
        if (delta == null) delta = 1f;
        return (double) Mth.lerp(delta, entity.getEntity().yBodyRotO, entity.getEntity().yBodyRot);
    }

}
