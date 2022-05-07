package org.moon.figura.lua;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.config.Config;
import org.moon.figura.lua.api.EventsAPI;
import org.moon.figura.lua.api.entity.PlayerEntityWrapper;
import org.moon.figura.lua.api.math.MatricesAPI;
import org.moon.figura.lua.api.math.VectorsAPI;
import org.moon.figura.lua.api.model.VanillaModelAPI;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.lua.types.LuaTable;
import org.moon.figura.utils.ColorUtils.Colors;
import org.moon.figura.utils.TextUtils;
import org.terasology.jnlua.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class FiguraLuaState extends LuaState53 {

    private static String sandboxerScript;

    private final Avatar owner;

    //API References
    public EventsAPI events;
    public VanillaModelAPI vanillaModel;

    public FiguraLuaState(Avatar owner, int memory) {
        super(memory * 1_000_000); //memory is given in mb
        setJavaReflector(FiguraJavaReflector.INSTANCE);
        setConverter(FiguraConverter.INSTANCE);

        this.owner = owner;

        //Load the built-in figura libraries
        loadLibraries();

        //Loads print(), log(), and logTable() into the env.
        loadPrintFunctions();

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
            sendLuaError(e, owner.name);
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

    private void loadPrintFunctions() {
        pushJavaFunction(PRINT_FUNCTION);
        pushValue(-1);
        setGlobal("print");
        setGlobal("log");

        pushJavaFunction(PRINT_JSON_FUNCTION);
        pushValue(-1);
        setGlobal("printJson");
        setGlobal("logJson");

        pushJavaFunction(PRINT_TABLE_FUNCTION);
        pushValue(-1);
        setGlobal("printTable");
        setGlobal("logTable");
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
        throw new LuaRuntimeException(error);
    };

    // -- logging -- //

    //print a string either on chat or console
    public static void sendLuaMessage(Object message, String owner) {
        Component component = TextComponent.EMPTY.copy()
                .append(new TextComponent("[lua] ").withStyle(Colors.LUA_LOG.style))
                .append(new TextComponent(owner).withStyle(ChatFormatting.ITALIC))
                .append(new TextComponent(" : ").withStyle(Colors.LUA_LOG.style))
                .append(message instanceof Component c ? c : new TextComponent(message.toString()));

        if ((int) Config.LOG_LOCATION.value == 0)
            FiguraMod.sendChatMessage(component);
        else
            FiguraMod.LOGGER.info(component.getString());
    }

    //print an error, errors should always show up on chat
    public static void sendLuaError(Exception error, String owner) {
        //Jank as hell
        String message = error.toString().replace("org.terasology.jnlua.LuaRuntimeException: ", "");

        Component component = TextComponent.EMPTY.copy()
                .append(new TextComponent("[error] ").withStyle(Colors.LUA_ERROR.style))
                .append(new TextComponent(owner).withStyle(ChatFormatting.ITALIC))
                .append(new TextComponent(" : " + message).withStyle(Colors.LUA_ERROR.style));

        FiguraMod.sendChatMessage(component);
        FiguraMod.LOGGER.error("", error);
    }

    //print an ping!
    public static void sendPingMessage(String ping, int size, String owner) {
        int config = 0; //(int) Config.LOG_PINGS.value;

        //no ping? *megamind.png*
        if (config == 0)
            return;

        Component component = TextComponent.EMPTY.copy()
                .append(new TextComponent("[ping] ").withStyle(Colors.LUA_PING.style))
                .append(new TextComponent(owner).withStyle(ChatFormatting.ITALIC))
                .append(new TextComponent(" : ").withStyle(Colors.LUA_PING.style))
                .append(size + "b")
                .append(new TextComponent(" : ").withStyle(Colors.LUA_PING.style))
                .append(ping);

        if (config == 1)
            FiguraMod.sendChatMessage(component);
        else
            FiguraMod.LOGGER.info(component.getString());
    }

    //print functions
    private static final JavaFunction PRINT_FUNCTION = luaState -> {
        MutableComponent text = TextComponent.EMPTY.copy();

        //execute if the stack has entries
        while (luaState.getTop() > 0) {
            if (luaState.type(1) == LuaType.USERDATA)
                tryParseUserdata(luaState, 1);

            text.append(getPrintText(luaState, 1, true)).append("\t");
            luaState.remove(1);
        }

        //prints the value, either on chat or console
        sendLuaMessage(text, ((FiguraLuaState) luaState).owner.name);

        return 0;
    };

    private static final JavaFunction PRINT_JSON_FUNCTION = luaState -> {
        sendLuaMessage(TextUtils.tryParseJson(luaToString(luaState, 1)), ((FiguraLuaState) luaState).owner.name);
        return 0;
    };

    private static final JavaFunction PRINT_TABLE_FUNCTION = luaState -> {
        int depth = luaState.getTop() > 1 ? (int) luaState.checkInteger(2) : 1;
        Component result = tableToText(luaState, 1, depth, 1, true);
        sendLuaMessage(result, ((FiguraLuaState) luaState).owner.name);
        return 0;
    };

    private static Component tableToText(LuaState luaState, int index, int depth, int indent, boolean tooltip) {
        //normal print (value only) or when failed to parse the userdata (userdata first, so we always parse it)
        if ((luaState.type(index) == LuaType.USERDATA && !tryParseUserdata(luaState, index)) || depth <= 0)
            return getPrintText(luaState, index, tooltip);

        String spacing = "\t".repeat(indent - 1);

        //format text
        MutableComponent text = TextComponent.EMPTY.copy();
        text.append(new TextComponent("table:").withStyle(getTypeColor(LuaType.TABLE)));
        text.append(new TextComponent(" {\n").withStyle(ChatFormatting.GRAY));

        luaState.pushNil();
        while (luaState.next(index)) {
            int top = luaState.getTop();

            //add indentation
            text.append(spacing).append("\t");

            //add key
            //if (luaState.type(top - 1) == LuaType.USERDATA)
            //    tryParseUserdata(luaState, top - 1);

            text.append(new TextComponent("[").withStyle(ChatFormatting.GRAY));
            text.append(getPrintText(luaState, top - 1, tooltip));
            text.append(new TextComponent("] = ").withStyle(ChatFormatting.GRAY));

            //add value
            LuaType type = luaState.type(top);
            if (type == LuaType.TABLE || type == LuaType.USERDATA)
                text.append(tableToText(luaState, top, depth - 1, indent + 1, tooltip));
            else
                text.append(getPrintText(luaState, top, tooltip));

            //pop value
            luaState.pop(1);
            text.append("\n");
        }

        text.append(spacing).append(new TextComponent("}").withStyle(ChatFormatting.GRAY));
        return text;
    }

    private static boolean tryParseUserdata(LuaState luaState, int index) {
        //if we have an userdata item, get its table representation
        LuaTable table = FiguraJavaReflector.getTableRepresentation(luaState.toJavaObject(index, Object.class));
        if (table != null) {
            table.push(luaState);
            luaState.replace(index);
            return true;
        }

        return false;
    }

    private static String luaToString(LuaState luaState, int index) {
        //push lua "tostring" into the stack
        luaState.getGlobal("tostring");

        //copies a value from the stack and place it on top
        luaState.pushValue(index);

        //make a function call at the top of the stack
        luaState.call(1, 1);

        //gets a value from stack, as string
        String ret = luaState.toString(-1);

        //remove the copied value
        luaState.pop(1);

        return ret;
    }

    private static MutableComponent getPrintText(LuaState luaState, int index, boolean tooltip) {
        LuaType type = luaState.type(index);
        String ret;

        //format value
        switch (type) {
            case STRING -> ret = "\"" + luaToString(luaState, index) + "\"";
            case NUMBER -> {
                Double d = luaState.toJavaObject(index, Double.class);
                ret = d == Math.rint(d) ? String.valueOf(d.intValue()) : String.valueOf(d);
            }
            default -> ret = luaToString(luaState, index);
        }

        MutableComponent text = new TextComponent(ret).withStyle(getTypeColor(type));

        //table tooltip
        if (tooltip && type == LuaType.TABLE) {
            Component table = TextUtils.replaceTabs(tableToText(luaState, index, 1, 1, false));
            text.withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, table)));
        }

        return text;
    }

    private static Style getTypeColor(LuaType type) {
        return switch (type) {
            case TABLE -> Colors.FRAN_PINK.style;
            case NUMBER -> Colors.MAYA_BLUE.style;
            case NIL -> Colors.LUA_ERROR.style;
            case BOOLEAN -> Colors.LUA_PING.style;
            case FUNCTION -> Style.EMPTY.withColor(ChatFormatting.GREEN);
            case USERDATA, LIGHTUSERDATA -> Style.EMPTY.withColor(ChatFormatting.YELLOW);
            case THREAD -> Style.EMPTY.withColor(ChatFormatting.GOLD);
            default -> Style.EMPTY;
        };
    }
}
