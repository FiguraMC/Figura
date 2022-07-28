package org.moon.figura.newlua.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.math.newvector.FiguraVec2;
import org.moon.figura.math.newvector.FiguraVec3;
import org.moon.figura.newlua.LuaType;
import org.moon.figura.newlua.LuaWhitelist;
import org.moon.figura.newlua.docs.LuaFunctionOverload;
import org.moon.figura.newlua.docs.LuaMetamethodDoc;
import org.moon.figura.newlua.docs.LuaMethodDoc;
import org.moon.figura.newlua.docs.LuaTypeDoc;

import java.util.UUID;

@LuaType(typeName = "entity")
@LuaTypeDoc(
        name = "Entity",
        description = "entity"
)
public class EntityAPI<T extends Entity> {

    protected final T entity; //We just do not care about memory anymore so, have something not wrapped in a WeakReference

    private String cacheType;

    public EntityAPI(T entity) {
        this.entity = entity;
    }

    public static EntityAPI<?> wrap(Entity e) {
        if (e == null)
            return null;
        if (e instanceof Player p)
            return new PlayerAPI(p);
        if (e instanceof LivingEntity le)
            return new LivingEntityAPI<>(le);
        return new EntityAPI<>(e);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload,
                    @LuaFunctionOverload(
                            argumentTypes = Float.class,
                            argumentNames = "delta"
                    )
            },
            description = "entity.get_pos"
    )
    public FiguraVec3 getPos(Float delta) {
        if (delta == null) delta = 1f;
        Vec3 pos = entity.getPosition(delta);
        return FiguraVec3.of(pos.x, pos.y, pos.z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload,
                    @LuaFunctionOverload(
                            argumentTypes = Float.class,
                            argumentNames = "delta"
                    )
            },
            description = "entity.get_rot"
    )
    public FiguraVec2 getRot(Float delta) {
        if (delta == null) delta = 1f;
        return FiguraVec2.of(Mth.lerp(delta, entity.xRotO, entity.getXRot()), Mth.lerp(delta, entity.yRotO, entity.getYRot()));
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.get_uuid")
    public String getUUID() {
        return entity.getUUID().toString();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.get_type")
    public String getType() {
        return cacheType != null ? cacheType : (cacheType = Registry.ENTITY_TYPE.getKey(entity.getType()).toString());
    }

    private static final UUID hambrgr = UUID.fromString("66a6c5c4-963b-4b73-a0d9-162faedd8b7f");
    @LuaWhitelist
    @LuaMethodDoc(description = "entity.is_hamburger")
    public boolean isHamburger() {
        return entity.getUUID().equals(hambrgr);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.get_velocity")
    public FiguraVec3 getVelocity() {
        return FiguraVec3.of(entity.getX() - entity.xOld, entity.getY() - entity.yOld, entity.getZ() - entity.zOld);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.get_look_dir")
    public FiguraVec3 getLookDir() {
        return FiguraVec3.fromVec3(entity.getLookAngle());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.get_fire_ticks")
    public int getFireTicks() {
        return entity.getRemainingFireTicks();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.get_frozen_ticks")
    public int getFrozenTicks() {
        return entity.getTicksFrozen();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.get_air")
    public int getAir() {
        return entity.getAirSupply();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.get_max_air")
    public int getMaxAir() {
        return entity.getMaxAirSupply();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.get_dimension_name")
    public String getDimensionName() {
        return entity.level.dimension().location().toString();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.get_pose")
    public String getPose() {
        return entity.getPose().toString();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.get_vehicle")
    public EntityAPI<?> getVehicle() {
        return wrap(entity.getVehicle());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.is_on_ground")
    public boolean isOnGround() {
        return entity.isOnGround();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.get_eye_height")
    public float getEyeHeight() {
        return entity.getEyeHeight(entity.getPose());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.get_bounding_box")
    public FiguraVec3 getBoundingBox() {
        EntityDimensions dim = entity.getDimensions(entity.getPose());
        return FiguraVec3.of(dim.width, dim.height, dim.width);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.get_name")
    public String getName() {
        return entity.getName().getString();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.is_wet")
    public boolean isWet() {
        return entity.isInWaterRainOrBubble();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.is_in_water")
    public boolean isInWater() {
        return entity.isInWater();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.is_underwater")
    public boolean isUnderwater() {
        return entity.isUnderWater();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.is_in_lava")
    public boolean isInLava() {
        return entity.isInLava();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.is_in_rain")
    public boolean isInRain() {
        BlockPos blockPos = entity.blockPosition();
        return entity.level.isRainingAt(blockPos) || entity.level.isRainingAt(new BlockPos(blockPos.getX(), entity.getBoundingBox().maxY, entity.getZ()));
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.has_avatar")
    public boolean hasAvatar() {
        return AvatarManager.getAvatar(entity) != null;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.has_avatar")
    public boolean isSprinting() {
        return entity.isSprinting();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.get_eye_y")
    public double getEyeY() {
        return entity.getEyeY();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            description = "entity.is_glowing"
    )
    public boolean isGlowing() {
        return entity.isCurrentlyGlowing();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.has_avatar")
    public boolean isInvisible() {
        return entity.isInvisible();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.is_silent")
    public boolean isSilent() {
        return entity.isSilent();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.is_sneaking")
    public boolean isSneaking() {
        return entity.isDiscrete();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "key"
            ),
            description = "entity.get_variable"
    )
    public Object getVariable(String key) {
        //We'll be back for you in a while... this is a system we want to overhaul.
        return null;
    }

//    @LuaWhitelist
//    @LuaMethodDoc(
//            overloads = @LuaFunctionOverload(
//                    argumentTypes = int.class,
//                    argumentNames = "index"
//            ),
//            description = "entity.get_item"
//    )
//    public ItemAPI getItem(int index) {
//
//    }

    //Also want to redo how nbt is accessed, it's often a waste of time to copy the *entire* nbt data into a table.

    @LuaWhitelist
    @LuaMethodDoc(description = "entity.is_on_fire")
    public boolean isOnFire() {
        return entity.displayFireAnimation();
    }

//    @LuaWhitelist
//    @LuaMethodDoc(
//            overloads = {
//                    @LuaFunctionOverload,
//                    @LuaFunctionOverload(
//                            argumentTypes = Boolean.class,
//                            argumentNames = "ignoreLiquids"
//                    ),
//                    @LuaFunctionOverload(
//                            argumentTypes = {Boolean.class, Double.class},
//                            argumentNames = {"ignoreLiquids", "distance"}
//                    )
//            },
//            description = "entity.get_targeted_block"
//    )
//    public BlockStateAPI getTargetedBlock(Boolean ignoreLiquids, Double distance) {
//        if (distance == null) distance = 20d;
//        distance = Math.max(Math.min(distance, 20), -20);
//        HitResult result = entity.pick(distance, 0f, !ignoreLiquids);
//        if (result instanceof BlockHitResult blockHit) {
//            BlockPos pos = blockHit.getBlockPos();
//            return new BlockStateWrapper(WorldAPI.getCurrentWorld().getBlockState(pos), pos);
//        }
//        return null;
//    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {boolean.class, EntityAPI.class, EntityAPI.class}
            )
    )
    public boolean __eq(EntityAPI<?> rhs) {
        return equals(rhs);
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {String.class, EntityAPI.class}
            )
    )
    public String __tostring() {
        return toString();
    }

    @Override
    public String toString() {
        return getType();
    }

}
