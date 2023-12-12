package org.figuramc.figura.lua.api;

import java.util.function.Predicate;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;

import com.mojang.datafixers.util.Pair;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

@LuaWhitelist
@LuaTypeDoc(
        name = "RaycastAPI",
        value = "raycast"
)
public class RaycastAPI {
    
    private final Avatar owner;

    public RaycastAPI(Avatar owner) {
        this.owner = owner;
    }

    
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {String.class, String.class, FiguraVec3.class, FiguraVec3.class},
                            argumentNames = {"blockCastType", "fluidCastType", "start", "end"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, String.class, Double.class, Double.class, Double.class, FiguraVec3.class},
                            argumentNames = {"blockCastType", "fluidCastType", "startX", "startY", "startZ", "end"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, String.class, FiguraVec3.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockCastType", "fluidCastType", "start", "endX", "endY", "endZ"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, String.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockCastType", "fluidCastType", "startX", "startY", "startZ", "endX", "endY", "endZ"}
                    )
                }
            ,
            value = "raycast.block"
    )
    public Object[] block(String blockCastType, String fluidCastType, Object x, Object y, Double z, Object w, Double t, Double h) {
        FiguraVec3 start, end;

        Pair<FiguraVec3, FiguraVec3> pair = LuaUtils.parse2Vec3("block", x, y, z, w, t, h,1);
        start = pair.getFirst();
        end = pair.getSecond();

        ClipContext.Block blockContext;
        try{
            blockContext = blockCastType != null ? ClipContext.Block.valueOf(blockCastType.toUpperCase()) : ClipContext.Block.COLLIDER;
        }
        catch(IllegalArgumentException e){
            throw new LuaError("Invalid blockRaycastType provided");
        }

        ClipContext.Fluid fluidContext;
        try{
            fluidContext = fluidCastType != null ? ClipContext.Fluid.valueOf(fluidCastType.toUpperCase()) : ClipContext.Fluid.NONE;
        }
        catch(IllegalArgumentException e){
            throw new LuaError("Invalid fluidRaycastType provided");
        }

        BlockHitResult result = WorldAPI.getCurrentWorld().clip(new ClipContext(start.asVec3(), end.asVec3(), blockContext, fluidContext, new Marker(EntityType.MARKER, WorldAPI.getCurrentWorld())));
        return LuaUtils.parseBlockHitResult(result);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {LuaFunction.class, FiguraVec3.class, FiguraVec3.class},
                            argumentNames = {"predicate", "start", "end"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {LuaFunction.class, Double.class, Double.class, Double.class, FiguraVec3.class},
                            argumentNames = {"predicate", "startX", "startY", "startZ", "end"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {LuaFunction.class, FiguraVec3.class, Double.class, Double.class, Double.class},
                            argumentNames = {"predicate", "start", "endX", "endY", "endZ"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {LuaFunction.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"predicate", "startX", "startY", "startZ", "endX", "endY", "endZ"}
                    )
            }
            ,
            value = "raycast.entity"
    )
    public Object[] entity(LuaFunction predicate, Object x, Object y, Double z, Object w, Double t, Double h) {
        FiguraVec3 start, end;

        Pair<FiguraVec3, FiguraVec3> pair = LuaUtils.parse2Vec3("entity", x, y, z, w, t, h, 1);
        start = pair.getFirst();
        end = pair.getSecond();

        Predicate<Entity> entityPredicate = (entity) -> {
            if (predicate == null) return true;
            LuaValue result = predicate.invoke(this.owner.luaRuntime.typeManager.javaToLua(EntityAPI.wrap(entity))).arg1();
            if ((result.isboolean() && result.checkboolean() == false) || result.isnil())
                return false;
            return true;
        };

        EntityHitResult result = ProjectileUtil.getEntityHitResult(new Marker(EntityType.MARKER, WorldAPI.getCurrentWorld()), start.asVec3(), end.asVec3(), new AABB(start.asVec3(), end.asVec3()), entityPredicate, Double.MAX_VALUE);

        if (result != null)
            return new Object[]{EntityAPI.wrap(result.getEntity()), FiguraVec3.fromVec3(result.getLocation())};

        return null;
    }
}
