package org.moon.figura.lua.api.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.mixin.ClientLevelInvoker;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.UUID;

@LuaWhitelist
@LuaTypeDoc(
        name = "Entity",
        description = "entity"
)
public class EntityWrapper<T extends Entity> {

    protected final UUID savedUUID;

    public EntityWrapper(UUID uuid) {
        savedUUID = uuid;
    }

    public static EntityWrapper<?> fromEntity(Entity entity) {
        if (entity instanceof Player)
            return new PlayerEntityWrapper(entity.getUUID());

        if (entity instanceof LivingEntity)
            return new LivingEntityWrapper<>(entity.getUUID());

        return new EntityWrapper<>(entity.getUUID());
    }

    protected T getEntity() {
        return getEntityByUUID(savedUUID);
    }

    private T getEntityByUUID(UUID uuid) {
        if (Minecraft.getInstance().level == null)
            return null;
        return (T) ((ClientLevelInvoker) Minecraft.getInstance().level).getEntityGetter().get(uuid);
    }

    protected static <T extends Entity> T getEntity(EntityWrapper<T> entity) {
        if (!exists(entity)) throw new LuaRuntimeException("Entity does not exist!");
        return entity.getEntity();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.exists"
    )
    public static <T extends Entity> boolean exists(EntityWrapper<T> entity) {
        return entity != null && entity.getEntity() != null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = EntityWrapper.class,
                            argumentNames = "entity"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {EntityWrapper.class, Float.class},
                            argumentNames = {"entity", "delta"}
                    )
            },
            description = "entity.get_pos"
    )
    public static <T extends Entity> FiguraVec3 getPos(EntityWrapper<T> entity, Float delta) {
        if (delta == null) delta = 1f;
        Vec3 pos = getEntity(entity).getPosition(delta);
        return FiguraVec3.of(pos.x, pos.y, pos.z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = EntityWrapper.class,
                            argumentNames = "entity"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {EntityWrapper.class, Float.class},
                            argumentNames = {"entity", "delta"}
                    )
            },
            description = "entity.get_rot"
    )
    public static <T extends Entity> FiguraVec2 getRot(EntityWrapper<T> entity, Float delta) {
        if (delta == null) delta = 1f;
        Entity e = getEntity(entity);
        return FiguraVec2.of(Mth.lerp(delta, e.xRotO, e.getXRot()), Mth.lerp(delta, e.yRotO, e.getYRot()));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.get_uuid"
    )
    public static <T extends Entity> String getUUID(EntityWrapper<T> entity) {
        return entity.savedUUID.toString();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.get_type"
    )
    public static <T extends Entity> String getType(EntityWrapper<T> entity) {
        return Registry.ENTITY_TYPE.getKey(getEntity(entity).getType()).toString();
    }

    @LuaWhitelist
    @LuaMethodDoc( //yes it should show on docs
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.is_hamburger"
    )
    public static <T extends Entity> boolean isHamburger(EntityWrapper<T> entity) {
        return entity.savedUUID.compareTo(UUID.fromString("66a6c5c4-963b-4b73-a0d9-162faedd8b7f")) == 0;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.get_velocity"
    )
    public static <T extends Entity> FiguraVec3 getVelocity(EntityWrapper<T> entity) {
        Entity e = getEntity(entity);
        return FiguraVec3.of(e.getX() - e.xOld, e.getY() - e.yOld, e.getZ() - e.yOld);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.get_look_dir"
    )
    public static <T extends Entity> FiguraVec3 getLookDir(EntityWrapper<T> entity) {
        Vec3 vec = getEntity(entity).getLookAngle();
        return FiguraVec3.of(vec.x, vec.y, vec.z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.get_fire_ticks"
    )
    public static <T extends Entity> int getFireTicks(EntityWrapper<T> entity) {
        return getEntity(entity).getRemainingFireTicks();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.get_frozen_ticks"
    )
    public static <T extends Entity> int getFrozenTicks(EntityWrapper<T> entity) {
        return getEntity(entity).getTicksFrozen();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.get_air"
    )
    public static <T extends Entity> int getAir(EntityWrapper<T> entity) {
        return getEntity(entity).getAirSupply();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.get_max_air"
    )
    public static <T extends Entity> int getMaxAir(EntityWrapper<T> entity) {
        return getEntity(entity).getMaxAirSupply();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.get_dimension_name"
    )
    public static <T extends Entity> String getDimensionName(EntityWrapper<T> entity) {
        return getEntity(entity).level.dimension().location().toString();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.get_pose"
    )
    public static <T extends Entity> String getPose(EntityWrapper<T> entity) {
        return getEntity(entity).getPose().toString();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.get_vehicle"
    )
    public static <T extends Entity> EntityWrapper<?> getVehicle(EntityWrapper<T> entity) {
        return fromEntity(getEntity(entity).getVehicle());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.is_on_ground"
    )
    public static <T extends Entity> boolean isOnGround(EntityWrapper<T> entity) {
        return getEntity(entity).isOnGround();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.get_eye_height"
    )
    public static <T extends Entity> float getEyeHeight(EntityWrapper<T> entity) {
        Entity e = getEntity(entity);
        return e.getEyeHeight(e.getPose());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.get_bounding_box"
    )
    public static <T extends Entity> FiguraVec3 getBoundingBox(EntityWrapper<T> entity) {
        Entity e = getEntity(entity);
        EntityDimensions dim = e.getDimensions(e.getPose());
        float x = dim.width;
        float y = dim.height;
        float z = dim.width;

        return FiguraVec3.of(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.get_name"
    )
    public static <T extends Entity> String getName(EntityWrapper<T> entity) {
        return getEntity(entity).getName().getString();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.is_wet"
    )
    public static <T extends Entity> boolean isWet(EntityWrapper<T> entity) {
        return getEntity(entity).isInWaterRainOrBubble();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.is_touching_water"
    )
    public static <T extends Entity> boolean isTouchingWater(EntityWrapper<T> entity) {
        return getEntity(entity).isInWater();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.is_underwater"
    )
    public static <T extends Entity> boolean isUnderwater(EntityWrapper<T> entity) {
        return getEntity(entity).isUnderWater();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.is_in_lava"
    )
    public static <T extends Entity> boolean isInLava(EntityWrapper<T> entity) {
        return getEntity(entity).isInLava();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.is_in_rain"
    )
    public static <T extends Entity> boolean isInRain(EntityWrapper<T> entity) {
        Entity e = getEntity(entity);
        BlockPos blockPos = e.blockPosition();
        return e.level.isRainingAt(blockPos) || e.level.isRainingAt(new BlockPos(blockPos.getX(), e.getBoundingBox().maxY, e.getZ()));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.has_avatar"
    )
    public static <T extends Entity> boolean hasAvatar(EntityWrapper<T> entity) {
        return AvatarManager.getAvatar(getEntity(entity)) != null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.is_sprinting"
    )
    public static <T extends Entity> boolean isSprinting(EntityWrapper<T> entity) {
        return getEntity(entity).isSprinting();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.get_eye_y"
    )
    public static <T extends Entity> double getEyeY(EntityWrapper<T> entity) {
        return getEntity(entity).getEyeY();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.is_glowing"
    )
    public static <T extends Entity> boolean isGlowing(EntityWrapper<T> entity) {
        return getEntity(entity).isCurrentlyGlowing();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.is_invisible"
    )
    public static <T extends Entity> boolean isInvisible(EntityWrapper<T> entity) {
        return getEntity(entity).isInvisible();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.is_silent"
    )
    public static <T extends Entity> boolean isSilent(EntityWrapper<T> entity) {
        return getEntity(entity).isSilent();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.is_sneaking"
    )
    public static <T extends Entity> boolean isSneaking(EntityWrapper<T> entity) {
        return getEntity(entity).isDiscrete();
    }

    @Override
    public String toString() {
        return savedUUID + " (Entity)";
    }
}
