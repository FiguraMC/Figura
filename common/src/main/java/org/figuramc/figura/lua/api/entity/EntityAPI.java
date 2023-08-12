package org.figuramc.figura.lua.api.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.NbtToLua;
import org.figuramc.figura.lua.ReadOnlyLuaTable;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.docs.LuaMetamethodDoc;
import org.figuramc.figura.lua.docs.LuaMetamethodDoc.LuaMetamethodOverload;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.mixin.EntityAccessor;
import org.figuramc.figura.utils.EntityUtils;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@LuaWhitelist
@LuaTypeDoc(
        name = "EntityAPI",
        value = "entity"
)
public class EntityAPI<T extends Entity> {

    protected final UUID entityUUID;
    protected T entity; // We just do not care about memory anymore so, just have something not wrapped in a WeakReference
    protected EntityType<T> type;

    private boolean thingy = true;
    private String cacheType;

    public EntityAPI(T entity) {
        this.entity = entity;
        //noinspection unchecked
        this.type   = (EntityType<T>) entity.getType();
        entityUUID = entity.getUUID();
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

    protected final void checkEntity() {
        if (entity.isRemoved() || getLevel() != Minecraft.getInstance().level) {
            Entity e = EntityUtils.getEntityByUUID(entityUUID);
            thingy = e != null;
            if (e != null && type.equals(e.getType())) {
                //noinspection unchecked
                T newEntityInstance = (T) e;
                if (thingy)
                    entity = newEntityInstance;
            }
        }
    }

    protected Level getLevel() {
        return ((EntityAccessor) entity).getLevel();
    }

    public T getEntity() {
        return entity;
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.is_loaded")
    public boolean isLoaded() {
        checkEntity();
        return thingy;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Float.class,
                            argumentNames = "delta"
                    )
            },
            value = "entity.get_pos"
    )
    public FiguraVec3 getPos(Float delta) {
        checkEntity();
        if (delta == null) delta = 1f;
        return FiguraVec3.fromVec3(entity.getPosition(delta));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Float.class,
                            argumentNames = "delta"
                    )
            },
            value = "entity.get_rot"
    )
    public FiguraVec2 getRot(Float delta) {
        checkEntity();
        if (delta == null) delta = 1f;
        return FiguraVec2.of(Mth.lerp(delta, entity.xRotO, entity.getXRot()), Mth.lerp(delta, entity.yRotO, entity.getYRot()));
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.get_uuid")
    public String getUUID() {
        return entityUUID.toString();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.get_type")
    public String getType() {
        checkEntity();
        return cacheType != null ? cacheType : (cacheType = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.get_velocity")
    public FiguraVec3 getVelocity() {
        checkEntity();
        return FiguraVec3.of(entity.getX() - entity.xOld, entity.getY() - entity.yOld, entity.getZ() - entity.zOld);
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.get_look_dir")
    public FiguraVec3 getLookDir() {
        checkEntity();
        return FiguraVec3.fromVec3(entity.getLookAngle());
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.get_frozen_ticks")
    public int getFrozenTicks() {
        checkEntity();
        return entity.getTicksFrozen();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.get_max_air")
    public int getMaxAir() {
        checkEntity();
        return entity.getMaxAirSupply();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.get_dimension_name")
    public String getDimensionName() {
        checkEntity();
        return getLevel().dimension().location().toString();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.get_pose")
    public String getPose() {
        checkEntity();
        return entity.getPose().toString();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.get_vehicle")
    public EntityAPI<?> getVehicle() {
        checkEntity();
        return wrap(entity.getVehicle());
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.is_on_ground")
    public boolean isOnGround() {
        checkEntity();
        return entity.onGround();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.get_eye_height")
    public float getEyeHeight() {
        checkEntity();
        return entity.getEyeHeight(entity.getPose());
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.get_bounding_box")
    public FiguraVec3 getBoundingBox() {
        checkEntity();
        EntityDimensions dim = entity.getDimensions(entity.getPose());
        return FiguraVec3.of(dim.width, dim.height, dim.width);
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.get_name")
    public String getName() {
        checkEntity();
        return entity.getName().getString();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.is_wet")
    public boolean isWet() {
        checkEntity();
        return entity.isInWaterRainOrBubble();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.is_in_water")
    public boolean isInWater() {
        checkEntity();
        return entity.isInWater();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.is_underwater")
    public boolean isUnderwater() {
        checkEntity();
        return entity.isUnderWater();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.is_in_lava")
    public boolean isInLava() {
        checkEntity();
        return entity.isInLava();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.is_in_rain")
    public boolean isInRain() {
        checkEntity();
        BlockPos blockPos = entity.blockPosition();
        return getLevel().isRainingAt(blockPos) || getLevel().isRainingAt(new BlockPos(blockPos.getX(), (int) entity.getBoundingBox().maxY, (int) entity.getZ()));
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.has_avatar")
    public boolean hasAvatar() {
        checkEntity();
        return AvatarManager.getAvatar(entity) != null;
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.is_sprinting")
    public boolean isSprinting() {
        checkEntity();
        return entity.isSprinting();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.get_eye_y")
    public double getEyeY() {
        checkEntity();
        return entity.getEyeY();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.is_glowing")
    public boolean isGlowing() {
        checkEntity();
        return entity.isCurrentlyGlowing();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.is_invisible")
    public boolean isInvisible() {
        checkEntity();
        return entity.isInvisible();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.is_silent")
    public boolean isSilent() {
        checkEntity();
        return entity.isSilent();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.is_sneaking")
    public boolean isSneaking() {
        checkEntity();
        return entity.isDiscrete();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.is_crouching")
    public boolean isCrouching() {
        checkEntity();
        return entity.isCrouching();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = int.class,
                    argumentNames = "index"
            ),
            value = "entity.get_item"
    )
    public ItemStackAPI getItem(int index) {
        checkEntity();
        if (--index < 0)
            return null;

        int i = 0;
        for (ItemStack item : entity.getAllSlots()) {
            if (i == index)
                return ItemStackAPI.verify(item);
            i++;
        }

        return null;
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.get_nbt")
    public LuaTable getNbt() {
        checkEntity();
        CompoundTag tag = new CompoundTag();
        entity.saveWithoutId(tag);
        return (LuaTable) NbtToLua.convert(tag);
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.is_on_fire")
    public boolean isOnFire() {
        checkEntity();
        return entity.displayFireAnimation();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.is_alive")
    public boolean isAlive() {
        checkEntity();
        return entity.isAlive();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.get_permission_level")
    public int getPermissionLevel() {
        checkEntity();
        return ((EntityAccessor) entity).getPermissionLevel();
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.get_passengers")
    public List<EntityAPI<?>> getPassengers() {
        checkEntity();

        List<EntityAPI<?>> list = new ArrayList<>();
        for (Entity passenger : entity.getPassengers())
            list.add(wrap(passenger));
        return list;
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.get_controlling_passenger")
    public EntityAPI<?> getControllingPassenger() {
        checkEntity();
        return wrap(entity.getControllingPassenger());
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.get_controlled_vehicle")
    public EntityAPI<?> getControlledVehicle() {
        checkEntity();
        return wrap(entity.getControlledVehicle());
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.has_container")
    public boolean hasContainer() {
        checkEntity();
        return entity instanceof ContainerEntity;
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.has_inventory")
    public boolean hasInventory() {
        checkEntity();
        return entity instanceof HasCustomInventoryScreen;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Boolean.class,
                            argumentNames = "ignoreLiquids"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Boolean.class, Double.class},
                            argumentNames = {"ignoreLiquids", "distance"}
                    )
            },
            value = "entity.get_targeted_block"
    )
    public Object[] getTargetedBlock(boolean ignoreLiquids, Double distance) {
        checkEntity();
        if (distance == null) distance = 20d;
        distance = Math.max(Math.min(distance, 20), -20);
        HitResult result = entity.pick(distance, 1f, !ignoreLiquids);
        return LuaUtils.parseBlockHitResult(result);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Double.class,
                            argumentNames = "distance"
                    )
            },
            value = "entity.get_targeted_entity"
    )
    public Object[] getTargetedEntity(Double distance) {
        checkEntity();
        if (distance == null) distance = 20d;
        distance = Math.max(Math.min(distance, 20), 0);

        Vec3 vec3 = entity.getEyePosition(1f);
        HitResult result = entity.pick(distance, 1f, false);

        if (result != null)
            distance = result.getLocation().distanceToSqr(vec3);

        Vec3 vec32 = entity.getViewVector(1f);
        Vec3 vec33 = vec3.add(vec32.x * distance, vec32.y * distance, vec32.z * distance);
        AABB aABB = entity.getBoundingBox().expandTowards(vec32.scale(distance)).inflate(1d);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(entity, vec3, vec33, aABB, e -> e != entity, distance);

        if (entityHit != null)
            return new Object[]{EntityAPI.wrap(entityHit.getEntity()), FiguraVec3.fromVec3(entityHit.getLocation())};

        return null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "key"
                    )
            },
            value = "entity.get_variable"
    )
    public LuaValue getVariable(String key) {
        checkEntity();
        Avatar a = AvatarManager.getAvatar(entity);
        LuaTable table;
        if (a == null || a.luaRuntime == null) {
            table = new ReadOnlyLuaTable(new LuaTable());
        } else {
            table = new ReadOnlyLuaTable(a.luaRuntime.avatar_meta.storedStuff, a.luaRuntime.typeManager);
        }
        return key == null ? table : table.get(key);
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.is_living")
    public boolean isLiving() {
        return this instanceof LivingEntityAPI<?>;
    }

    @LuaWhitelist
    @LuaMethodDoc("entity.is_player")
    public boolean isPlayer() {
        return this instanceof PlayerAPI;
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodOverload(
                    types = {boolean.class, EntityAPI.class, EntityAPI.class}
            )
    )
    public boolean __eq(EntityAPI<?> rhs) {
        return this.entity.equals(rhs.entity);
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodOverload(
                    types = {String.class, EntityAPI.class}
            )
    )
    public String __tostring() {
        return toString();
    }

    @Override
    public String toString() {
        checkEntity();
        return (entity.hasCustomName() ? entity.getCustomName().getString() + " (" + getType() + ")" : getType()) + " (Entity)";
    }
}
