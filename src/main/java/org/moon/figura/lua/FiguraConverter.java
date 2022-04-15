package org.moon.figura.lua;

import org.moon.figura.FiguraMod;
import org.terasology.jnlua.Converter;
import org.terasology.jnlua.DefaultConverter;
import org.terasology.jnlua.LuaState;

import java.math.BigDecimal;
import java.math.BigInteger;

public class FiguraConverter implements Converter {

    public static final FiguraConverter INSTANCE = new FiguraConverter();
    private static final Converter DEFAULT = DefaultConverter.getInstance();

    @Override
    public int getTypeDistance(LuaState luaState, int index, Class<?> formalType) {
        return DEFAULT.getTypeDistance(luaState, index, formalType);
    }

    @Override
    public <T> T convertLuaValue(LuaState luaState, int index, Class<T> formalType) {
        return DEFAULT.convertLuaValue(luaState, index, formalType);
    }

    @Override
    public void convertJavaObject(LuaState luaState, Object object) {
        //Whitelisted types (NOT Object!)
        if (object instanceof Double
            || object instanceof LuaObject
            || object instanceof String
            || object instanceof Integer
            || object == null
            || object instanceof Float
            || object instanceof Boolean
            || object instanceof Long
            || object instanceof BigInteger
            || object instanceof Character
            || object instanceof BigDecimal
            || object instanceof Byte
            || object instanceof Short)
        {
            DEFAULT.convertJavaObject(luaState, object);
        } else {
            luaState.pushNil();
            FiguraMod.LOGGER.warn("Tried to push unsafe object of type " + object.getClass().getCanonicalName()
            + ", but was stopped by the converter!");
        }

    }
}
