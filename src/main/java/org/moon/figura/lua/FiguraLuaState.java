package org.moon.figura.lua;

import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.api.EventsAPI;
import org.moon.figura.lua.api.ParticleAPI;
import org.moon.figura.lua.api.SoundAPI;
import org.moon.figura.lua.api.entity.PlayerEntityWrapper;
import org.moon.figura.lua.api.math.MatricesAPI;
import org.moon.figura.lua.api.math.VectorsAPI;
import org.moon.figura.lua.api.model.VanillaModelAPI;
import org.moon.figura.lua.api.nameplate.NameplateAPI;
import org.moon.figura.lua.api.world.WorldAPI;
import org.terasology.jnlua.JavaFunction;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.LuaState53;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class FiguraLuaState extends LuaState53 {

    private static String sandboxerScript;

    private final Avatar owner;

    //API References
    public EventsAPI events;
    public VanillaModelAPI vanillaModel;
    public NameplateAPI nameplate;

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

    public boolean init(Map<String, String> scripts, String mainScript) {
        if (scripts.size() == 0)
            return false;

        boolean failure;

        if (scripts.size() == 1) {
            Map.Entry<String, String> entry = scripts.entrySet().iterator().next();
            failure = runScript(entry.getValue(), entry.getKey());
        } else {
            if (!scripts.containsKey(mainScript)) {
                FiguraMod.LOGGER.error("Failed to load scripts, no script with name \"" + mainScript + ".lua\"");
                return false;
            }
            pushJavaFunction(requireFunc(scripts));
            setGlobal("require");
            failure = runScript(scripts.get(mainScript), mainScript);
        }

        return !failure;
    }

    public boolean runScript(String script, String name) {
        load(script, name);
        try {
            call(0, 0);
            return false;
        } catch (Exception e) {
            FiguraLuaPrinter.sendLuaError(e, owner.name);
            owner.scriptError = true;
        }
        return true;
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
        return luaState -> {
            String scriptName = luaState.checkString(1);
            if (scriptName.endsWith(".lua")) scriptName = scriptName.substring(0, scriptName.length() - 4);

            if (scripts.containsKey(scriptName)) {
                String src = scripts.get(scriptName);
                scripts.remove(scriptName);
                luaState.load(src, scriptName);
                luaState.call(0, MULTRET);
                return Math.min(luaState.getTop(), 1); //not sure if correct
            } else {
                throw new LuaRuntimeException("Failed to require " + scriptName + ". " +
                        "Either this file doesn't exist, or you've already required it before.");
            }
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
