package org.figuramc.figura.lua.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.mixin.AABBInvoker;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import com.mojang.datafixers.util.Pair;

import kroppeb.stareval.function.Type.Int;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

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
            blockContext = blockCastType != null ? ClipContext.Block.valueOf(blockCastType.toUpperCase(Locale.US)) : ClipContext.Block.COLLIDER;
        }
        catch(IllegalArgumentException e){
            throw new LuaError("Invalid blockRaycastType provided");
        }

        ClipContext.Fluid fluidContext;
        try{
            fluidContext = fluidCastType != null ? ClipContext.Fluid.valueOf(fluidCastType.toUpperCase(Locale.US)) : ClipContext.Fluid.NONE;
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

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec3.class, FiguraVec3.class, LuaTable.class},
                            argumentNames = {"start", "end", "aabbs"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, FiguraVec3.class, LuaTable.class},
                            argumentNames = {"startX", "startY", "startZ", "end", "aabbs"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec3.class, Double.class, Double.class, Double.class, LuaTable.class},
                            argumentNames = {"start", "endX", "endY", "endZ", "aabbs"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, LuaTable.class},
                            argumentNames = {"startX", "startY", "startZ", "endX", "endY", "endZ", "aabbs"}
                    )
            }
            ,
            value = "raycast.aabb"
    )
    public Object[] aabb(Object x, Object y, Object z, Object w, Object t, Object h, LuaTable aabbs) {
        Vec3 start, end;

        Pair<Pair<FiguraVec3, FiguraVec3>, Object[]> pair = LuaUtils.parse2Vec3(
            "aabb", 
            new Class<?>[]{LuaTable.class},
            x, y, z, w, t, h, aabbs
        );

        start = pair.getFirst().getFirst().asVec3();
        end = pair.getFirst().getSecond().asVec3();

        aabbs = (LuaTable)pair.getSecond()[0];
        if (aabbs == null)
            throw new LuaError("Illegal argument to aabb(): Expected LuaTable, recieved nil");
        
        ArrayList<AABB> aabbList = new ArrayList<AABB>();
        for (int i=1;i<=aabbs.length();i++){
            LuaValue arg = aabbs.get(i);
            if (!arg.istable())
                throw new LuaError("Illegal argument at array index " + i + ": Expected table, recieved " + arg.typename() + " ("+arg.toString()+")");

            LuaValue min = arg.get(1);
            if (!min.isuserdata(FiguraVec3.class))
                throw new LuaError("Illegal argument to AABB at array index "+ i +" at index 1: Expected Vector3, recieved " + min.typename() + " ("+min.toString()+")");

            LuaValue max = arg.get(2);
            if (!max.isuserdata(FiguraVec3.class))
                throw new LuaError("Illegal argument to AABB at array index "+ i +" at index 2: Expected Vector3, recieved " + max.typename() + " ("+max.toString()+")");

            aabbList.add(new AABB(
                ((FiguraVec3)min.checkuserdata(FiguraVec3.class)).asVec3(), 
                ((FiguraVec3)max.checkuserdata(FiguraVec3.class)).asVec3()
            ));
        }

        // Modified from ProjectileUtil.getEntityHitResult to utilize arbitrary AABBs, and the custom clipAABB function
        // I was unable to figure out how the BlockState clipping worked, which would have been better.
        {
            double d = Double.MAX_VALUE;
            int index = -1;
            Pair<Vec3, Direction> result = null;
            
            for(int i = 0; i < aabbList.size(); i++) {
                AABB box = aabbList.get(i);
                Optional<Pair<Vec3, Direction>> optional = clipAABB(box, start, end);
                if (box.contains(start)) {
                    if (d >= 0.0) {
                        index = i+1;
                        result = optional.orElse(Pair.of(start, null));
                        d = 0.0;
                    }
                } else if (optional.isPresent()) {
                    Vec3 position = optional.get().getFirst();
                    double e = start.distanceToSqr(position);
                    if (e < d || d == 0.0) {
                        index = i+1;
                        result = optional.get();
                        d = e;
                    }
                }
            }

            if (index == -1) {
                return null;
            }

            return new Object[]{
                aabbs.get(index), 
                FiguraVec3.fromVec3(result.getFirst()), 
                result.getSecond()!=null ? result.getSecond().getName() : null,
                index
            };
        }
    }

    // Modified from AABB.clip(Vec3 min, Vec3 max) to also return the side hit
    public Optional<Pair<Vec3, Direction>> clipAABB(AABB aabb, Vec3 min, Vec3 max) {
        double[] ds = new double[]{1.0};
        double d = max.x - min.x;
        double e = max.y - min.y;
        double f = max.z - min.z;
        Direction direction = AABBInvoker.getDirection(aabb, min, ds, (Direction)null, d, e, f);
        if (direction == null) {
           return Optional.empty();
        } else {
           double g = ds[0];
           return Optional.of(Pair.of(min.add(g * d, g * e, g * f), direction));
        }
     }
}
