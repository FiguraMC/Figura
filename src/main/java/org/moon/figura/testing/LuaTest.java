package org.moon.figura.testing;

import org.moon.figura.lua.FiguraLuaState;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaState;

import java.util.Map;

public class LuaTest {

    public static void test() {
        LuaUtils.setupNativesForLua();

        FiguraLuaState luaState = new FiguraLuaState("Test", 1000);
        luaState.openLib(LuaState.Library.BASE);
        luaState.openLib(LuaState.Library.TABLE);
        luaState.openLib(LuaState.Library.STRING);
        luaState.openLib(LuaState.Library.MATH);
        luaState.pop(4); //Pop the four libraries we just put on there

        luaState.pushJavaObject(FiguraVec4.of(1, 2, 3, 1));
        luaState.setGlobal("vec");
        luaState.pushJavaObject(FiguraMat4.of());
        luaState.setGlobal("mat");

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
                "println(mat);" +
                "println(vec);" +
                "println(mat * vec);" +
                "mat.v11 = 2;" +
                "mat.v24 = 3" +
                "println(mat:getInverse())" +
                "print(\"the test\")" +
                "print(mat)";

        luaState.load(testCode, "main");
        try {
            luaState.call(0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
