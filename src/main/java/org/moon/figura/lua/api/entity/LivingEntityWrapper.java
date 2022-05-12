package org.moon.figura.lua.api.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.ItemStackWrapper;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaTable;

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
                            argumentNames = "entity"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {LivingEntityWrapper.class, Float.class},
                            argumentNames = {"entity", "delta"}
                    )
            },
            description = "living_entity.get_body_yaw"
    )
    public static <T extends LivingEntity> Double getBodyYaw(@LuaNotNil LivingEntityWrapper<T> entity, Float delta) {
        if (delta == null) delta = 1f;
        LivingEntity e = getEntity(entity);
        return (double) Mth.lerp(delta, e.yBodyRotO, e.yBodyRot);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LivingEntityWrapper.class,
                            argumentNames = "entity"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {LivingEntityWrapper.class, Boolean.class},
                            argumentNames = {"entity", "offhand"}
                    )
            },
            description = "living_entity.get_held_item"
    )
    public static <T extends LivingEntity> ItemStackWrapper getHeldItem(@LuaNotNil LivingEntityWrapper<T> entity, Boolean offhand) {
        if (offhand == null) offhand = false;
        LivingEntity e = getEntity(entity);
        ItemStack stack = offhand ? e.getOffhandItem() : e.getMainHandItem();
        return ItemStackWrapper.verify(stack);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LivingEntityWrapper.class,
                            argumentNames = "entity"
                    )
            },
            description = "living_entity.get_health"
    )
    public static <T extends LivingEntity> float getHealth(@LuaNotNil LivingEntityWrapper<T> entity) {
        return getEntity(entity).getHealth();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LivingEntityWrapper.class,
                            argumentNames = "entity"
                    )
            },
            description = "living_entity.get_max_health"
    )
    public static <T extends LivingEntity> float getMaxHealth(@LuaNotNil LivingEntityWrapper<T> entity) {
        return getEntity(entity).getMaxHealth();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LivingEntityWrapper.class,
                            argumentNames = "entity"
                    )
            },
            description = "living_entity.get_armor"
    )
    public static <T extends LivingEntity> int getArmor(@LuaNotNil LivingEntityWrapper<T> entity) {
        return getEntity(entity).getArmorValue();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LivingEntityWrapper.class,
                            argumentNames = "entity"
                    )
            },
            description = "living_entity.get_death_time"
    )
    public static <T extends LivingEntity> int getDeathTime(@LuaNotNil LivingEntityWrapper<T> entity) {
        return getEntity(entity).deathTime;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LivingEntityWrapper.class,
                            argumentNames = "entity"
                    )
            },
            description = "living_entity.get_status_effects"
    )
    public static <T extends LivingEntity> LuaTable getStatusEffects(@LuaNotNil LivingEntityWrapper<T> entity) {
        LuaTable tbl = new LuaTable();

        int i = 1;
        for (MobEffectInstance effect : getEntity(entity).getActiveEffects()) {
            LuaTable effectTbl = new LuaTable();
            effectTbl.put("name", effect.getEffect().getDescriptionId());
            effectTbl.put("amplifier", effect.getAmplifier());
            effectTbl.put("duration", effect.getDuration());
            effectTbl.put("visible", effect.isVisible());

            tbl.put(i, effectTbl);
            i++;
        }

        return tbl;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LivingEntityWrapper.class,
                            argumentNames = "entity"
                    )
            },
            description = "living_entity.get_arrow_count"
    )
    public static <T extends LivingEntity> int getArrowCount(@LuaNotNil LivingEntityWrapper<T> entity) {
        return getEntity(entity).getArrowCount();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LivingEntityWrapper.class,
                            argumentNames = "entity"
                    )
            },
            description = "living_entity.get_stinger_count"
    )
    public static <T extends LivingEntity> int getStingerCount(@LuaNotNil LivingEntityWrapper<T> entity) {
        return getEntity(entity).getStingerCount();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LivingEntityWrapper.class,
                            argumentNames = "entity"
                    )
            },
            description = "living_entity.is_left_handed"
    )
    public static <T extends LivingEntity> boolean isLeftHanded(@LuaNotNil LivingEntityWrapper<T> entity) {
        return getEntity(entity).getMainArm() == HumanoidArm.LEFT;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LivingEntityWrapper.class,
                            argumentNames = "entity"
                    )
            },
            description = "living_entity.is_using_item"
    )
    public static <T extends LivingEntity> boolean isUsingItem(@LuaNotNil LivingEntityWrapper<T> entity) {
        return getEntity(entity).isUsingItem();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LivingEntityWrapper.class,
                            argumentNames = "entity"
                    )
            },
            description = "living_entity.get_active_hand"
    )
    public static <T extends LivingEntity> String getActiveHand(@LuaNotNil LivingEntityWrapper<T> entity) {
        return getEntity(entity).getUsedItemHand().toString();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LivingEntityWrapper.class,
                            argumentNames = "entity"
                    )
            },
            description = "living_entity.get_active_item"
    )
    public static <T extends LivingEntity> ItemStackWrapper getActiveItem(@LuaNotNil LivingEntityWrapper<T> entity) {
        ItemStack useItem = getEntity(entity).getUseItem();
        return ItemStackWrapper.verify(useItem);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LivingEntityWrapper.class,
                            argumentNames = "entity"
                    )
            },
            description = "living_entity.is_climbing"
    )
    public static <T extends LivingEntity> boolean isClimbing(@LuaNotNil LivingEntityWrapper<T> entity) {
        return getEntity(entity).onClimbable();
    }

    @Override
    public String toString() {
        return savedUUID + " (LivingEntity)";
    }
}
