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
                            argumentTypes = {FiguraVec3.class, FiguraVec3.class, String.class, String.class},
                            argumentNames = {"start", "end", "blockCastType", "fluidCastType"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, FiguraVec3.class, String.class, String.class},
                            argumentNames = {"startX", "startY", "startZ", "end", "blockCastType", "fluidCastType"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec3.class, Double.class, Double.class, Double.class, String.class, String.class},
                            argumentNames = {"start", "endX", "endY", "endZ", "blockCastType", "fluidCastType"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, String.class, String.class},
                            argumentNames = {"startX", "startY", "startZ", "endX", "endY", "endZ", "blockCastType", "fluidCastType"}
                    )
                }
            ,
            value = "raycast.block"
    )
    public Object[] block(Object x, Object y, Object z, Object w, Object t, Object h, String blockCastType, String fluidCastType) {
        FiguraVec3 start, end;
        Pair<Pair<FiguraVec3, FiguraVec3>, Object[]> parseResult = LuaUtils.parse2Vec3(
            "block", 
            new Class<?>[]{String.class, String.class}, 
            x, y, z, w, t, h, blockCastType, fluidCastType
        );

        start = parseResult.getFirst().getFirst();
        end = parseResult.getFirst().getSecond();

        blockCastType = (String)parseResult.getSecond()[0];
        fluidCastType = (String)parseResult.getSecond()[1];

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
                            argumentTypes = {FiguraVec3.class, FiguraVec3.class, LuaFunction.class},
                            argumentNames = {"start", "end", "predicate"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, FiguraVec3.class, LuaFunction.class},
                            argumentNames = {"startX", "startY", "startZ", "end", "predicate"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec3.class, Double.class, Double.class, Double.class, LuaFunction.class},
                            argumentNames = {"start", "endX", "endY", "endZ", "predicate"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, LuaFunction.class},
                            argumentNames = {"startX", "startY", "startZ", "endX", "endY", "endZ", "predicate"}
                    )
            }
            ,
            value = "raycast.entity"
    )
    public Object[] entity(Object x, Object y, Object z, Object w, Object t, Double h, LuaFunction predicate) {
        FiguraVec3 start, end;

        Pair<Pair<FiguraVec3, FiguraVec3>, Object[]> pair = LuaUtils.parse2Vec3(
            "entity", 
            new Class<?>[]{LuaFunction.class},
            x, y, z, w, t, h, predicate);

        start = pair.getFirst().getFirst();
        end = pair.getFirst().getSecond();

        final LuaFunction fn = (LuaFunction)pair.getSecond()[0];

        Predicate<Entity> entityPredicate = (entity) -> {
            if (fn == null) return true;
            LuaValue result = fn.invoke(this.owner.luaRuntime.typeManager.javaToLua(EntityAPI.wrap(entity))).arg1();
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
