package org.figuramc.figura.utils;

import com.google.gson.*;
import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.*;

import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.lua.NbtToLua;
import org.figuramc.figura.lua.ReadOnlyLuaTable;
import org.figuramc.figura.lua.api.json.FiguraJsonSerializer;
import org.figuramc.figura.lua.api.world.BlockStateAPI;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.math.vector.FiguraVector;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class LuaUtils {

    /**
     * This is a generic vector parsing function that also parses the arguments after the vectors, allowing vectors to be at the beginning of the function signature
     * @param methodName The name of the function that is calling this function. Used for readable errors.
     * @param vectorSizes The sizes of Vectors to parse. The number of vectors is determined by the size of the array.
     * @param defaultValues When a Vector or a Vector argument is nil, it will be filled in with the value in this array at the correct index.
     * @param expectedReturns An array of Classes for what the extra arguments are supposed to be. Used for readable errors.
     * @param args The arguments of the function, passed in as varargs. 
     * @return The new args list with multi-number-argument Vectors being returned as real Vectors.
     */
    public static Object[] parseVec(String methodName, int[] vectorSizes, double[] defaultValues, Class<?>[] expectedReturns, Object ...args) {
        ArrayList<Object> ret = new ArrayList<Object>(args.length);
        int i=0;
        for(int size : vectorSizes) {
            if (args[i] instanceof FiguraVector vec){
                if(vec.size()!=size)
                    throw new LuaError("Illegal argument at position " + (i + 1) + " to " + methodName + "(): Expected Vector" + size + ", recieved Vector" + vec.size());
                ret.add(vec);
                i += 1;
            }
            else if (args[i]==null || args[i] instanceof Number) {
                double[] vec = new double[size];
                for (int o=0;o<size;o++){
                    if (args[i+o] instanceof Number n)
                        vec[o]=n.doubleValue();
                    else if(args[i+o] == null)
                        vec[o]=defaultValues[o];
                    else
                        throw new LuaError("Illegal argument at position " + (i + o + 1) + " to " + methodName + "():" + 
                            " Expected Number, recieved " + args[i+o].getClass().getSimpleName() + " (" + args[i+o] + ")"
                        );
                }
                ret.add(
                    switch(size){
                        case 2->FiguraVec2.of(vec[0], vec[1]);
                        case 3->FiguraVec3.of(vec[0], vec[1], vec[2]);
                        case 4->FiguraVec4.of(vec[0], vec[1], vec[2], vec[3]);
                        default->throw new IllegalArgumentException("Illegal vector size: " + size);
                    }
                );
                i += size;
            }
            else if(args[i]==null) {
                ret.add(
                    switch(size){
                        case 2->FiguraVec2.of(defaultValues[0], defaultValues[1]);
                        case 3->FiguraVec3.of(defaultValues[0], defaultValues[1], defaultValues[2]);
                        case 4->FiguraVec4.of(defaultValues[0], defaultValues[1], defaultValues[2], defaultValues[3]);
                        default->throw new IllegalArgumentException("Illegal vector size: " + size);
                    }
                );
                i += 1;
            }
            else
                throw new LuaError("Illegal argument at position " + (i + 1) + " to " + methodName + "():" + 
                    " Expected Vector" + size + " or Number, recieved " + args[i].getClass().getSimpleName() + " (" + args[i] + ")"
                );
        }
        for(int o = i; o < args.length; o++) {
            if(args[o] != null && (o-i) < expectedReturns.length && !expectedReturns[o-i].isAssignableFrom(args[o].getClass()))
                throw new LuaError("Illegal argument at position " + (o + 1) + " to " + methodName + "():" + 
                    " Expected " + expectedReturns[o-i].getSimpleName() + ", recieved " + args[o].getClass().getSimpleName() + " (" + args[o] + ")"
                );
            ret.add(args[o]);
        }
        return ret.toArray();
    }

    public static Object[] parseVec(String methodName, int[] vectorSizes, Class<?>[] expectedReturns, Object ...args) {
        return parseVec(methodName, vectorSizes, new double[]{0,0,0,0}, expectedReturns, args);
    }

    public static FiguraVec2 parseVec2(String methodName, Object x, Number y) {
        return parseVec2(methodName, x, y, 0, 0);
    }

    public static FiguraVec2 parseVec2(String methodName, Object x, Number y, double defaultX, double defaultY) {
        if (x instanceof FiguraVec2 vec)
            return vec.copy();
        if (x == null || x instanceof Number) {
            if (x == null) x = defaultX;
            if (y == null) y = defaultY;
            return FiguraVec2.of(((Number) x).doubleValue(), y.doubleValue());
        }
        throw new LuaError("Illegal argument to " + methodName + "(): " + x.getClass().getSimpleName());
    }

    /**
     * This code gets repeated SO MUCH that I decided to put it in the utils class.
     * @param x Either the x coordinate of a vector, or a vector itself.
     * @param y The y coordinate of a vector, used if the first parameter was a number.
     * @param z The z coordinate of a vector, used if the first parameter was a number.
     * @return A FiguraVec3 representing the data passed in.
     */
    public static FiguraVec3 parseVec3(String methodName, Object x, Number y, Number z) {
        return parseVec3(methodName, x, y, z, 0, 0, 0);
    }

    public static FiguraVec3 parseVec3(String methodName, Object x, Number y, Number z, double defaultX, double defaultY, double defaultZ) {
        if (x instanceof FiguraVec3 vec)
            return vec.copy();
        if (x == null || x instanceof Number) {
            if (x == null) x = defaultX;
            if (y == null) y = defaultY;
            if (z == null) z = defaultZ;
            return FiguraVec3.of(((Number) x).doubleValue(), y.doubleValue(), z.doubleValue());
        }
        throw new LuaError("Illegal argument to " + methodName + "(): " + x.getClass().getSimpleName());
    }

    public static FiguraVec3 parseOneArgVec(String methodName, Object x, Number y, Number z, double defaultArg) {
        double d = x instanceof Number n ? n.doubleValue() : defaultArg;
        return parseVec3(methodName, x, y, z, d, d, d);
    }

    public static FiguraVec3 nullableVec3(String methodName, Object x, Number y, Number z) {
        return x == null ? null : parseVec3(methodName, x, y, z);
    }

    public static Pair<FiguraVec3, FiguraVec3> parse2Vec3(String methodName, Object x, Object y, Number z, Object w, Number t, Number h, int xIndex) {
        FiguraVec3 a, b;

        if (x instanceof FiguraVec3 vec1) {
            a = vec1.copy();
            if (y instanceof FiguraVec3 vec2) {
                b = vec2.copy();
            } else if (y == null || y instanceof Number) {
                if (w == null || w instanceof Number) {
                    b = parseVec3(methodName, y, z, (Number) w);
                } else {
                    throw new LuaError("Illegal argument at position" + xIndex+3 + "to " + methodName + "(): " + w);
                }
            } else {
                throw new LuaError("Illegal argument at position "+ xIndex+1 + " to " + methodName + "(): " + y);
            }
        } else if (x instanceof Number && y == null || y instanceof Number) {
            a = parseVec3(methodName, x, (Number) y, z);
            if (w instanceof FiguraVec3 vec1) {
                b = vec1.copy();
            } else if (w == null || w instanceof Number) {
                b = parseVec3(methodName, w, t, h);
            } else {
                throw new LuaError("Illegal argument at position "+ xIndex+3 + " to " + methodName + "(): " + w);
            }
        } else {
            throw new LuaError("Illegal argument at position "+ xIndex + " to " + methodName + "(): " + x);
        }

        return Pair.of(a, b);
    }

    // These functions allow having vector parsing at the beggining of the function, taking into account other arguments.
    public static Pair<FiguraVec3, Object[]> parseVec3(String methodName, Class<?>[] expectedReturns, Object ...args) {
        Object[] parsed = parseVec(methodName, new int[]{3}, expectedReturns, args);
        return Pair.of((FiguraVec3)parsed[0], Arrays.copyOfRange(parsed, 1, parsed.length));
    }

    public static Pair<Pair<FiguraVec3, FiguraVec3>, Object[]> parse2Vec3(String methodName, Class<?>[] expectedReturns, Object ...args) {
        Object[] parsed = parseVec(methodName, new int[]{3,3}, expectedReturns, args);
        return Pair.of(
            Pair.of((FiguraVec3)parsed[0], (FiguraVec3)parsed[1]), 
            Arrays.copyOfRange(parsed, 2, parsed.length)
        );
    }

    public static FiguraVec4 parseVec4(String methodName, Object x, Number y, Number z, Number w, double defaultX, double defaultY, double defaultZ, double defaultW) {
        if (x instanceof FiguraVec3 vec)
            return FiguraVec4.of(vec.x, vec.y, vec.z, defaultW);
        if (x instanceof FiguraVec4 vec)
            return vec.copy();
        if (x == null || x instanceof Number) {
            if (x == null) x = defaultX;
            if (y == null) y = defaultY;
            if (z == null) z = defaultZ;
            if (w == null) w = defaultW;
            return FiguraVec4.of(((Number) x).doubleValue(), y.doubleValue(), z.doubleValue(), w.doubleValue());
        }
        throw new LuaError("Illegal argument to " + methodName + "(): " + x.getClass().getSimpleName());
    }

    public static ItemStackAPI parseItemStackMap(String methodName, Object item) {
        if (item == null)
            return new ItemStackAPI(ItemStack.EMPTY);
        else if (item instanceof ItemStackAPI wrapper)
            return wrapper.copy();
        else if (item instanceof String string) {
            try {
                Level level = WorldAPI.getCurrentWorld();
                // Use the DFU only if necessary to convert item nbt -> components
                boolean oldLogic = string.contains("{") && string.contains("[") ? string.indexOf("{") < string.indexOf("[") && !string.contains("minecraft:attribute_modifiers"): string.contains("{");
                if (oldLogic) {
                    String tagStr = string.substring(string.indexOf("{"));
                    CompoundTag nbtItem = TagParser.parseCompoundFully(tagStr);
                    CompoundTag tag = new CompoundTag();
                    tag.putString("id", Identifier.read(new StringReader(string)).toString());
                    tag.putInt("Count", 1);
                    tag.put("tag", nbtItem);
                    Dynamic<Tag> ops = new Dynamic<>(NbtOps.INSTANCE, tag);
                    Optional<ItemStackComponentizationFix.ItemStackData> optionalItemStackData = ItemStackComponentizationFix.ItemStackData.read(ops);
                    if (optionalItemStackData.isPresent()) {
                        ItemStackComponentizationFix.ItemStackData data = optionalItemStackData.get();
                        ItemStackComponentizationFix.fixItemStack(data, data.tag);

                        ItemStack stack = parse(level.registryAccess(), data.write().cast(NbtOps.INSTANCE)).orElse(ItemArgument.item(CommandBuildContext.simple(level.registryAccess(), level.enabledFeatures())).parse(new StringReader(string)).createItemStack(1));
                        LuaTable table = new LuaTable();
                        for (String key : nbtItem.keySet())
                            table.set(key, NbtToLua.convert(nbtItem.get(key)));
                        CompoundTag itemTag = NbtToLua.convertToNbt(stack.getComponents());
                        for (String key : itemTag.keySet())
                            table.set(key, NbtToLua.convert(itemTag.get(key)));

                        table = new ReadOnlyLuaTable(table);
                        return new ItemStackAPI(stack, table);
                    }
                }
                return new ItemStackAPI(ItemArgument.item(CommandBuildContext.simple(level.registryAccess(), level.enabledFeatures())).parse(new StringReader(string)).createItemStack(1));
            } catch (Exception e) {
                throw new LuaError("Could not parse item stack from string: " + string);
            }
        }

        throw new LuaError("Illegal argument to " + methodName + "(): " + item);
    }

    public static Optional<ItemStack> parse(HolderLookup.Provider provider, Tag tag) {
        return ItemStack.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag)
                .resultOrPartial(string -> FiguraMod.LOGGER.error("Tried to load invalid item: '{}'", string));
    }

    public static void addLegacyNbtNames(LuaTable source, LuaTable dest) {
        if (!source.get("minecraft:damage").equals(LuaValue.NIL) ){
            dest.set("Damage", source.get("minecraft:damage"));
        }
        if (!source.get("minecraft:repair_cost").equals(LuaValue.NIL) ){
            dest.set("RepairCost", source.get("minecraft:repair_cost"));
        }
        if (!source.get("minecraft:custom_model_data").equals(LuaValue.NIL)) {
            dest.set("CustomModelData", source.get("minecraft:custom_model_data"));
        }
        if (!source.get("minecraft:block_state").equals(LuaValue.NIL)) {
            dest.set("BlockStateTag", source.get("minecraft:block_state"));
        }
        if (!source.get("minecraft:entity_data").equals(LuaValue.NIL)) {
            dest.set("EntityTag", source.get("minecraft:entity_data"));
        }
        if (!source.get("minecraft:block_entity_data").equals(LuaValue.NIL)) {
            dest.set("BlockEntityTag", source.get("minecraft:block_entity_data"));
        }
        if (!source.get("minecraft:enchantments").equals(LuaValue.NIL)) {
            dest.set("Enchantments", source.get("minecraft:enchantments"));
        }
        if (!source.get("minecraft:custom_name").equals(LuaValue.NIL)) {
            LuaTable tab = new LuaTable();
            if (!dest.get("display").equals(LuaValue.NIL))
                tab = (LuaTable) dest.get("display");
            tab.set("Name", source.get("minecraft:custom_name"));
            dest.set("display", tab);
        }
        if (!source.get("minecraft:lore").equals(LuaValue.NIL)) {
            LuaTable tab = new LuaTable();
            if (!dest.get("display").equals(LuaValue.NIL))
                tab = (LuaTable) dest.get("display");
            tab.set("Lore", source.get("minecraft:lore"));
            dest.set("display", tab);
        }
        if (!source.get("minecraft:dyed_color").equals(LuaValue.NIL)) {
            LuaTable tab = new LuaTable();
            if (!dest.get("display").equals(LuaValue.NIL))
                tab = (LuaTable) dest.get("display");
            tab.set("color", source.get("minecraft:dyed_color"));
            dest.set("display", tab);
        }
        if (!source.get("minecraft:map_color").equals(LuaValue.NIL)) {
            LuaTable tab = new LuaTable();
            if (!dest.get("display").equals(LuaValue.NIL))
                tab = (LuaTable) dest.get("display");
            tab.set("MapColor", source.get("minecraft:map_color"));
            dest.set("display", tab);
        }
        if (!source.get("minecraft:stored_enchantments").equals(LuaValue.NIL)) {
            dest.set("StoredEnchantments", source.get("minecraft:stored_enchantments"));
        }
        if (!source.get("minecraft:trim").equals(LuaValue.NIL)) {
            dest.set("Trim", source.get("minecraft:trim"));
        }
        if (!source.get("minecraft:charged_projectiles").equals(LuaValue.NIL)) {
            dest.set("ChargedProjectiles", source.get("minecraft:charged_projectiles"));
        }
        if (!source.get("minecraft:bundle_contents").equals(LuaValue.NIL)) {
            dest.set("Items", source.get("minecraft:bundle_contents"));
        }
        if (!source.get("minecraft:map_id").equals(LuaValue.NIL)) {
            dest.set("Map", source.get("minecraft:map_id"));
        }
        if (!source.get("minecraft:suspicious_stew_effects").equals(LuaValue.NIL)) {
            dest.set("effects", source.get("minecraft:suspicious_stew_effects"));
        }
        if (!source.get("minecraft:debug_stick_state").equals(LuaValue.NIL)) {
            dest.set("DebugProperty", source.get("minecraft:debug_stick_state"));
        }
        if (!source.get("minecraft:bucket_entity_data").equals(LuaValue.NIL)) {
            for (LuaValue key : ((LuaTable)source.get("minecraft:bucket_entity_data")).keys()) {
                dest.set(key, source.get("minecraft:bucket_entity_data").get(key));
            }
        }
        if (!source.get("minecraft:instrument").equals(LuaValue.NIL)) {
            dest.set("instrument", source.get("minecraft:instrument"));
        }
        if (!source.get("minecraft:recipes").equals(LuaValue.NIL)) {
            dest.set("recipes", source.get("minecraft:recipes"));
        }
        if (!source.get("minecraft:profile").equals(LuaValue.NIL)) {
            LuaTable tab = new LuaTable();
            if(!source.get("minecraft:profile").get("id").equals(LuaValue.NIL)) {
                tab.set("Id", source.get("minecraft:profile").get("id"));
            }
            if (!source.get("minecraft:profile").get("name").equals(LuaValue.NIL)) {
                tab.set("Name", source.get("minecraft:profile").get("name"));
            }
            if (!source.get("minecraft:profile").get("properties").equals(LuaValue.NIL)) {
                LuaTable properties = ((LuaTable)source.get("minecraft:profile").get("properties"));
                LuaTable property = new LuaTable();
                LuaTable textures = new LuaTable();
                for (LuaValue key : properties.keys()) {
                    LuaTable current = (LuaTable) properties.get(key);
                    LuaTable texture = new LuaTable();
                    if (!current.get("value").equals(LuaValue.NIL)) {
                        texture.set("Value", current.get("value"));
                    }
                    if (!current.get("signature").equals(LuaValue.NIL)) {
                        texture.set("Signature", current.get("signature"));
                    }
                    if (!current.get("name").equals(LuaValue.NIL)) {
                        texture.set("Name", current.get("name"));
                    }
                    textures.set(key, texture);
                }
                property.set("textures", textures);
                tab.set("Properties", property);
            }
            dest.set("SkullOwner", tab);
        }
    }

    public static ItemStack parseItemStack(String methodName, Object item) {
        return parseItemStackMap(methodName, item).itemStack;
    }

    public static BlockState parseBlockState(String methodName, Object block) {
        if (block == null)
            return Blocks.AIR.defaultBlockState();
        else if (block instanceof BlockStateAPI wrapper)
            return wrapper.blockState;
        else if (block instanceof String string) {
            try {
                Level level = WorldAPI.getCurrentWorld();
                return BlockStateArgument.block(CommandBuildContext.simple(level.registryAccess(), level.enabledFeatures())).parse(new StringReader(string)).getState();
            } catch (Exception e) {
                throw new LuaError("Could not parse block state from string: " + string);
            }
        }

        throw new LuaError("Illegal argument to " + methodName + "(): " + block);
    }

    public static Identifier parsePath(String path) {
        DataResult<Identifier> res = Identifier.read(path);
        return res.getOrThrow(s -> {
            throw new LuaError(s);
        });
    }

    public static Object[] parseBlockHitResult(HitResult hitResult) {
        if (hitResult instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            return new Object[]{new BlockStateAPI(WorldAPI.getCurrentWorld().getBlockState(pos), pos), FiguraVec3.fromVec3(blockHit.getLocation()), blockHit.getDirection().getName()};
        }
        return null;
    }

    // Converts an item stack into a valid stack string that can be turned back into an item with a command
    public static String getItemStackString(ItemStack stack) {
        StringBuilder builder = new StringBuilder();
        DynamicOps<Tag> dynamicOps = WorldAPI.getCurrentWorld().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        DataComponentMap map = stack.getComponents();
        Iterator<TypedDataComponent<?>> iterator = map.iterator();

        // the code is literally disgusting, loops through all components
        while (iterator.hasNext()) {
            // uses an iterator to avoid extra ,

            TypedDataComponent<?> typedDataComponent = iterator.next();
            Optional<Tag> optional = typedDataComponent.encodeValue(dynamicOps).result();
            Identifier resourceLocation = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(typedDataComponent.type());
            // apparently sometimes the game will not return a string, go figure
            String op = optional.map(tag -> tag.asString().orElse(tag.toString().isEmpty() ? "" : tag.toString())).orElse("");
            if (typedDataComponent.type() == DataComponents.ITEM_NAME && optional.isPresent() && op.contains("translate"))
                continue;

            if (optional.isPresent() && resourceLocation != null && !op.isEmpty()) {
                builder.append(resourceLocation).append("=");
                // minecraft gets super picky if you give it a resource location so this check has to be added, ew
                Identifier flag = Identifier.tryParse(op);
                if (optional.get().getType() == StringTag.TYPE && flag != null) {
                    builder.append("\"").append(op).append("\"");
                } else {
                    builder.append(op);
                }
                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
        }
        if (builder.isEmpty())
            return "";

        return "[" + builder + "]";
    }

    public static int parseSlot(Object slot, Inventory inventory) {
        if (slot instanceof String s) {
            try {
                return SlotArgument.slot().parse(new StringReader(s));
            } catch (Exception e) {
                throw new LuaError("Unable to get slot \"" + slot + "\"");
            }
        } else if (slot instanceof Integer i) {
            if (i == -1 && inventory != null) {
                return inventory.getFreeSlot();
            } else {
                return i;
            }
        } else {
            throw new LuaError("Invalid type for getSlot: " + slot.getClass().getSimpleName());
        }
    }

    public static JsonElement asJsonValue(LuaValue value) {
        if (value.isnil()) return JsonNull.INSTANCE;
        if (value.isboolean()) return new JsonPrimitive(value.checkboolean());
        if (value instanceof LuaString s) return new JsonPrimitive(s.checkjstring());
        if (value.isint()) return new JsonPrimitive(value.checkint());
        if (value.isnumber()) return new JsonPrimitive(value.checkdouble());
        if (value.istable()) {
            LuaTable table = value.checktable();

            // If it's an "array" (uses numbers as keys)
            if (checkTableArray(table) && table.length() > 0) {
                JsonArray arr = new JsonArray();
                LuaValue[] keys = table.keys();
                int arrayLength = keys[keys.length-1].checkint();
                for(int i = 1; i <= arrayLength; i++) {
                    arr.add(asJsonValue(table.get(i)));
                }
                return arr;
            }
            // Otherwise, if it's a proper key-value table
            else {
                JsonObject object = new JsonObject();
                for (LuaValue key : table.keys()) {
                    object.add(key.tojstring(), asJsonValue(table.get(key)));
                }
                return object;
            }
        }
        if (value.isuserdata() && value.checkuserdata() instanceof FiguraJsonSerializer.JsonValue val) {
            return val.getElement();
        }
        // Fallback for things that shouldn't be converted (like functions)
        return null;
    }

    public static boolean checkTableArray(LuaTable table) {
        for (LuaValue key : table.keys()) {
            if (!key.isnumber()) return false;
        }

        return true;
    }
}
