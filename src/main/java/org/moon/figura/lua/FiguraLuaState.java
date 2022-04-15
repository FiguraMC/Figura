package org.moon.figura.lua;

import org.terasology.jnlua.LuaState53;

public class FiguraLuaState extends LuaState53 {

    public FiguraLuaState() {
        super(10000000);
        setJavaReflector(FiguraJavaReflector.INSTANCE);
        setConverter(FiguraConverter.INSTANCE);
    }


}
