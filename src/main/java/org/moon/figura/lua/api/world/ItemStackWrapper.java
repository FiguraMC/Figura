package org.moon.figura.lua.api.world;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.NbtToLua;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaTable;
import org.terasology.jnlua.LuaRuntimeException;

import java.lang.ref.WeakReference;
import java.util.Optional;

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
    @LuaWhitelist
    @LuaFieldDoc(description = "itemstack.tag")
    public final LuaTable tag;

    private ItemStackWrapper(ItemStack wrapped) {
        this.itemStack = new WeakReference<>(wrapped);
        this.id = Registry.ITEM.getKey(wrapped.getItem()).toString();
        this.tag = (LuaTable) NbtToLua.convert(wrapped.getTag() != null ? wrapped.getTag() : null);
    }

    public static ItemStack getStack(ItemStackWrapper itemStack) {
        if (!exists(itemStack)) throw new LuaRuntimeException("ItemStack does not exist!");
        return itemStack.itemStack.get();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.exists"
    )
    public static boolean exists(@LuaNotNil ItemStackWrapper itemStack) {
        return itemStack.itemStack.get() != null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.get_count"
    )
    public static Integer getCount(@LuaNotNil ItemStackWrapper itemStack) {
        return getStack(itemStack).getCount();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.get_damage"
    )
    public static Integer getDamage(@LuaNotNil ItemStackWrapper itemStack) {
        return getStack(itemStack).getDamageValue();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.get_cooldown"
    )
    public static Integer getCooldown(@LuaNotNil ItemStackWrapper itemStack) {
        return getStack(itemStack).getPopTime();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.has_glint"
    )
    public static boolean hasGlint(@LuaNotNil ItemStackWrapper itemStack) {
        return getStack(itemStack).hasFoil();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.get_tags"
    )
    public static LuaTable getTags(@LuaNotNil ItemStackWrapper itemStack) {
        LuaTable table = new LuaTable();

        Registry<Item> registry = WorldAPI.getCurrentWorld().registryAccess().registryOrThrow(Registry.ITEM_REGISTRY);
        Optional<ResourceKey<Item>> key = registry.getResourceKey(getStack(itemStack).getItem());

        if (key.isEmpty())
            return table;

        int i = 1;
        for (TagKey<Item> itemTagKey : registry.getHolderOrThrow(key.get()).tags().toList()) {
            table.put(i, itemTagKey.location().toString());
            i++;
        }

        return table;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.is_block_item"
    )
    public static boolean isBlockItem(@LuaNotNil ItemStackWrapper itemStack) {
        return getStack(itemStack).getItem() instanceof BlockItem;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.is_food"
    )
    public static boolean isFood(@LuaNotNil ItemStackWrapper itemStack) {
        return getStack(itemStack).isEdible();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.get_use_action"
    )
    public static String getUseAction(@LuaNotNil ItemStackWrapper itemStack) {
        return getStack(itemStack).getUseAnimation().toString();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.get_name"
    )
    public static String getName(@LuaNotNil ItemStackWrapper itemStack) {
        return getStack(itemStack).getHoverName().getString();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.get_max_count"
    )
    public static Integer getMaxCount(@LuaNotNil ItemStackWrapper itemStack) {
        return getStack(itemStack).getMaxStackSize();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.get_rarity"
    )
    public static String getRarity(@LuaNotNil ItemStackWrapper itemStack) {
        return getStack(itemStack).getRarity().name();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.is_enchantable"
    )
    public static boolean isEnchantable(@LuaNotNil ItemStackWrapper itemStack) {
        return getStack(itemStack).isEnchantable();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.get_max_damage"
    )
    public static Integer getMaxDamage(@LuaNotNil ItemStackWrapper itemStack) {
        return getStack(itemStack).getMaxDamage();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.is_damageable"
    )
    public static boolean isDamageable(@LuaNotNil ItemStackWrapper itemStack) {
        return getStack(itemStack).isDamageableItem();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.is_stackable"
    )
    public static boolean isStackable(@LuaNotNil ItemStackWrapper itemStack) {
        return getStack(itemStack).isStackable();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.get_repair_cost"
    )
    public static Integer getRepairCost(@LuaNotNil ItemStackWrapper itemStack) {
        return getStack(itemStack).getBaseRepairCost();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.get_use_duration"
    )
    public static Integer getUseDuration(@LuaNotNil ItemStackWrapper itemStack) {
        return getStack(itemStack).getUseDuration();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ItemStackWrapper.class,
                    argumentNames = "itemStack"
            ),
            description = "itemstack.to_stack_string"
    )
    public static String toStackString(@LuaNotNil ItemStackWrapper itemStack) {
        ItemStack stack = getStack(itemStack);
        String ret = Registry.ITEM.getKey(stack.getItem()).toString();

        CompoundTag nbt = stack.getTag();
        if (nbt != null)
            ret += nbt.toString();

        return ret;
    }

    @Override
    public String toString() {
        return id + " (ItemStack)";
    }
}
