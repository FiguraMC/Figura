package org.moon.figura.newlua.api.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.moon.figura.newlua.LuaType;
import org.moon.figura.newlua.LuaWhitelist;
import org.moon.figura.newlua.docs.LuaFunctionOverload;
import org.moon.figura.newlua.docs.LuaMethodDoc;
import org.moon.figura.newlua.docs.LuaTypeDoc;

@LuaType(typeName = "livingEntity")
@LuaTypeDoc(
        name = "LivingEntity",
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

//    @LuaWhitelist
//    @LuaMethodDoc(
//            overloads = {
//                    @LuaFunctionOverload,
//                    @LuaFunctionOverload(
//                            argumentTypes = Boolean.class,
//                            argumentNames = "offhand"
//                    )
//            },
//            description = "living_entity.get_held_item"
//    )
//    public ItemAPI getHeldItem(boolean offhand) {
//        return ItemAPI.verify(offhand ? entity.getOffhandItem() : entity.getMainHandItem());
//    }


//    @LuaWhitelist
//    @LuaMethodDoc(description = "living_entity.get_active_item")
//    public ItemAPI getActiveItem() {
//        return ItemAPI.verify(entity.getUseItem());
//    }

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

    //Want to rework getStatusEffects().

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

}
