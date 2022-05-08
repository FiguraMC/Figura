package org.moon.figura.lua.api.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.UUID;

@LuaWhitelist
@LuaTypeDoc(
        name = "LivingEntity",
        description = "living_entity"
)
public class LivingEntityWrapper<T extends LivingEntity> extends EntityWrapper<T> {

    public LivingEntityWrapper(UUID uuid) {
        super(uuid);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LivingEntityWrapper.class,
                            argumentNames = "entity",
                            returnType = Double.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {LivingEntityWrapper.class, Float.class},
                            argumentNames = {"entity", "delta"},
                            returnType = Double.class
                    )
            },
            description = "living_entity.get_body_yaw"
    )
    public static <T extends LivingEntity> Double getBodyYaw(LivingEntityWrapper<T> entity, Float delta) {
        LuaUtils.nullCheck("getBodyYaw", "entity", entity);
        if (!exists(entity)) throw new LuaRuntimeException("Entity does not exist!");
        if (delta == null) delta = 1f;
        return (double) Mth.lerp(delta, entity.getEntity().yBodyRotO, entity.getEntity().yBodyRot);
    }

}
