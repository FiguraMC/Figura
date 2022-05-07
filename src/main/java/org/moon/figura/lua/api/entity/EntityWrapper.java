package org.moon.figura.lua.api.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.mixin.ClientLevelInvoker;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

import java.lang.ref.WeakReference;
import java.util.UUID;

@LuaWhitelist
@LuaTypeDoc(
        name = "Entity",
        description = "Acts as a proxy for an entity in the Minecraft world."
)
public class EntityWrapper<T extends Entity> {

    private final UUID savedUUID;

    protected T getEntity() {
        return getEntityByUUID(savedUUID);
    }

    public EntityWrapper(UUID uuid) {
        savedUUID = uuid;
    }

    private T getEntityByUUID(UUID uuid) {
        if (Minecraft.getInstance().level == null)
            return null;
        return (T) ((ClientLevelInvoker) Minecraft.getInstance().level).getEntityGetter().get(uuid);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity",
                    returnType = boolean.class
            ),
            description = "Checks whether the proxied entity exists. This returns " +
                    "false if the proxied entity leaves your loaded chunks, dies, " +
                    "or goes away for some other reason."
    )
    public static <T extends Entity> boolean exists(EntityWrapper<T> entity) {
        return entity.getEntity() != null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = EntityWrapper.class,
                            argumentNames = "entity",
                            returnType = FiguraVec3.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {EntityWrapper.class, Float.class},
                            argumentNames = {"entity", "delta"},
                            returnType = FiguraVec3.class
                    )
            },
            description = "Gets the position of the proxied entity in the world. " +
                    "If delta is passed in, then it will be used to linearly interpolate " +
                    "the position of the entity between the previous tick and the current tick. " +
                    "The default value of delta is 1."
    )
    public static <T extends Entity> FiguraVec3 getPos(EntityWrapper<T> entity, Float delta) {
        LuaUtils.nullCheck("getPos", "entity", entity);
        if (!exists(entity)) throw new LuaRuntimeException("Entity does not exist!");
        if (delta == null) delta = 1f;
        Vec3 pos = entity.getEntity().getPosition(delta);
        return FiguraVec3.of(pos.x, pos.y, pos.z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = EntityWrapper.class,
                            argumentNames = "entity",
                            returnType = FiguraVec2.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {EntityWrapper.class, Float.class},
                            argumentNames = {"entity", "delta"},
                            returnType = FiguraVec2.class
                    )
            },
            description = "Gets the rotation of the proxied entity in degrees. " +
                    "If delta is passed in, then it will be used to linearly interpolate " +
                    "the rotation of the entity between the previous tick and the current tick. " +
                    "The default value of delta is 1."
    )
    public static <T extends Entity> FiguraVec2 getRot(EntityWrapper<T> entity, Float delta) {
        LuaUtils.nullCheck("getRot", "entity", entity);
        if (!exists(entity)) throw new LuaRuntimeException("Entity does not exist!");
        if (delta == null) delta = 1f;
        T e = entity.getEntity();
        return FiguraVec2.of(Mth.lerp(delta, e.xRotO, e.getXRot()), Mth.lerp(delta, e.yRotO, e.getYRot()));
    }

    //TODO: add more of course :p

}
