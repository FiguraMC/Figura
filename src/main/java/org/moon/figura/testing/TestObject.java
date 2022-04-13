package org.moon.figura.testing;

import org.moon.figura.lua.LuaObject;
import org.moon.figura.lua.LuaWhitelist;
import org.terasology.jnlua.LuaState;

public class TestObject extends LuaObject {

    public int e = 621;

    @Override
    protected String getRegistryKey() {
        return "TEST_REGISTRY_KEY";
    }

    @Override
    protected void write(LuaState state) {
        putInteger(state, "e", e);
    }

    @Override
    protected void read(LuaState state, int index) {
        e = (int) readInteger(state, index, "e");
    }

    //Methods which can be called on TestObject parameters
    //First argument always has to be a TestObject, since we
    //Want to use colon notation.

    @LuaWhitelist
    public static void printHi(TestObject obj) {
        System.out.println("hi!");
    }

    @LuaWhitelist
    public static int getFive(TestObject object) {
        return 5;
    }

    @LuaWhitelist
    public static int getVal(TestObject object) {
        return object.e;
    }

}