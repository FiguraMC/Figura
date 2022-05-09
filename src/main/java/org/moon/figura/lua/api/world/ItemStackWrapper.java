package org.moon.figura.lua.api.world;

import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

import java.lang.ref.WeakReference;

@LuaWhitelist
@LuaTypeDoc(
        name = "ItemStack",
        description = "itemstack"
)
public class ItemStackWrapper {

    private final WeakReference<ItemStack> itemStack;

    /**
     * Checks whether the given ItemStack is null, empty, or air. If it is, returns null. If it isn't,
     * returns a new ItemStack for that item.
     * @param itemStack The ItemStack to check if it's a valid stack.
     * @return Null if the stack was invalid, or a wrapper for the stack if it was valid.
     */
    public static ItemStackWrapper verify(ItemStack itemStack) {
        if (itemStack == null || itemStack == ItemStack.EMPTY || itemStack.getItem() == Items.AIR)
            return null;
        return new ItemStackWrapper(itemStack);
    }

    @LuaWhitelist
    @LuaFieldDoc(description = "itemstack.id")
    public final String id;

    private ItemStackWrapper(ItemStack wrapped) {
        itemStack = new WeakReference<>(wrapped);
        id = Registry.ITEM.getKey(wrapped.getItem()).toString();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.exists"
    )
    public static boolean exists(ItemStackWrapper itemStack) {
        return itemStack != null && itemStack.itemStack.get() != null;
    }

    @Override
    public String toString() {
        return id + " (ItemStack)";
    }
}
