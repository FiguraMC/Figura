package org.moon.figura.lua.types;

import org.moon.figura.lua.LuaWhitelist;
import org.terasology.jnlua.JavaFunction;
import org.terasology.jnlua.JavaReflector;
import org.terasology.jnlua.LuaState;

import java.util.List;
import java.util.function.BiFunction;

@LuaWhitelist
public class LuaPairsIterator<T, I> implements JavaFunction {

    private final BiFunction<T, I, I> nextKeyProvider;
    private final Class<T> typeClass;
    private final Class<I> indexClass;

    public LuaPairsIterator(BiFunction<T, I, I> nextKey, Class<T> typeClass, Class<I> indexClass) {
        this.nextKeyProvider = nextKey;
        this.typeClass = typeClass;
        this.indexClass = indexClass;
    }

    public LuaPairsIterator(List<I> keys, Class<T> typeClass, Class<I> indexClass) {
        this((t, i) -> {
            if (i == null) return keys.get(0);
            int indexOfI = keys.indexOf(i);
            if (indexOfI < 0 || indexOfI == keys.size() - 1) return null;
            return keys.get(indexOfI+1);
        }, typeClass, indexClass);
    }

    @Override
    public int invoke(LuaState luaState) {
        T obj = luaState.toJavaObject(1, typeClass);
        I key = luaState.toJavaObject(2, indexClass);

        I nextKey = nextKeyProvider.apply(obj, key);

        luaState.pop(2);
        luaState.pushJavaFunction(luaState.getMetamethod(obj, JavaReflector.Metamethod.INDEX));
        luaState.pushJavaObject(obj);
        luaState.pushJavaObject(nextKey);
        luaState.call(2, 1);

        if (luaState.isNil(1))
            return 0;

        luaState.pushJavaObject(nextKey);
        luaState.insert(1);
        return 2;
    }
}
