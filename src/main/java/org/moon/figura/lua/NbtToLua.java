package org.moon.figura.lua;

import net.minecraft.nbt.*;
import org.moon.figura.lua.types.LuaTable;

import java.util.HashMap;
import java.util.function.Function;

public class NbtToLua {

    private static final HashMap<Class<?>, Function<Tag, Object>> CONVERTERS = new HashMap<>() {{
        //primitive types
        put(ByteTag.class, tag -> ((ByteTag) tag).getAsByte());
        put(ShortTag.class, tag -> ((ShortTag) tag).getAsShort());
        put(IntTag.class, tag -> ((IntTag) tag).getAsInt());
        put(LongTag.class, tag -> ((LongTag) tag).getAsLong());
        put(FloatTag.class, tag -> ((FloatTag) tag).getAsFloat());
        put(DoubleTag.class, tag -> ((DoubleTag) tag).getAsDouble());

        //compound special :D
        put(CompoundTag.class, tag -> {
            LuaTable table = new LuaTable();
            CompoundTag compound = (CompoundTag) tag;

            for (String key : compound.getAllKeys())
                table.put(key, convert(compound.get(key)));

            return table;
        });

        //collection types
        put(ByteArrayTag.class, tag -> fromCollection((CollectionTag<?>) tag));
        put(IntArrayTag.class, tag -> fromCollection((CollectionTag<?>) tag));
        put(LongArrayTag.class, tag -> fromCollection((CollectionTag<?>) tag));
        put(ListTag.class, tag -> fromCollection((CollectionTag<?>) tag));
    }};

    private static Object fromCollection(CollectionTag<?> tag) {
        LuaTable table = new LuaTable();

        int i = 1;
        for (Tag children : tag) {
            table.put(i, convert(children));
            i++;
        }

        return table;
    }

    public static Object convert(Tag tag) {
        if (tag == null)
            return null;

        Class<?> getClass = tag.getClass();
        Function<Tag, Object> builder = CONVERTERS.get(getClass);
        if (builder == null)
            return tag.toString();

        return builder.apply(tag);
    }
}
