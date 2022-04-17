package org.moon.figura.testing;

import org.moon.figura.lua.FiguraLuaState;
import org.moon.figura.lua.LuaUtils;
import org.moon.figura.math.FiguraVec3;
import org.terasology.jnlua.LuaState;

import java.util.Map;

public class LuaTest {

    public static void test() {
        LuaUtils.setupNativesForLua();

        FiguraLuaState luaState = new FiguraLuaState();
        luaState.openLib(LuaState.Library.BASE);
        luaState.openLib(LuaState.Library.TABLE);
        luaState.openLib(LuaState.Library.STRING);
        luaState.openLib(LuaState.Library.MATH);
        luaState.pop(4); //Pop the four libraries we just put on there

        luaState.pushJavaObject(FiguraVec3.create());
        luaState.setGlobal("vec1");
        luaState.pushJavaObject(FiguraVec3.create());
        luaState.setGlobal("vec2");

        luaState.pushJavaFunction(state -> {
            if (state.isString(1)) {
                String v = state.toString(1);
                System.out.println(v);
            } else if (state.isNil(1)) {
                System.out.println("nil");
            } else if (state.isBoolean(1)) {
                System.out.println(state.toBoolean(1));
            } else if (state.isJavaObjectRaw(1)) {
                System.out.println(state.toJavaObject(1, Object.class));
            } else if (state.isTable(1)) {
                System.out.println(state.toJavaObject(1, Map.class));
            }
            return 0;
        });
        luaState.setGlobal("println");

        String testCode = "" +
                "vec1.x = 2; vec1.y = 3; vec1.z = 5;" +
                "vec2.x = 7; vec2.y = 11; vec2.z = 13;" +
                "println(vec1.xxyxzx);" +
                "println(vec2.yyxzxz)";

        luaState.load(testCode, "main");
        try {
            luaState.call(0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
