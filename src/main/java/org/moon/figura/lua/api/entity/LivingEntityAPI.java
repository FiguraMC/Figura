package org.moon.figura.lua.api.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.ItemStackAPI;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.mixin.LivingEntityAccessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@LuaWhitelist
@LuaTypeDoc(
        name = "LivingEntityAPI",
        value = "living_entity"
)
public class LivingEntityAPI<T extends LivingEntity> extends EntityAPI<T> {

    public LivingEntityAPI(T entity) {
        super(entity);
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
            value = "living_entity.get_body_yaw"
    )
    public double getBodyYaw(Float delta) {
        checkEntity();
        if (delta == null) delta = 1f;
        return Mth.lerp(delta, entity.yBodyRotO, entity.yBodyRot);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Boolean.class,
                            argumentNames = "offhand"
                    )
            },
            value = "living_entity.get_held_item"
    )
    public ItemStackAPI getHeldItem(boolean offhand) {
        checkEntity();
        return ItemStackAPI.verify(offhand ? entity.getOffhandItem() : entity.getMainHandItem());
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.get_active_item")
    public ItemStackAPI getActiveItem() {
        checkEntity();
        return ItemStackAPI.verify(entity.getUseItem());
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.get_active_item_time")
    public int getActiveItemTime() {
        checkEntity();
        return entity.getTicksUsingItem();
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.get_health")
    public float getHealth() {
        checkEntity();
        return entity.getHealth();
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.get_max_health")
    public float getMaxHealth() {
        checkEntity();
        return entity.getMaxHealth();
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.get_armor")
    public float getArmor() {
        checkEntity();
        return entity.getArmorValue();
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.get_death_time")
    public float getDeathTime() {
        checkEntity();
        return entity.deathTime;
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.get_status_effects")
    public List<Map<String, Object>> getStatusEffects() {
        checkEntity();
        List<Map<String, Object>> list = new ArrayList<>();

        for (MobEffectInstance effect : entity.getActiveEffects()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", effect.getEffect().getDescriptionId());
            map.put("amplifier", effect.getAmplifier());
            map.put("duration", effect.getDuration());
            map.put("visible", effect.isVisible());

            list.add(map);
        }

        return list;
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.get_arrow_count")
    public int getArrowCount() {
        checkEntity();
        return entity.getArrowCount();
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.get_stinger_count")
    public int getStingerCount() {
        checkEntity();
        return entity.getStingerCount();
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.is_left_handed")
    public boolean isLeftHanded() {
        checkEntity();
        return entity.getMainArm() == HumanoidArm.LEFT;
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.is_using_item")
    public boolean isUsingItem() {
        checkEntity();
        return entity.isUsingItem();
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.get_active_hand")
    public String getActiveHand() {
        checkEntity();
        return entity.getUsedItemHand().name();
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.is_climbing")
    public boolean isClimbing() {
        checkEntity();
        return entity.onClimbable();
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.get_swing_time")
    public int getSwingTime() {
      checkEntity();
      return entity.swingTime;
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.is_swinging_arm")
    public boolean isSwingingArm() {
      checkEntity();
      return entity.swinging;
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.get_swing_arm")
    public String getSwingArm() {
      checkEntity();
      return entity.swinging ? entity.swingingArm.name() : null;
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.get_swing_duration")
    public int getSwingDuration() {
      checkEntity();
      return ((LivingEntityAccessor) entity).getSwingDuration();
    }

    @Override
    public String toString() {
        checkEntity();
        return (entity.hasCustomName() ? entity.getCustomName().getString() + " (" + getType() + ")" : getType() ) + " (LivingEntity)";
    }
}
