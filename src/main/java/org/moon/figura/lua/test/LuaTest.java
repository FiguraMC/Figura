package org.moon.figura.lua.test;

import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.moon.figura.math.vector.FiguraVec6;
import org.moon.figura.lua.LuaType;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.LuaTypeManager;

public class LuaTest {

    public static void main(String[] args) {

        Globals globals = new Globals();
        globals.load(new JseBaseLib());
        globals.load(new PackageLib());
        globals.load(new TableLib());
        globals.load(new StringLib());
        globals.load(new JseMathLib());

        LoadState.install(globals);
        LuaC.install(globals);

        LuaTypeManager typeManager = new LuaTypeManager();

        typeManager.generateMetatableFor(FiguraVec6.class);

        globals.set("vec", typeManager.wrap(FiguraVec6.of()));

        globals.set("print", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                System.out.println(arg.toString());
                return LuaValue.NIL;
            }
        });

        LuaTable tab = new LuaTable();
        typeManager.dumpMetatables(tab);
        globals.set("figuraMetatables", tab);

        LuaValue chunk = globals.load("""
                
                print("Hello")
                
                local a = vec
                local b = vec:copy()
                a:set(3.4, 3.6, 7.8, 8.999, 10.3, 0)
                b:set(1, 2, 3, 4, 5, 6)
                
                print(a:floor())
                            
                """, "main");

        long time = System.nanoTime();
        chunk.call();
        time = System.nanoTime() - time;
        System.out.println(time / 1000000 + " ms");

    }

    @LuaType(typeName = "test_class")
    public static class TestClass {
        @LuaWhitelist
        public static double add(MyFunClass fun) {
            return fun.a + fun.b;
        }

        @LuaWhitelist
        public static MyFunClass get(int a, double b) {
            MyFunClass result = new MyFunClass();
            result.a = a;
            result.b = b;
            return result;
        }

    }

    @LuaType(typeName = "my_fun_class")
    public static class MyFunClass {

        int a;
        double b;

        @LuaWhitelist
        public int getA() {
            return a;
        }

        @LuaWhitelist
        public double getB() {
            return b;
        }

        @LuaWhitelist
        public Object __index(String key) {
            if (key.equals("a")) return a;
            if (key.equals("b")) return b;
            return null;
        }

        @LuaWhitelist
        public void __newindex(String key, Object value) {
            if (key.equals("a")) a = ((Number) value).intValue();
            if (key.equals("b")) b = ((Number) value).doubleValue();
        }

        @LuaWhitelist
        public int __len() {
            return 2;
        }

        @LuaWhitelist
        public String toString() {
            return "(" + a + ", " + b + ")";
        }

    }

}
