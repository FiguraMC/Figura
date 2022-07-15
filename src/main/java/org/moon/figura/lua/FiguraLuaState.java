package org.moon.figura.lua;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.moon.figura.FiguraMod;
import org.moon.figura.animation.Animation;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.config.Config;
import org.moon.figura.lua.api.*;
import org.moon.figura.lua.api.entity.PlayerEntityWrapper;
import org.moon.figura.lua.api.keybind.KeybindAPI;
import org.moon.figura.lua.api.math.MatricesAPI;
import org.moon.figura.lua.api.math.VectorsAPI;
import org.moon.figura.lua.api.model.VanillaModelAPI;
import org.moon.figura.lua.api.nameplate.NameplateAPI;
import org.moon.figura.lua.api.SoundAPI;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.lua.types.LuaOwnedTable;
import org.moon.figura.utils.FiguraResourceListener;
import org.terasology.jnlua.JavaFunction;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.LuaState53;
import org.terasology.jnlua.LuaType;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FiguraLuaState extends LuaState53 {

    private static final ArrayList<String> RESOURCE_SCRIPTS = new ArrayList<>();
    public static final FiguraResourceListener SCRIPT_LISTENER = new FiguraResourceListener("resource_script", manager -> {
        RESOURCE_SCRIPTS.clear();

        manager.listResources("lua/scripts/server", resource -> resource.getNamespace().equals(FiguraMod.MOD_ID) && resource.getPath().endsWith(".lua")).forEach((location, resource) -> {
            try (InputStream stream = resource.open()) {
                RESOURCE_SCRIPTS.add(new String(stream.readAllBytes()));
                FiguraMod.LOGGER.debug("Loaded resource script \"" + location.toString() + "\"");
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to load resource pack script \"" + location.toString() + "\"", e);
            }
        });
    });

    private final Avatar owner;

    //API References
    public EventsAPI events;
    public VanillaModelAPI vanillaModel;
    public PlayerEntityWrapper entity;
    public NameplateAPI nameplate;
    public HostAPI host;
    public MetaAPI meta;
    public KeybindAPI keybind;
    public RendererAPI renderer;
    public ActionWheelAPI actionWheel;

    public static final String STORAGE_KEY = "STORAGE";
    public LuaOwnedTable<Object> storedStuff = new LuaOwnedTable<>(this, STORAGE_KEY);

    public FiguraLuaState(Avatar owner, int memory) {
        super(memory * 1_000_000); //memory is given in mb
        setJavaReflector(FiguraJavaReflector.INSTANCE);
        setConverter(FiguraConverter.INSTANCE);

        this.owner = owner;

        //Load the standard lua libraries
        loadStandardLibraries();

        //Run the sandboxer script
        try {
            runSandboxer();
        } catch (Exception e) {
            FiguraLuaPrinter.sendLuaError(e, owner.name, owner.owner);
            owner.scriptError = true;
            return;
        }

        //Load debug.setHook to registry, used later for instruction caps
        loadSetHook();

        //load the loadString function
        loadLoadStringFunction();

        //Loads print(), log(), and logTable() into the env.
        FiguraLuaPrinter.loadPrintFunctions(this);

        loadFiguraApis();

        loadExternalLibraries();

        // Run libraries provided by resource packs
        // 2048 instructions is probably reasonable for server code on startup,
        // this should mainly be used for providing helper functions to users like our math script does.
        setInstructionLimit(2048);
        runResourceLibraries();
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
        loadGlobal(host = new HostAPI(owner), "host");
        loadGlobal(meta = new MetaAPI(owner), "meta");
        loadGlobal(keybind = new KeybindAPI(owner), "keybind");
        loadGlobal(renderer = new RendererAPI(owner.owner), "renderer");
        loadGlobal(actionWheel = new ActionWheelAPI(owner.owner), "action_wheel");
        loadGlobal(Animation.getTableForAnimations(owner), "animation");
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

    public void loadLoadStringFunction() {
        pushJavaFunction(LOADSTRING_FUNCTION);
        pushValue(-1);
        setGlobal("load");
        setGlobal("loadstring");
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

    private void loadStandardLibraries() {
        openLib(Library.BASE);
        openLib(Library.TABLE);
        openLib(Library.STRING);
        openLib(Library.MATH);
        pop(4);
    }

    private void runSandboxer() throws IOException {
        InputStream inputStream = FiguraMod.class.getResourceAsStream("/assets/" + FiguraMod.MOD_ID + "/lua/scripts/sandbox.lua");
        if (inputStream == null) throw new IOException("Failed to load sandbox.lua");
        load(new String(inputStream.readAllBytes()), "sandboxer");
        call(0, 0);
    }

    private void loadExternalLibraries() {
        try {
            InputStream inputStream = FiguraMod.class.getResourceAsStream("/assets/" + FiguraMod.MOD_ID + "/lua/scripts/math.lua");
            if (inputStream == null) throw new Exception();
            load(new String(inputStream.readAllBytes()), "math");
            call(0, 0);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to load " + FiguraMod.MOD_ID + " math library", e);
        }
    }

    private void runResourceLibraries() {
        if (FiguraMod.isLocal(owner.owner) && !(boolean) Config.SERVER_SCRIPT.value)
            return;

        for (int i = 0; i < RESOURCE_SCRIPTS.size(); i++) {
            load(RESOURCE_SCRIPTS.get(i), "resources" + i);
            call(0, 0);
        }
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

    private static final JavaFunction LOADSTRING_FUNCTION = luaState -> {
        //fix stack args
        if (luaState.getTop() < 1)
            luaState.pushNil();

        String string = luaState.checkString(1);
        luaState.pop(1);

        try {
            //load string
            luaState.load(string, "loadstring");

            //call string and add return to the stack
            luaState.call(0, 0);
            luaState.pushNil(); //make sure it returns nil
        } catch (Exception e) {
            luaState.pushString(e.getMessage());
        }

        return 1;
    };

    public Avatar getOwner() {
        return owner;
    }
}
