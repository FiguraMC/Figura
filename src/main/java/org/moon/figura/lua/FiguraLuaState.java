package org.moon.figura.lua;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.api.*;
import org.moon.figura.lua.api.entity.PlayerEntityWrapper;
import org.moon.figura.lua.api.math.MatricesAPI;
import org.moon.figura.lua.api.math.VectorsAPI;
import org.moon.figura.lua.api.model.VanillaModelAPI;
import org.moon.figura.lua.api.nameplate.NameplateAPI;
import org.moon.figura.lua.api.world.WorldAPI;
import org.terasology.jnlua.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FiguraLuaState extends LuaState53 {

    private static String sandboxerScript;

    private final Avatar owner;

    //API References
    public EventsAPI events;
    public VanillaModelAPI vanillaModel;
    public NameplateAPI nameplate;
    public MetaAPI meta;

    public FiguraLuaState(Avatar owner, int memory) {
        super(memory * 1_000_000); //memory is given in mb
        setJavaReflector(FiguraJavaReflector.INSTANCE);
        setConverter(FiguraConverter.INSTANCE);

        this.owner = owner;

        //Load the built-in figura libraries
        loadLibraries();

        //Loads print(), log(), and logTable() into the env.
        FiguraLuaPrinter.loadPrintFunctions(this);

        //GS easter egg :3
        getGlobal("_G");
        setGlobal("_GS");

        //Run the figura sandboxer script
        try {
            runSandboxer();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to load script sandboxer", e);
        }

        //Load debug.setHook to registry, used later for instruction caps
        loadSetHook();

        loadFiguraApis();
    }


    /**
     * Extensive documentation of this function, just because I don't
     * want to go insane debugging this I'm going to take it slowly.
     *
     * If there are no scripts: stop, return false to indicate failure.
     * If autoScripts is null: Run ALL scripts.
     * If autoScripts is non-null: Run only the scripts inside of autoScripts.
     * This means if autoScripts is empty, then no scripts will be run. You can run them
     * via the command /figura run, however, so this isn't a useless situation.
     * require() is thrown into the mix as well, just to make it extra spicy.
     */
    public boolean init(Map<String, String> scripts, ListTag autoScripts) {
        if (scripts.size() == 0)
            return false;

        pushJavaFunction(requireFunc(scripts));
        setGlobal("require");

        //Registry require() result cache table
        newTable();
        setField(REGISTRYINDEX, "requireResults");

        StringBuilder rootFunction = new StringBuilder();
        if (autoScripts == null) {
            for (String name : scripts.keySet())
                rootFunction.append("require(\"").append(name).append("\") ");
        } else {
            for (Tag scriptName : autoScripts)
                rootFunction.append("require(\"").append(scriptName.getAsString()).append("\") ");
        }
        return runScript(rootFunction.toString(), "autoScripts");
    }

    public boolean runScript(String script, String name) {
        load(script, name);
        try {
            call(0, 0);
            return true;
        } catch (Exception e) {
            FiguraLuaPrinter.sendLuaError(e, owner.name);
            owner.scriptError = true;
        }
        return false;
    }

    private void loadFiguraApis() {
        loadGlobal(VectorsAPI.INSTANCE, "vectors");
        loadGlobal(MatricesAPI.INSTANCE, "matrices");
        events = new EventsAPI();
        loadGlobal(events, "events");
        vanillaModel = new VanillaModelAPI();
        loadGlobal(vanillaModel, "vanilla_model");
        loadGlobal(WorldAPI.INSTANCE, "world");
        loadGlobal(new PlayerEntityWrapper(owner.owner), "player");
        loadGlobal(ParticleAPI.INSTANCE, "particle");
        loadGlobal(SoundAPI.INSTANCE, "sound");
        nameplate = new NameplateAPI();
        loadGlobal(nameplate, "nameplate");
        loadGlobal(ClientAPI.INSTANCE, "client");
        loadGlobal(new HostAPI(owner.owner), "host");
        meta = new MetaAPI(owner);
        loadGlobal(meta, "meta");

        //Load "vec" as global alias for "vectors.vec"
        pushJavaFunction(getJavaReflector().getMetamethod(JavaReflector.Metamethod.INDEX));
        getGlobal("vectors");
        pushString("vec");
        call(2, 1);
        setGlobal("vec");
    }

    private void loadSetHook() {
        //Open debug and push it
        openLib(Library.DEBUG);
        //Load sethook function
        getField(-1, "sethook");
        //Store the sethook function in the registry for safekeeping, and also pop the function
        setField(REGISTRYINDEX, "sethook");
        //Pop the debug table
        pop(1);
        //Remove debug from the environment
        loadGlobal(null, "debug");
    }

    public void setInstructionLimit(int limit) {
        getField(REGISTRYINDEX, "sethook");
        pushJavaFunction(INSTRUCTION_LIMIT_FUNCTION);
        pushString("");
        pushInteger(limit);
        call(3, 0);
    }

    public void loadGlobal(Object api, String name) {
        pushJavaObject(api);
        setGlobal(name);
    }

    private void loadLibraries() {
        openLib(Library.BASE);
        openLib(Library.TABLE);
        openLib(Library.STRING);
        openLib(Library.MATH);
        pop(4);
    }

    private void runSandboxer() throws IOException {
        if (sandboxerScript == null) {
            String path = "/assets/figura/lua/scripts/sandbox.lua";
            InputStream stream = FiguraMod.class.getResourceAsStream(path);
            if (stream == null)
                throw new IOException("Cannot locate sandbox.lua at " + path);
            sandboxerScript = new String(stream.readAllBytes());
        }
        load(sandboxerScript, "sandboxer");
        call(0, 0);
    }


    private static JavaFunction requireFunc(Map<String, String> scripts) {
        final Set<String> previouslyRun = new HashSet<>();
        return luaState -> {
            String scriptName = luaState.checkString(1);
            luaState.pop(1);
            if (scriptName.endsWith(".lua")) scriptName = scriptName.substring(0, scriptName.length() - 4);

            if (!scripts.containsKey(scriptName)) {
                if (!previouslyRun.contains(scriptName))
                    throw new LuaRuntimeException("Tried to require nonexistent script \"" + scriptName + "\"!");

                luaState.getField(luaState.REGISTRYINDEX, "requireResults");
                luaState.getField(-1, scriptName);
                luaState.remove(1);
                return 1;
            }

            String src = scripts.get(scriptName);
            scripts.remove(scriptName);
            previouslyRun.add(scriptName);
            luaState.load(src, scriptName);
            luaState.call(0, 1); //Stack has return value on it right now

            //If luaState didn't return anything, we want require() to return true
            if (luaState.type(1) == LuaType.NIL) {
                luaState.pop(1);
                luaState.pushBoolean(true);
            }

            //Stack labeled bottom to top
            luaState.getField(luaState.REGISTRYINDEX, "requireResults"); //Stack has return value, then requireResults
            luaState.pushValue(1); //Stack now has return value, requireResults, then return value again
            luaState.setField(-2, scriptName); //Stack now has return value, then requireResults
            luaState.pop(1); //Stack now has return value.

            return 1;
        };
    }

    private static final JavaFunction INSTRUCTION_LIMIT_FUNCTION = luaState -> {
        String error = "Script overran resource limits!";
        ((FiguraLuaState) luaState).setInstructionLimit(1);
        throw new LuaRuntimeException(error);
    };

    public Avatar getOwner() {
        return owner;
    }
}
