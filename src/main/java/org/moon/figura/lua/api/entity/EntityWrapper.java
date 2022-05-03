package org.moon.figura.lua.api.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

import java.lang.ref.WeakReference;

@LuaWhitelist
public class EntityWrapper<T extends Entity> {

    private final WeakReference<T> entity;

    protected T getEntity() {
        return entity.get();
    }

    public EntityWrapper(T wrapped) {
        entity = new WeakReference<>(wrapped);
    }

    @LuaWhitelist
    public static <T extends Entity> boolean exists(EntityWrapper<T> entity) {
        return entity.getEntity() != null;
    }

    @LuaWhitelist
    public static <T extends Entity> FiguraVec3 getPos(EntityWrapper<T> entity, Float delta) {
        LuaUtils.nullCheck("getPos", "entity", entity);
        if (!exists(entity)) throw new LuaRuntimeException("Entity does not exist!");
        if (delta == null) delta = 1f;
        Vec3 pos = entity.getEntity().getPosition(delta);
        return FiguraVec3.of(pos.x, pos.y, pos.z);
    }

    @LuaWhitelist
    public static <T extends Entity> FiguraVec2 getRot(EntityWrapper<T> entity, Float delta) {
        LuaUtils.nullCheck("getRot", "entity", entity);
        if (!exists(entity)) throw new LuaRuntimeException("Entity does not exist!");
        if (delta == null) delta = 1f;
        T e = entity.getEntity();
        return FiguraVec2.of(Mth.lerp(delta, e.xRotO, e.getXRot()), Mth.lerp(delta, e.yRotO, e.getYRot()));
    }

    //TODO: add more of course :p

}
