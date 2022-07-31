package org.moon.figura.lua.api.world;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.luaj.vm2.LuaTable;
import org.moon.figura.lua.LuaType;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.NbtToLua;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

import java.util.Optional;

@LuaType(typeName = "item_stack")
@LuaTypeDoc(
        name = "ItemStack",
        description = "item_stack"
)
public class ItemStackAPI {

    public final ItemStack itemStack;

    /**
     * Checks whether the given ItemStack is null, empty. If it is, returns air. If it isn't,
     * returns a new ItemStack for that item.
     * @param itemStack The ItemStack to check if it's a valid stack.
     * @return Null if the stack was invalid, or a wrapper for the stack if it was valid.
     */
    public static ItemStackAPI verify(ItemStack itemStack) {
        if (itemStack == null || itemStack == ItemStack.EMPTY)
            itemStack = Items.AIR.getDefaultInstance();
        return new ItemStackAPI(itemStack);
    }

    @LuaFieldDoc(description = "itemstack.id")
    public final String id;
    @LuaFieldDoc(description = "itemstack.tag")
    public final LuaTable tag;

    private ItemStackAPI(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.id = Registry.ITEM.getKey(itemStack.getItem()).toString();
        this.tag = (LuaTable) NbtToLua.convert(itemStack.getTag() != null ? itemStack.getTag() : null);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "itemstack.get_count")
    public Integer getCount() {
        return itemStack.getCount();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "itemstack.get_damage")
    public Integer getDamage() {
        return itemStack.getDamageValue();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "itemstack.get_cooldown")
    public Integer getCooldown() {
        return itemStack.getPopTime();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "itemstack.has_glint")
    public boolean hasGlint() {
        return itemStack.hasFoil();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "itemstack.get_tags")
    public LuaTable getTags() {
        LuaTable table = new LuaTable();

        Registry<Item> registry = WorldAPI.getCurrentWorld().registryAccess().registryOrThrow(Registry.ITEM_REGISTRY);
        Optional<ResourceKey<Item>> key = registry.getResourceKey(itemStack.getItem());

        if (key.isEmpty())
            return table;

        int i = 1;
        for (TagKey<Item> itemTagKey : registry.getHolderOrThrow(key.get()).tags().toList()) {
            table.set(i, itemTagKey.location().toString());
            i++;
        }

        return table;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "itemstack.is_block_item")
    public boolean isBlockItem() {
        return itemStack.getItem() instanceof BlockItem;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "itemstack.is_food")
    public boolean isFood() {
        return itemStack.isEdible();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "itemstack.get_use_action")
    public String getUseAction() {
        return itemStack.getUseAnimation().toString();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "itemstack.get_name")
    public String getName() {
        return itemStack.getHoverName().getString();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "itemstack.get_max_count")
    public Integer getMaxCount() {
        return itemStack.getMaxStackSize();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "itemstack.get_rarity")
    public String getRarity() {
        return itemStack.getRarity().name();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "itemstack.is_enchantable")
    public boolean isEnchantable() {
        return itemStack.isEnchantable();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "itemstack.get_max_damage")
    public Integer getMaxDamage() {
        return itemStack.getMaxDamage();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "itemstack.is_damageable")
    public boolean isDamageable() {
        return itemStack.isDamageableItem();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "itemstack.is_stackable")
    public boolean isStackable() {
        return itemStack.isStackable();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "itemstack.get_repair_cost")
    public Integer getRepairCost() {
        return itemStack.getBaseRepairCost();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "itemstack.get_use_duration")
    public Integer getUseDuration() {
        return itemStack.getUseDuration();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "itemstack.to_stack_string")
    public String toStackString() {
        ItemStack stack = itemStack;
        String ret = Registry.ITEM.getKey(stack.getItem()).toString();

        CompoundTag nbt = stack.getTag();
        if (nbt != null)
            ret += nbt.toString();

        return ret;
    }
}
