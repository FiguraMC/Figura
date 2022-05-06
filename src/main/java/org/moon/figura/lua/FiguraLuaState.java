package org.moon.figura.lua;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.*;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.config.Config;
import org.moon.figura.lua.api.EventsAPI;
import org.moon.figura.lua.api.entity.PlayerEntityWrapper;
import org.moon.figura.lua.api.math.MatricesAPI;
import org.moon.figura.lua.api.math.VectorsAPI;
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
    public EventsAPI events;

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
        loadGlobal(WorldAPI.INSTANCE, "world");
        loadGlobal(new PlayerEntityWrapper(Minecraft.getInstance().player), "player");
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

    //add a chat message on the client
    //param force means that it will be logged somewhere even if unable to send the chat message
    public static void sendChatMessage(Component message) {
        sendChatMessage(message, false);
    }

    public static void sendChatMessage(Component message, boolean force) {
        if (Minecraft.getInstance().gui != null)
            Minecraft.getInstance().gui.getChat().addMessage(TextUtils.replaceTabs(message));
        else if (force)
            FiguraMod.LOGGER.info(message.getString());
    }

    //print a string either on chat or console
    public static void sendLuaMessage(Object message, String owner) {
        Component component = TextComponent.EMPTY.copy()
                .append(new TextComponent("[lua] ").withStyle(Colors.LUA_LOG.style))
                .append(new TextComponent(owner).withStyle(ChatFormatting.ITALIC))
                .append(new TextComponent(" : ").withStyle(Colors.LUA_LOG.style))
                .append(message instanceof Component c ? c : new TextComponent(message.toString()));

        if ((int) Config.LOG_LOCATION.value == 0)
            sendChatMessage(component);
        else
            FiguraMod.LOGGER.info(component.getString());
    }

    //print an error, errors should always show up on chat
    public static void sendLuaError(Exception error, String owner) {
        //Jank as hell
        String message = error.toString().replace("org.terasology.jnlua.LuaRuntimeException: ", "");

        Component component = TextComponent.EMPTY.copy().withStyle(ChatFormatting.ITALIC) 
                .append(new TextComponent("[error] ").withStyle(Colors.LUA_ERROR.style))
                .append(new TextComponent(owner).withStyle(ChatFormatting.ITALIC))
                .append(new TextComponent(" : " + message).withStyle(Colors.LUA_ERROR.style));

        sendChatMessage(component, true);

        if (!(error instanceof LuaRuntimeException))
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
            sendChatMessage(component);
        else
            FiguraMod.LOGGER.info(component.getString());
    }

    private static final JavaFunction INSTRUCTION_LIMIT_FUNCTION = luaState -> {
        String error = "Script overran resource limits!";
        throw new LuaRuntimeException(error);
    };

    //print functions
    private static final JavaFunction PRINT_FUNCTION = luaState -> {
        MutableComponent text = TextComponent.EMPTY.copy();

        //execute if the stack has entries
        while (luaState.getTop() > 0) {
            LuaType type = luaState.type(1);

            if (type == LuaType.TABLE)
                text.append(tableToText(luaState, 1, 0, 1));
            else
                text.append(new TextComponent(getPrintObject(luaState, 1) + "\t").setStyle(getColorForType(type)));

            luaState.remove(1);
        }

        //prints the value, either on chat or console
        sendLuaMessage(text, ((FiguraLuaState) luaState).owner.name);

        return 0;
    };

    private static final JavaFunction PRINT_JSON_FUNCTION = luaState -> {
        sendLuaMessage(TextUtils.tryParseJson(parsePrintArg(luaState, 1).getString()), ((FiguraLuaState) luaState).owner.name);
        return 0;
    };

    private static final JavaFunction PRINT_TABLE_FUNCTION = luaState -> {
        //If we have a userdata item, get its table representation and print that instead
        if (luaState.type(1) == LuaType.USERDATA) {
            LuaTable table = FiguraJavaReflector.getTableRepresentation(luaState.toJavaObject(1, Object.class));
            if (table != null) {
                table.push(luaState);
                luaState.replace(1);
            }
        }
        int depth = luaState.getTop() > 1 ? (int) luaState.checkInteger(2) : 1;
        Component result = tableToText(luaState, 1, depth, 1);
        sendLuaMessage(result, ((FiguraLuaState) luaState).owner.name);
        return 0;
    };

    private static Component tableToText(LuaState luaState, int index, int depth, int indent) {
        //normal print (value only)
        if (depth <= 0)
            return parsePrintArg(luaState, index);

        String spacing = "\t".repeat(indent - 1);

        //format text
        MutableComponent text = TextComponent.EMPTY.copy();
        text.append(new TextComponent("table:").withStyle(getColorForType(LuaType.TABLE)));
        text.append(new TextComponent(" {\n").withStyle(ChatFormatting.GRAY));

        luaState.pushNil();
        while (luaState.next(index)) {
            //add indentation
            text.append(spacing).append("\t");

            //add key
            text.append(new TextComponent("[").withStyle(ChatFormatting.GRAY));

            LuaType type = luaState.type(-2);
            if (type == LuaType.TABLE) {
                Component table = TextUtils.replaceTabs(tableToText(luaState, luaState.getTop() - 1, depth - 1, 1));
                text.append(tableToText(luaState, luaState.getTop() - 1, 0, indent + 1).copy()
                        .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, table)))
                );
            } else
                text.append(new TextComponent(String.valueOf(getPrintObject(luaState, -2))).withStyle(getColorForType(type)));

            text.append(new TextComponent("] = ").withStyle(ChatFormatting.GRAY));

            //print value
            type = luaState.type(-1);
            if (type == LuaType.TABLE)
                text.append(tableToText(luaState, luaState.getTop(), depth - 1, indent + 1));
            else
                text.append(new TextComponent(String.valueOf(getPrintObject(luaState, -1))).withStyle(getColorForType(type)));

            //pop value
            luaState.pop(1);
            text.append("\n");
        }

        text.append(new TextComponent(spacing + "}").withStyle(ChatFormatting.GRAY));
        return text;
    }

    private static Component parsePrintArg(LuaState luaState, int index) {
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

        return new TextComponent(ret).withStyle(getColorForType(LuaType.TABLE));
    }

    private static Style getColorForType(LuaType type) {
        return switch (type) {
            case TABLE -> Colors.FRAN_PINK.style;
            case NUMBER -> Colors.MAYA_BLUE.style;
            case STRING -> Style.EMPTY;
            case BOOLEAN -> Colors.LUA_PING.style;
            case FUNCTION -> Style.EMPTY.applyFormat(ChatFormatting.GREEN);
            case USERDATA, LIGHTUSERDATA -> Style.EMPTY.applyFormat(ChatFormatting.YELLOW);
            case THREAD -> Style.EMPTY.applyFormat(ChatFormatting.BLUE);
            default -> Style.EMPTY.applyFormat(ChatFormatting.GOLD);
        };
    }

    private static Object getPrintObject(LuaState luaState, int index) {
        Object obj = luaState.toJavaObject(index, Object.class);

        if (obj instanceof String s)
            return "\"" + s + "\"";
        if (obj instanceof Double d)
            return d == Math.rint(d) ? d.intValue() : obj;

        return obj;
    }
}
