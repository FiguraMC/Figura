package org.moon.figura.newlua;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.FiguraLuaPrinter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * One runtime per avatar
 */
public class FiguraLuaRuntime {

    private final Avatar owner;

    private final Globals userGlobals;
    private final LuaValue setHookFunction;
    private final LuaTable requireResults = new LuaTable();
    private final LuaTypeManager typeManager = new LuaTypeManager();

    public FiguraLuaRuntime(Avatar avatar) {
        owner = avatar;
        //Each user gets their own set of globals as well.
        userGlobals = new Globals();
        userGlobals.load(new JseBaseLib());
        userGlobals.load(new Bit32Lib());
        userGlobals.load(new TableLib());
        userGlobals.load(new StringLib());
        userGlobals.load(new JseMathLib());

        userGlobals.load(new DebugLib());
        setHookFunction = userGlobals.get("debug").get("sethook");
        userGlobals.set("debug", LuaValue.NIL);

        FiguraAPIManager.setupTypesAndAPIs(this);

        LuaTable figuraMetatables = new LuaTable();
        typeManager.dumpMetatables(figuraMetatables);
        setGlobal("figuraMetatables", figuraMetatables);
    }

    public void registerClass(Class<?> clazz) {
        typeManager.generateMetatableFor(clazz);
    }

    public void setGlobal(String name, Object obj) {
        if (obj instanceof LuaValue val)
            userGlobals.set(name, val);
        else
            userGlobals.set(name, typeManager.wrap(obj));
    }


    /**
     * Writing documentation of this function, just because I don't
     * want to go insane debugging this I'm going to take it slowly.
     *
     * If there are no scripts: stop, return false to indicate failure.
     * If autoScripts is null: Run ALL scripts.
     * If autoScripts is non-null: Run only the scripts inside of autoScripts.
     * This means if autoScripts is empty, then no scripts will be run. You can run them
     * via the command /figura run, however, so this isn't a useless situation.
     * require() is thrown into the mix as well, just to make it extra spicy.
     *
     * The method works by creating a new script, which just require()s everything in
     * autoScripts, then running this new script.
     */
    public boolean init(Map<String, String> scripts, ListTag autoScripts) {
        if (scripts.size() == 0)
            return false;

        userGlobals.set("require", getRequireFor(scripts));

        StringBuilder rootFunction = new StringBuilder();
        if (autoScripts == null) {
            for (String name : scripts.keySet())
                rootFunction.append("require(\"").append(name).append("\") ");
        } else {
            for (Tag scriptName : autoScripts)
                rootFunction.append("require(\"").append(scriptName.getAsString()).append("\") ");
        }
        return runScript(rootFunction.toString(), "autoScripts") != null;
    }

    //In the case of an error, this will return null.
    //If there is no error, it returns the LuaValue that the script does.
    public LuaValue runScript(String script, String name) {
        LuaValue chunk = FiguraAPIManager.MOD_WIDE_GLOBALS.load(script, name);
        try {
            return chunk.call();
        } catch (LuaError e) {
            FiguraLuaPrinter.sendLuaError(e, owner.name, owner.owner);
            owner.scriptError = true;
        }
        return null;
    }

    /**
     * Generates a require() function that can work with the given set of scripts.
     * @param scripts A map from string paths to scripts -> source code of those scripts
     * @return The require() function itself.
     */
    private OneArgFunction getRequireFor(Map<String, String> scripts) {
        return new OneArgFunction() {
            private final Set<String> previouslyRun = new HashSet<>();
            @Override
            public LuaValue call(LuaValue arg) {
                String scriptName = arg.checkjstring();

                if (scriptName.endsWith(".lua")) scriptName = scriptName.substring(0, scriptName.length() - 4);

                if (!scripts.containsKey(scriptName)) {
                    if (!previouslyRun.contains(scriptName))
                        //TODO: translation key
                        throw new LuaError("Tried to require nonexistent script \"" + scriptName + "\"!");
                    return requireResults.get(scriptName);
                }

                String src = scripts.get(scriptName);
                scripts.remove(scriptName);
                previouslyRun.add(scriptName);

                LuaValue retVal = runScript(src, scriptName);
                if (retVal == null) {
                    throw new LuaError("Error running required script " + scriptName);
                }

                //If luaState didn't return anything, we want the function to return true
                if (retVal == LuaValue.NIL)
                    retVal = LuaValue.TRUE;

                requireResults.set(scriptName, retVal);
                return retVal;
            }
        };
    }


    private final ZeroArgFunction onReachedLimit = new ZeroArgFunction() {
        @Override
        public LuaValue call() {
            //TODO: translation key for this
            FiguraLuaPrinter.sendLuaError(new LuaError("Script overran resource limits!"), owner.name, owner.owner);
            return LuaValue.NIL;
        }
    };

    public void setInstructionLimit(int limit) {
        userGlobals.running.state.bytecodes = 0;
        setHookFunction.invoke(LuaValue.varargsOf(onReachedLimit, LuaValue.EMPTYSTRING, LuaValue.valueOf(Math.max(limit, 1))));
    }




}
