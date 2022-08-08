package org.moon.figura.lua;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.config.Config;
import org.moon.figura.lua.api.*;
import org.moon.figura.lua.api.entity.EntityAPI;
import org.moon.figura.lua.api.entity.PlayerAPI;
import org.moon.figura.lua.api.event.EventsAPI;
import org.moon.figura.lua.api.keybind.KeybindAPI;
import org.moon.figura.lua.api.nameplate.NameplateAPI;
import org.moon.figura.lua.api.ping.PingAPI;
import org.moon.figura.lua.api.vanilla_model.VanillaModelAPI;
import org.moon.figura.utils.FiguraResourceListener;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * One runtime per avatar
 */
public class FiguraLuaRuntime {

    //Global API instances
    //---------------------------------
    public Entity user;
    public EntityAPI<?> entityAPI;
    @Nullable public PlayerAPI playerAPI;
    public EventsAPI events;
    public VanillaModelAPI vanilla_model;
    public KeybindAPI keybind;
    public HostAPI host;
    public NameplateAPI nameplate;
    public RendererAPI renderer;
    public ActionWheelAPI action_wheel;
    public AvatarAPI avatar_meta;

    public PingAPI ping;

    //---------------------------------

    public final Avatar owner;
    private final Globals userGlobals;
    private final LuaValue setHookFunction;
    private final LuaTable requireResults = new LuaTable();
    public final LuaTypeManager typeManager = new LuaTypeManager();

    private static final ArrayList<String> RESOURCE_SCRIPTS = new ArrayList<>();
    public static final FiguraResourceListener SCRIPT_LISTENER = new FiguraResourceListener("resource_script", manager -> {
        RESOURCE_SCRIPTS.clear();
        manager.listResources("script/resources", resource -> resource.getNamespace().equals(FiguraMod.MOD_ID) && resource.getPath().endsWith(".lua")).forEach((location, resource) -> {
            try (InputStream stream = resource.open()) {
                RESOURCE_SCRIPTS.add(new String(stream.readAllBytes()));
                FiguraMod.LOGGER.debug("Loaded resource script \"" + location.toString() + "\"");
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to load resource pack script \"" + location.toString() + "\"", e);
            }
        });
    });

    public FiguraLuaRuntime(Avatar avatar) {
        owner = avatar;
        //Each user gets their own set of globals as well.
        userGlobals = new Globals();
        userGlobals.load(new JseBaseLib());
        userGlobals.load(new Bit32Lib());
        userGlobals.load(new TableLib());
        userGlobals.load(new StringLib());
        userGlobals.load(new JseMathLib());

        LuaC.install(userGlobals);

        userGlobals.load(new DebugLib());
        setHookFunction = userGlobals.get("debug").get("sethook");
        userGlobals.set("debug", LuaValue.NIL);

        setupFiguraSandbox();

        FiguraAPIManager.setupTypesAndAPIs(this);

        loadExtraLibraries();

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
            userGlobals.set(name, typeManager.javaToLua(obj));
    }

    public void setUser(Entity user) {
        this.user = user;
        entityAPI = EntityAPI.wrap(user);
        if (entityAPI instanceof PlayerAPI playerAPI)
            this.playerAPI = playerAPI;
        setGlobal("user", entityAPI);
        userGlobals.set("player", userGlobals.get("user"));
    }

    private void setupFiguraSandbox() {
        try (InputStream inputStream = FiguraMod.class.getResourceAsStream("/assets/" + FiguraMod.MOD_ID + "/script/sandbox.lua")) {
            if (inputStream == null) throw new IOException("Failed to load sandbox.lua");
            runScript(new String(inputStream.readAllBytes()), "figura_sandbox");
        } catch (Exception e) {
            FiguraLuaPrinter.sendLuaError(e, owner.name, owner.owner);
        }
    }

    private void loadExtraLibraries() {
        //load print functions
        FiguraLuaPrinter.loadPrintFunctions(this);

        //load math library
        try (InputStream inputStream = FiguraMod.class.getResourceAsStream("/assets/" + FiguraMod.MOD_ID + "/script/math.lua")) {
            if (inputStream == null) throw new IOException("Failed to load math.lua");
            runScript(new String(inputStream.readAllBytes()), "math");
        } catch (Exception e) {
            FiguraLuaPrinter.sendLuaError(e, owner.name, owner.owner);
        }

        //load server scripts
        if (FiguraMod.isLocal(owner.owner) && !(boolean) Config.SERVER_SCRIPT.value)
            return;

        for (int i = 0; i < RESOURCE_SCRIPTS.size(); i++)
            runScript(RESOURCE_SCRIPTS.get(i), "resources" + i);
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
        LuaValue chunk = userGlobals.load(script, name);
        try {
            return chunk.call();
        } catch (LuaError e) {
            FiguraLuaPrinter.sendLuaError(e, owner.name, owner.owner);
            owner.scriptError = true;
            owner.luaRuntime = null;
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
                    //throw new LuaError("Error running required script " + scriptName);
                    return FALSE;
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
            //TODO: translation key for this // cant unless if we do it in a cursed way
            FiguraLuaPrinter.sendLuaError(new LuaError("Script overran resource limits!"), owner.name, owner.owner);
            return LuaValue.NIL;
        }
    };

    public void setInstructionLimit(int limit) {
        userGlobals.running.state.bytecodes = 0;
        setHookFunction.invoke(LuaValue.varargsOf(onReachedLimit, LuaValue.EMPTYSTRING, LuaValue.valueOf(Math.max(limit, 1))));
    }

    public int getInstructions() {
        return userGlobals.running.state.bytecodes;
    }
}
