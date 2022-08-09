package org.moon.figura.lua.api.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.ItemStackAPI;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

@LuaWhitelist
@LuaTypeDoc(
        name = "LivingEntityAPI",
        description = "living_entity"
)
public class LivingEntityAPI<T extends LivingEntity> extends EntityAPI<T> {

    public LivingEntityAPI(T entity) {
        super(entity);
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
            description = "living_entity.get_body_yaw"
    )
    public double getBodyYaw(Float delta) {
        if (delta == null) delta = 1f;
        return Mth.lerp(delta, entity.yBodyRotO, entity.yBodyRot);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload,
                    @LuaFunctionOverload(
                            argumentTypes = Boolean.class,
                            argumentNames = "offhand"
                    )
            },
            description = "living_entity.get_held_item"
    )
    public ItemStackAPI getHeldItem(boolean offhand) {
        return ItemStackAPI.verify(offhand ? entity.getOffhandItem() : entity.getMainHandItem());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "living_entity.get_active_item")
    public ItemStackAPI getActiveItem() {
        return ItemStackAPI.verify(entity.getUseItem());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "living_entity.get_health")
    public float getHealth() {
        return entity.getHealth();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "living_entity.get_max_health")
    public float getMaxHealth() {
        return entity.getMaxHealth();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "living_entity.get_armor")
    public float getArmor() {
        return entity.getArmorValue();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "living_entity.get_death_time")
    public float getDeathTime() {
        return entity.deathTime;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "living_entity.get_status_effects")
    public LuaTable getStatusEffects() {
        LuaTable tbl = new LuaTable();

        int i = 1;
        for (MobEffectInstance effect : entity.getActiveEffects()) {
            LuaTable effectTbl = new LuaTable();
            effectTbl.set("name", effect.getEffect().getDescriptionId());
            effectTbl.set("amplifier", effect.getAmplifier());
            effectTbl.set("duration", effect.getDuration());
            effectTbl.set("visible", LuaValue.valueOf(effect.isVisible()));

            tbl.set(i, effectTbl);
            i++;
        }

        return tbl;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "living_entity.get_arrow_count")
    public int getArrowCount() {
        return entity.getArrowCount();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "living_entity.get_stinger_count")
    public int getStingerCount() {
        return entity.getStingerCount();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "living_entity.is_left_handed")
    public boolean isLeftHanded() {
        return entity.getMainArm() == HumanoidArm.LEFT;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "living_entity.is_using_item")
    public boolean isUsingItem() {
        return entity.isUsingItem();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "living_entity.get_active_hand")
    public String getActiveHand() {
        return entity.getUsedItemHand().toString();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "living_entity.is_climbing")
    public boolean isClimbing() {
        return entity.onClimbable();
    }

    @Override
    public String toString() {
        return (entity.hasCustomName() ? entity.getCustomName().getString() + " (" + getType() + ")" : getType() ) + " (LivingEntity)";
    }
}
