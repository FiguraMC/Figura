package org.figuramc.figura.lua.api.world;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.NbtToLua;
import org.figuramc.figura.lua.ReadOnlyLuaTable;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@LuaWhitelist
@LuaTypeDoc(
        name = "ItemStack",
        value = "itemstack"
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

    @LuaWhitelist
    @LuaFieldDoc("itemstack.id")
    public final String id;
    @LuaWhitelist
    @LuaFieldDoc("itemstack.tag")
    public final LuaTable tag;

    public ItemStackAPI(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.id = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
        this.tag = new ReadOnlyLuaTable(itemStack.getTag() != null ? NbtToLua.convert(itemStack.getTag()) : new LuaTable());
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.get_id")
    public String getID() {
        return id;
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.get_tag")
    public LuaTable getTag() {
        return tag;
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.get_count")
    public int getCount() {
        return itemStack.getCount();
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.get_damage")
    public int getDamage() {
        return itemStack.getDamageValue();
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.get_pop_time")
    public int getPopTime() {
        return itemStack.getPopTime();
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.has_glint")
    public boolean hasGlint() {
        return itemStack.hasFoil();
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.get_tags")
    public List<String> getTags() {
        List<String> list = new ArrayList<>();

        Registry<Item> registry = WorldAPI.getCurrentWorld().registryAccess().registryOrThrow(Registries.ITEM);
        Optional<ResourceKey<Item>> key = registry.getResourceKey(itemStack.getItem());

        if (key.isEmpty())
            return list;

        for (TagKey<Item> itemTagKey : registry.getHolderOrThrow(key.get()).tags().toList())
            list.add(itemTagKey.location().toString());

        return list;
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.is_block_item")
    public boolean isBlockItem() {
        return itemStack.getItem() instanceof BlockItem;
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.is_food")
    public boolean isFood() {
        return itemStack.isEdible();
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.get_use_action")
    public String getUseAction() {
        return itemStack.getUseAnimation().name();
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.get_name")
    public String getName() {
        return itemStack.getHoverName().getString();
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.get_max_count")
    public int getMaxCount() {
        return itemStack.getMaxStackSize();
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.get_rarity")
    public String getRarity() {
        return itemStack.getRarity().name();
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.is_enchantable")
    public boolean isEnchantable() {
        return itemStack.isEnchantable();
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.get_max_damage")
    public int getMaxDamage() {
        return itemStack.getMaxDamage();
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.is_damageable")
    public boolean isDamageable() {
        return itemStack.isDamageableItem();
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.is_stackable")
    public boolean isStackable() {
        return itemStack.isStackable();
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.get_repair_cost")
    public int getRepairCost() {
        return itemStack.getBaseRepairCost();
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.get_use_duration")
    public int getUseDuration() {
        return itemStack.getUseDuration();
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.to_stack_string")
    public String toStackString() {
        ItemStack stack = itemStack;
        String ret = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        CompoundTag nbt = stack.getTag();
        if (nbt != null)
            ret += nbt.toString();

        return ret;
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.is_armor")
    public boolean isArmor() {
        return itemStack.getItem() instanceof ArmorItem;
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.is_tool")
    public boolean isTool() {
        return itemStack.getItem() instanceof DiggerItem;
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.get_equipment_slot")
    public String getEquipmentSlot() {
        return LivingEntity.getEquipmentSlotForItem(itemStack).name();
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.copy")
    public ItemStackAPI copy() {
        return new ItemStackAPI(itemStack.copy());
    }

    @LuaWhitelist
    @LuaMethodDoc("itemstack.get_blockstate")
    public BlockStateAPI getBlockstate() {
        return itemStack.getItem() instanceof BlockItem blockItem ? new BlockStateAPI(blockItem.getBlock().defaultBlockState(), null) : null;
    }

    @LuaWhitelist
    public boolean __eq(ItemStackAPI other) {
        if (this == other)
            return true;

        ItemStack t = this.itemStack;
        ItemStack o = other.itemStack;
        if (t.getCount() != o.getCount())
            return false;
        if (!t.is(o.getItem()))
            return false;

        CompoundTag tag1 = t.getTag();
        CompoundTag tag2 = o.getTag();
        if (tag1 == null && tag2 != null)
            return false;

        return tag1 == null || tag1.equals(tag2);
    }

    @LuaWhitelist
    public Object __index(String arg) {
        if (arg == null) return null;
        return switch (arg) {
            case "id" -> id;
            case "tag" -> tag;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return id + " x" + getCount() + " (ItemStack)";
    }
}
