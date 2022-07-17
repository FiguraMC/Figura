package org.moon.figura.newlua.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.entity.EntityWrapper;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.newlua.LuaType;

@LuaType(typeName = "entity")
public class EntityAPI<T extends Entity> {

    private T entity;

    public EntityAPI(T entity) {
        this.entity = entity;
    }


    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = EntityAPI.class,
                            argumentNames = "entity"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {EntityAPI.class, Float.class},
                            argumentNames = {"entity", "delta"}
                    )
            },
            description = "entity.get_pos"
    )
    public FiguraVec3 getPos(Float delta) {
        if (delta == null) delta = 1f;
        Vec3 pos = entity.getPosition(delta);
        return FiguraVec3.of(pos.x, pos.y, pos.z);
    }


}
