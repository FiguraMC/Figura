package org.moon.figura.lua;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.api.*;
import org.moon.figura.lua.api.entity.PlayerEntityWrapper;
import org.moon.figura.lua.api.keybind.KeybindAPI;
import org.moon.figura.lua.api.math.MatricesAPI;
import org.moon.figura.lua.api.math.VectorsAPI;
import org.moon.figura.lua.api.model.VanillaModelAPI;
import org.moon.figura.lua.api.nameplate.NameplateAPI;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.lua.types.LuaOwnedTable;
import org.moon.figura.utils.IOUtils;
import org.spongepowered.asm.util.Files;
import org.terasology.jnlua.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FiguraLuaState extends LuaState53 {

    private static String sandboxerScript;

    private final Avatar owner;

    //API References
    public EventsAPI events;
    public VanillaModelAPI vanillaModel;
    public PlayerEntityWrapper entity;
    public NameplateAPI nameplate;
    public MetaAPI meta;
    public KeybindAPI keybind;
    public RendererAPI renderer;

    public static final String STORAGE_KEY = "STORAGE";
    public LuaOwnedTable<Object> storedStuff = new LuaOwnedTable<>(this, STORAGE_KEY);

    public FiguraLuaState(Avatar owner, int memory) {
        super(memory * 1_000_000); //memory is given in mb
        setJavaReflector(FiguraJavaReflector.INSTANCE);
        setConverter(FiguraConverter.INSTANCE);

        this.owner = owner;

        //Load the built-in figura libraries
        loadLibraries();

        //Run the figura sandboxer script
        runSandboxer();

        //Load debug.setHook to registry, used later for instruction caps
        loadSetHook();

        //Loads print(), log(), and logTable() into the env.
        FiguraLuaPrinter.loadPrintFunctions(this);

        loadFiguraApis();

        // Run figura libraries provided by resource pack (NOTE: THIS IS DISABLED CURRENTLY! The system for doing this
        // isn't really finished, so for now it just has hardcoded Resource Scripts instead of using resource packs.
        //TODO: Lock this behind an option, so people have to opt-in to allowing servers to run code on startup
        //TODO: Also make this use trust settings for instruction limits
        try {
            // 2000 instructions is probably reasonable for server code on startup,
            // this should mainly be used for providing helper functions to users like our math script does.
            setInstructionLimit(2000);
            runResourceScripts();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            FiguraLuaPrinter.sendLuaError(e, owner.name, owner.owner);
            owner.scriptError = true;
        }
        return false;
    }

    private void loadFiguraApis() {
        loadGlobal(VectorsAPI.INSTANCE, "vectors");
        loadGlobal(MatricesAPI.INSTANCE, "matrices");
        loadGlobal(events = new EventsAPI(this), "events");
        loadGlobal(vanillaModel = new VanillaModelAPI(), "vanilla_model");
        loadGlobal(WorldAPI.INSTANCE, "world");
        loadGlobal(entity = new PlayerEntityWrapper(owner.owner), "player");
        loadGlobal(new ParticleAPI(owner), "particle");
        loadGlobal(new SoundAPI(owner), "sound");
        loadGlobal(nameplate = new NameplateAPI(), "nameplate");
        loadGlobal(ClientAPI.INSTANCE, "client");
        loadGlobal(new HostAPI(owner), "host");
        loadGlobal(meta = new MetaAPI(owner), "meta");
        loadGlobal(keybind = new KeybindAPI(owner), "keybind");
        loadGlobal(renderer = new RendererAPI(), "renderer");
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

    private void runSandboxer() {
        //Sandboxer is now hardcoded, don't want servers overriding it in resource packs
        String sandboxer = """
                -- yeet FileIO and gc globals
                dofile = nil
                loadfile = nil
                collectgarbage = nil

                -- GS easter egg
                _GS = _G""";

        load(sandboxer, "sandboxer");
        call(0, 0);
    }

    //Runs the lua files that add additional functions.
    private void runResourceScripts() throws IOException {
        String mathScript = """
                function math.clamp(val, min, max)
                    return math.min(math.max(val, min), max)
                end
                
                function math.lerp(a, b, t)
                    return a + (b - a) * t
                end
                
                function math.round(arg)
                    return math.floor(arg + 0.5)
                end
                
                vec = vectors.vec
                """;
        load(mathScript, "math");
        call(0, 0);

        //Code below is potentially a vulnerability, since servers can provide resource packs,
        //so if a server provides malicious code in a resource pack it would be run here. We're
        //disallowing that for now until we figura out how to stop this from being bad.
        //For now, the resource scripts (just math) are instead hardcoded.

//        Path scripts = FiguraMod.ASSETS_DIR.resolve("lua/scripts/init");
//        for (File file : IOUtils.getFilesByExtension(scripts, ".lua", false)) {
//            String src = IOUtils.readFile(file);
//            load(src, file.getName().substring(0, file.getName().length()-4));
//            call(0, 0);
//        }
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
