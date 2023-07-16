package org.figuramc.figura.ducks;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public abstract class SkullBlockRendererAccessor {

    private static ItemStack stack = null;
    private static Entity entity = null;
    private static SkullRenderMode renderMode = SkullRenderMode.OTHER;

    public static void setItem(ItemStack item) {
        stack = item;
    }

    public static ItemStack getItem() {
        return stack;
    }

    public static void setEntity(Entity e) {
        entity = e;
    }

    public static Entity getEntity() {
        return entity;
    }

    public static void setRenderMode(SkullRenderMode skullRenderMode) {
        renderMode = skullRenderMode;
    }

    public static SkullRenderMode getRenderMode() {
        return renderMode;
    }

    public enum SkullRenderMode {
        HEAD,
        FIRST_PERSON_LEFT_HAND,
        FIRST_PERSON_RIGHT_HAND,
        THIRD_PERSON_LEFT_HAND,
        THIRD_PERSON_RIGHT_HAND,
        BLOCK,
        OTHER
    }
}