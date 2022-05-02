package org.moon.figura.lua;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.Config;
import org.moon.figura.lua.api.EventsAPI;
import org.moon.figura.lua.api.entity.PlayerEntityWrapper;
import org.moon.figura.lua.api.math.MatricesAPI;
import org.moon.figura.lua.api.math.VectorsAPI;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.utils.ColorUtils.Colors;
import org.moon.figura.utils.TextUtils;
import org.terasology.jnlua.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class FiguraLuaState extends LuaState53 {

    private static String sandboxerScript;

    private final String owner;
    public EventsAPI events;

    public FiguraLuaState(String owner, int memory) {
        super(memory * 1_000_000); //memory is given in mb
        setJavaReflector(FiguraJavaReflector.INSTANCE);
        setConverter(FiguraConverter.INSTANCE);

        this.owner = owner;

        //Load the built-in figura libraries
        loadLibraries();

        //Loads print(), log(), and logTable() into the env.
        loadPrintFunctions();

        //Run the figura sandboxer script
        try {
            runSandboxer();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to load script sandboxer", e);
        }

        loadFiguraApis();
    }

    public boolean init(Map<String, String> scripts, String mainScript) {
        if (scripts.size() == 0)
            return false;

        if (scripts.size() == 1) {
            Map.Entry<String, String> entry = scripts.entrySet().iterator().next();
            runScript(entry.getValue(), entry.getKey());
        } else {
            if (!scripts.containsKey(mainScript)) {
                FiguraMod.LOGGER.error("Failed to load scripts, no script with name \"" + mainScript + ".lua\"");
                return false;
            }
            pushJavaFunction(requireFunc(scripts));
            setGlobal("require");
            runScript(scripts.get(mainScript), mainScript);
        }

        return true;
    }

    public void runScript(String script, String name) {
        load(script, name);
        try {
            call(0, 0);
        } catch (LuaRuntimeException e) {
            sendLuaError(e, owner);
        } catch (Exception e) {
            sendLuaError(new LuaRuntimeException(e), owner);
            FiguraMod.LOGGER.error("", e);
        }
    }

    private void loadFiguraApis() {
        loadGlobal(VectorsAPI.INSTANCE, "vectors");
        loadGlobal(MatricesAPI.INSTANCE, "matrices");
        events = new EventsAPI();
        loadGlobal(events, "events");
        loadGlobal(WorldAPI.INSTANCE, "world");
        loadGlobal(new PlayerEntityWrapper(Minecraft.getInstance().player), "player");
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
            InputStream stream = FiguraMod.class.getResourceAsStream("/assets/figura/scripts/sandbox.lua");
            if (stream == null)
                throw new IOException("Cannot locate sandbox.lua at /assets/figura/scripts/sandbox.lua");
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
            Minecraft.getInstance().gui.getChat().addMessage(TextUtils.replaceInText(message, "\t", new TextComponent("  ")));
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
    public static void sendLuaError(LuaRuntimeException error, String owner) {
        //Jank as hell
        String message = error.toString().replace("org.terasology.jnlua.LuaRuntimeException: ", "");

        Component component = TextComponent.EMPTY.copy().withStyle(ChatFormatting.ITALIC) 
                .append(new TextComponent("[error] ").withStyle(Colors.LUA_ERROR.style))
                .append(new TextComponent(owner).withStyle(ChatFormatting.ITALIC))
                .append(new TextComponent(" : " + message).withStyle(Colors.LUA_ERROR.style));

        sendChatMessage(component, true);
    }

    //print an ping!
    public static void sendPingMessage(String ping, int size, String owner) {
        int config = (int) Config.LOG_PINGS.value;

        //no print? *megamind.png*
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

    //print functions
    private static final JavaFunction PRINT_FUNCTION = luaState -> {
        StringBuilder ret = new StringBuilder();

        //execute if the stack has entries
        while (luaState.getTop() > 0) {
            ret.append(parsePrintArg(luaState));
        }

        //prints the value, either on chat or console
        sendLuaMessage(ret.toString(), ((FiguraLuaState) luaState).owner);

        return 0;
    };

    private static final JavaFunction PRINT_JSON_FUNCTION = luaState -> {
        sendLuaMessage(TextUtils.tryParseJson(parsePrintArg(luaState)), ((FiguraLuaState) luaState).owner);
        return 0;
    };

    private static final JavaFunction PRINT_TABLE_FUNCTION = luaState -> {
        int depth = (int) luaState.checkInteger(2);
        String result = tableToString(luaState, 1, depth, 1);
        sendLuaMessage(result, ((FiguraLuaState) luaState).owner);
        return 0;
    };

    private static String tableToString(LuaState luaState, int index, int depth, int indent) {
        StringBuilder builder = new StringBuilder();
        if (depth <= 0) {
            luaState.getGlobal("tostring");
            luaState.pushValue(index);
            luaState.call(1, 1);
            builder.append(luaState.toString(-1));
            luaState.pop(1);
            return builder.toString();
        }
        String indentStr = "    ".repeat(indent-1);
        builder.append("\n").append(indentStr).append("{\n");
        luaState.pushNil();
        while (luaState.next(index)) {
            //Print indentation
            builder.append(indentStr).append("    ");

            //Print key
            LuaType type = luaState.type(-2);
            if (type == LuaType.TABLE)
                builder.append(tableToString(luaState, luaState.getTop()-1, depth-1, indent+1));
            else
                builder.append(luaState.toJavaObject(-2, Object.class));
            builder.append(": ");

            //Print value
            type = luaState.type(-1);
            if (type == LuaType.TABLE)
                builder.append(tableToString(luaState, luaState.getTop(), depth-1, indent+1));
            else
                builder.append(luaState.toJavaObject(-1, Object.class));
            builder.append("\n");

            //Pop value
            luaState.pop(1);
        }
        builder.append(indentStr).append("}\n");
        return builder.toString();
    }

    private static String parsePrintArg(LuaState luaState) {
        //push lua "tostring" into the stack
        luaState.getGlobal("tostring");

        //copies a value from the stack and place it on top
        luaState.pushValue(1);

        //make a function call at the top of the stack
        luaState.call(1, 1);

        //gets a value from stack, as string
        String ret = luaState.toString(-1) + "\t";

        //remove the copied value and original
        luaState.pop(1);
        luaState.remove(1);

        return ret;
    }
}
