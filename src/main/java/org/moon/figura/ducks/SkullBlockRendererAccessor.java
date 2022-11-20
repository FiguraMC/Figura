package org.moon.figura.ducks;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SkullBlockRendererAccessor {
    static ItemStack stack = null;

    public static void setReferenceItem(@Nullable ItemStack item) {
        stack = item;
    }
    public static ItemStack getReferenceItem() {
        return stack;
    }
}
