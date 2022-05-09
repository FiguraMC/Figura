package org.moon.figura.lua.api.world;

import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
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

    @LuaWhitelist
    @LuaFieldDoc(description = "itemstack.id")
    public final String id;

    public ItemStackWrapper(ItemStack wrapped) {
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
