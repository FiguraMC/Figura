package org.moon.figura.lua.api.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.NbtToLua;
import org.moon.figura.lua.api.world.BlockStateWrapper;
import org.moon.figura.lua.api.world.ItemStackWrapper;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaTable;
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
        this.savedUUID = uuid;
    }

    public static EntityWrapper<?> fromEntity(Entity entity) {
        if (entity == null)
            return null;

        if (entity instanceof Player)
            return new PlayerEntityWrapper(entity.getUUID());

        if (entity instanceof LivingEntity)
            return new LivingEntityWrapper<>(entity.getUUID());

        return new EntityWrapper<>(entity.getUUID());
    }

    public T getEntity() {
        return getEntityByUUID(savedUUID);
    }

    private T getEntityByUUID(UUID uuid) {
        if (Minecraft.getInstance().level == null)
            return null;
        return (T) ((ClientLevelInvoker) Minecraft.getInstance().level).getEntityGetter().get(uuid);
    }

    public static <T extends Entity> T getEntity(EntityWrapper<T> entity) {
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
    public static <T extends Entity> boolean exists(@LuaNotNil EntityWrapper<T> entity) {
        return entity.getEntity() != null;
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
    public static <T extends Entity> FiguraVec3 getPos(@LuaNotNil EntityWrapper<T> entity, Float delta) {
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
    public static <T extends Entity> FiguraVec2 getRot(@LuaNotNil EntityWrapper<T> entity, Float delta) {
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
    public static <T extends Entity> String getUUID(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> String getType(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> boolean isHamburger(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> FiguraVec3 getVelocity(@LuaNotNil EntityWrapper<T> entity) {
        Entity e = getEntity(entity);
        return FiguraVec3.of(e.getX() - e.xOld, e.getY() - e.yOld, e.getZ() - e.zOld);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.get_look_dir"
    )
    public static <T extends Entity> FiguraVec3 getLookDir(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> int getFireTicks(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> int getFrozenTicks(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> int getAir(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> int getMaxAir(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> String getDimensionName(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> String getPose(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> EntityWrapper<?> getVehicle(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> boolean isOnGround(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> float getEyeHeight(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> FiguraVec3 getBoundingBox(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> String getName(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> boolean isWet(@LuaNotNil EntityWrapper<T> entity) {
        return getEntity(entity).isInWaterRainOrBubble();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.is_in_water"
    )
    public static <T extends Entity> boolean isInWater(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> boolean isUnderwater(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> boolean isInLava(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> boolean isInRain(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> boolean hasAvatar(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> boolean isSprinting(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> double getEyeY(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> boolean isGlowing(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> boolean isInvisible(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> boolean isSilent(@LuaNotNil EntityWrapper<T> entity) {
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
    public static <T extends Entity> boolean isSneaking(@LuaNotNil EntityWrapper<T> entity) {
        return getEntity(entity).isDiscrete();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {EntityWrapper.class, String.class},
                    argumentNames = {"entity", "key"}
            ),
            description = "entity.get_variable"
    )
    public static <T extends Entity> Object getVariable(@LuaNotNil EntityWrapper<T> entity, @LuaNotNil String key) {
        Avatar a = AvatarManager.getAvatarForPlayer(entity.savedUUID);
        if (a == null || a.luaState == null)
            return null;

        return a.luaState.storedStuff.getValue(key, Object.class);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {EntityWrapper.class, Integer.class},
                    argumentNames = {"entity", "index"}
            ),
            description = "entity.get_item"
    )
    public static <T extends Entity> ItemStackWrapper getItem(@LuaNotNil EntityWrapper<T> entity, @LuaNotNil Integer index) {
        if (--index < 0)
            return null;

        int i = 0;
        for (ItemStack item : getEntity(entity).getAllSlots()) {
            if (i == index)
                return ItemStackWrapper.verify(item);
            i++;
        }

        return null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.get_nbt"
    )
    public static <T extends Entity> LuaTable getNbt(@LuaNotNil EntityWrapper<T> entity) {
        CompoundTag tag = new CompoundTag();
        getEntity(entity).saveWithoutId(tag);
        return (LuaTable) NbtToLua.convert(tag);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = EntityWrapper.class,
                    argumentNames = "entity"
            ),
            description = "entity.is_on_fire"
    )
    public static <T extends Entity> boolean isOnFire(@LuaNotNil EntityWrapper<T> entity) {
        return getEntity(entity).displayFireAnimation();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = EntityWrapper.class,
                            argumentNames = "entity"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {EntityWrapper.class, Boolean.class},
                            argumentNames = {"entity", "ignoreLiquids"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {EntityWrapper.class, Boolean.class, Double.class},
                            argumentNames = {"entity", "ignoreLiquids", "distance"}
                    )
            },
            description = "entity.get_targeted_block"
    )
    public static <T extends Entity> BlockStateWrapper getTargetedBlock(@LuaNotNil EntityWrapper<T> entity, Boolean ignoreLiquids, Double distance) {
        if (distance == null) distance = 20d;
        distance = Math.max(Math.min(distance, 20), -20);
        HitResult result = getEntity(entity).pick(distance, 0f, !ignoreLiquids);
        if (result instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            return new BlockStateWrapper(WorldAPI.getCurrentWorld().getBlockState(pos), pos);
        }
        return null;
    }

    @Override
    public String toString() {
        return savedUUID + " (Entity)";
    }
}
