package org.moon.figura.testing;

import org.moon.figura.lua.LuaObject;
import org.moon.figura.lua.LuaWhitelist;

import java.util.HashMap;

public class TestObject extends LuaObject {

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

}
