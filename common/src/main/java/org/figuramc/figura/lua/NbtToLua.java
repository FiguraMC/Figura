package org.figuramc.figura.lua;

import com.mojang.serialization.DynamicOps;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class NbtToLua {

    private static final HashMap<Class<?>, Function<Tag, LuaValue>> CONVERTERS = new HashMap<>() {{
        // primitive types
        put(ByteTag.class, tag -> LuaValue.valueOf(((ByteTag) tag).byteValue()));
        put(ShortTag.class, tag -> LuaValue.valueOf(((ShortTag) tag).shortValue()));
        put(IntTag.class, tag -> LuaValue.valueOf(((IntTag) tag).intValue()));
        put(LongTag.class, tag -> LuaValue.valueOf(((LongTag) tag).longValue()));
        put(FloatTag.class, tag -> LuaValue.valueOf(((FloatTag) tag).floatValue()));
        put(DoubleTag.class, tag -> LuaValue.valueOf(((DoubleTag) tag).doubleValue()));

        // compound special :D
        put(CompoundTag.class, tag -> {
            LuaTable table = new LuaTable();
            CompoundTag compound = (CompoundTag) tag;

            for (String key : compound.keySet())
                table.set(key, convert(compound.get(key)));

            return table;
        });

        // collection types
        put(ByteArrayTag.class, tag -> fromCollection((CollectionTag) tag));
        put(IntArrayTag.class, tag -> fromCollection((CollectionTag) tag));
        put(LongArrayTag.class, tag -> fromCollection((CollectionTag) tag));
        put(ListTag.class, tag -> fromCollection((CollectionTag) tag));
    }};

    private static LuaValue fromCollection(CollectionTag tag) {
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
            return LuaValue.valueOf(tag.asString().get());

        return builder.apply(tag);
    }

    public static CompoundTag convertToNbt(DataComponentMap components) {
        if (components == null)
            return null;

        CompoundTag tag = new CompoundTag();
        DynamicOps<Tag> dynamicOps = WorldAPI.getCurrentWorld().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        components.forEach(typedDataComponent -> {
            Optional<Tag> optional = typedDataComponent.encodeValue(dynamicOps).result();
            ResourceLocation resourceLocation = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(typedDataComponent.type());
            if (optional.isPresent() && resourceLocation != null){
                tag.put(resourceLocation.toString(), optional.get());
            }
        });

        return tag;
    }
}
