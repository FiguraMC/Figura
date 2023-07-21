package org.figuramc.figura.lua.api.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.mixin.LivingEntityAccessor;

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

    @LuaWhitelist
    @LuaMethodDoc("living_entity.get_absorption_amount")
    public float getAbsorptionAmount() {
        checkEntity();
        return entity.getAbsorptionAmount();
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.is_sensitive_to_water")
    public boolean isSensitiveToWater() {
        checkEntity();
        return entity.isSensitiveToWater();
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.get_entity_category")
    public String getEntityCategory() {
        checkEntity();

        MobType mobType = entity.getMobType(); // why it is not an enum
        if (mobType == MobType.ARTHROPOD)
            return "ARTHROPOD";
        if (mobType == MobType.UNDEAD)
            return "UNDEAD";
        if (mobType == MobType.WATER)
            return "WATER";
        if (mobType == MobType.ILLAGER)
            return "ILLAGER";

        return "UNDEFINED";
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.is_gliding")
    public boolean isGliding() {
        checkEntity();
        return entity.isFallFlying();
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.is_blocking")
    public boolean isBlocking() {
        checkEntity();
        return entity.isBlocking();
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.is_visually_swimming")
    public boolean isVisuallySwimming() {
        checkEntity();
        return entity.isVisuallySwimming();
    }

    @LuaWhitelist
    @LuaMethodDoc("living_entity.riptide_spinning")
    public boolean riptideSpinning() {
        checkEntity();
        return entity.isAutoSpinAttack();
    }

    @Override
    public String toString() {
        checkEntity();
        return (entity.hasCustomName() ? entity.getCustomName().getString() + " (" + getType() + ")" : getType() ) + " (LivingEntity)";
    }
}
