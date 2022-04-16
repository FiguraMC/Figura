package org.moon.figura.testing;

import org.moon.figura.lua.LuaWhitelist;

import java.util.HashMap;

@LuaWhitelist
public class TestObject {

    @LuaWhitelist
    public double x = 5;

    public double y = 6;


    @LuaWhitelist
    public static void printThings(TestObject object) {
        System.out.println("Things!");
    }

    @LuaWhitelist
    public static void printX(TestObject object) {
        System.out.println(object.x);
    }

    @LuaWhitelist
    public static TestObject getNewObject(TestObject object) {
        TestObject result = new TestObject();
        result.x += 7;
        return result;
    }

    @LuaWhitelist
    public static HashMap getHashMap(TestObject object) {
        return new HashMap();
    }

    @LuaWhitelist
    public static void testVarArgs(TestObject object, Integer arg1, Integer arg2) {
        String result = "("+(arg1 == null ? "null" : arg1) + ", " + (arg2 == null ? "null" : arg2) + ")";
        System.out.println(result);
    }

}
