package org.figuramc.figura.lua;

import net.minecraft.nbt.*;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.HashMap;
import java.util.function.Function;

public class NbtToLua {

    private static final HashMap<Class<?>, Function<Tag, LuaValue>> CONVERTERS = new HashMap<>() {{
        // primitive types
        put(ByteTag.class, tag -> LuaValue.valueOf(((ByteTag) tag).getAsByte()));
        put(ShortTag.class, tag -> LuaValue.valueOf(((ShortTag) tag).getAsShort()));
        put(IntTag.class, tag -> LuaValue.valueOf(((IntTag) tag).getAsInt()));
        put(LongTag.class, tag -> LuaValue.valueOf(((LongTag) tag).getAsLong()));
        put(FloatTag.class, tag -> LuaValue.valueOf(((FloatTag) tag).getAsFloat()));
        put(DoubleTag.class, tag -> LuaValue.valueOf(((DoubleTag) tag).getAsDouble()));

        // compound special :D
        put(CompoundTag.class, tag -> {
            LuaTable table = new LuaTable();
            CompoundTag compound = (CompoundTag) tag;

            for (String key : compound.getAllKeys())
                table.set(key, convert(compound.get(key)));

            return table;
        });

        // collection types
        put(ByteArrayTag.class, tag -> fromCollection((CollectionTag<?>) tag));
        put(IntArrayTag.class, tag -> fromCollection((CollectionTag<?>) tag));
        put(LongArrayTag.class, tag -> fromCollection((CollectionTag<?>) tag));
        put(ListTag.class, tag -> fromCollection((CollectionTag<?>) tag));
    }};

    private static LuaValue fromCollection(CollectionTag<?> tag) {
        LuaTable table = new LuaTable();

        int i = 1;
        for (Tag children : tag) {
            table.set(i, convert(children));
            i++;
        }

        return table;
    }

    public static LuaValue convert(Tag tag) {
        if (tag == null)
            return null;

        Class<?> clazz = tag.getClass();
        Function<Tag, LuaValue> builder = CONVERTERS.get(clazz);
        if (builder == null)
            return LuaValue.valueOf(tag.getAsString());

        return builder.apply(tag);
    }
}
