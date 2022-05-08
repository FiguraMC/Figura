package org.moon.figura.lua;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.Config;
import org.moon.figura.lua.types.LuaTable;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.TextUtils;
import org.terasology.jnlua.JavaFunction;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaType;

import java.util.LinkedList;

public class FiguraLuaPrinter {

    public static void loadPrintFunctions(FiguraLuaState luaState) {
        luaState.pushJavaFunction(PRINT_FUNCTION);
        luaState.pushValue(-1);
        luaState.setGlobal("print");
        luaState.setGlobal("log");

        luaState.pushJavaFunction(PRINT_JSON_FUNCTION);
        luaState.pushValue(-1);
        luaState.setGlobal("printJson");
        luaState.setGlobal("logJson");

        luaState.pushJavaFunction(PRINT_TABLE_FUNCTION);
        luaState.pushValue(-1);
        luaState.setGlobal("printTable");
        luaState.setGlobal("logTable");
    }

    //print a string either on chat or console
    public static void sendLuaMessage(Object message, String owner) {
        MutableComponent component = TextComponent.EMPTY.copy()
                .append(new TextComponent("[lua] ").withStyle(ColorUtils.Colors.LUA_LOG.style))
                .append(new TextComponent(owner).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.WHITE))
                .append(new TextComponent(" : ").withStyle(ColorUtils.Colors.LUA_LOG.style))
                .append(message instanceof Component c ? c : new TextComponent(message.toString()))
                .append(new TextComponent("\n"));

        if ((int) Config.LOG_LOCATION.value == 0)
            sendLuaChatMessage(component);
        else
            FiguraMod.LOGGER.info(component.getString());
    }

    //print an error, errors should always show up on chat
    public static void sendLuaError(Exception error, String owner) {
        //Jank as hell
        String message = error.toString().replace("org.terasology.jnlua.LuaRuntimeException: ", "");

        MutableComponent component = TextComponent.EMPTY.copy()
                .append(new TextComponent("[error] ").withStyle(ColorUtils.Colors.LUA_ERROR.style))
                .append(new TextComponent(owner).withStyle(ChatFormatting.ITALIC))
                .append(new TextComponent(" : " + message).withStyle(ColorUtils.Colors.LUA_ERROR.style))
                .append(new TextComponent("\n"));

        sendLuaChatMessage(component);
        FiguraMod.LOGGER.error("", error);
    }

    //print an ping!
    public static void sendPingMessage(String ping, int size, String owner) {
        int config = 0; //(int) Config.LOG_PINGS.value;

        //no ping? *megamind.png*
        if (config == 0)
            return;

        MutableComponent component = TextComponent.EMPTY.copy()
                .append(new TextComponent("[ping] ").withStyle(ColorUtils.Colors.LUA_PING.style))
                .append(new TextComponent(owner).withStyle(ChatFormatting.ITALIC))
                .append(new TextComponent(" : ").withStyle(ColorUtils.Colors.LUA_PING.style))
                .append(size + "b")
                .append(new TextComponent(" : ").withStyle(ColorUtils.Colors.LUA_PING.style))
                .append(ping)
                .append(new TextComponent("\n"));

        if (config == 1)
            sendLuaChatMessage(component);
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
        sendLuaMessage(text, ((FiguraLuaState) luaState).getOwner().name);

        return 0;
    };

    private static final JavaFunction PRINT_JSON_FUNCTION = luaState -> {
        sendLuaMessage(TextUtils.tryParseJson(luaToString(luaState, 1)), ((FiguraLuaState) luaState).getOwner().name);
        return 0;
    };

    private static final JavaFunction PRINT_TABLE_FUNCTION = luaState -> {
        int depth = luaState.getTop() > 1 ? (int) luaState.checkInteger(2) : 1;
        Component result = tableToText(luaState, 1, depth, 1, true);
        sendLuaMessage(result, ((FiguraLuaState) luaState).getOwner().name);
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
            if (luaState.type(top - 1) == LuaType.USERDATA)
                tryParseUserdata(luaState, top - 1);

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
            case TABLE -> ColorUtils.Colors.FRAN_PINK.style;
            case NUMBER -> ColorUtils.Colors.MAYA_BLUE.style;
            case NIL -> ColorUtils.Colors.LUA_ERROR.style;
            case BOOLEAN -> ColorUtils.Colors.LUA_PING.style;
            case FUNCTION -> Style.EMPTY.withColor(ChatFormatting.GREEN);
            case USERDATA, LIGHTUSERDATA -> Style.EMPTY.withColor(ChatFormatting.YELLOW);
            case THREAD -> Style.EMPTY.withColor(ChatFormatting.GOLD);
            case STRING -> Style.EMPTY.withColor(ChatFormatting.WHITE);
        };
    }

    //-- SLOW PRINTING OF LOG --//

    //Log safety
    private static final LinkedList<MutableComponent> chatQueue = new LinkedList<>();
    private static final int MAX_CHARS_QUEUED = 10000000;
    private static int charsQueued = 0;
    private static final int MAX_CHARS_PER_TICK = 10000;

    /**
     * Sends a message making use of the queue
     * @param message
     * @throws LuaRuntimeException if the message could not fit in the queue
     */
    private static void sendLuaChatMessage(MutableComponent message) throws LuaRuntimeException {
        if (message.getSiblings().isEmpty()) {
            charsQueued += message.getContents().length();
            if (charsQueued > MAX_CHARS_QUEUED) {
                chatQueue.clear();
                charsQueued = 0;
                throw new LuaRuntimeException("Chat overflow: printing too much!");
            }
            chatQueue.offer(message);
        } else {
            MutableComponent withoutSiblings = message.plainCopy().withStyle(message.getStyle());
            sendLuaChatMessage(withoutSiblings);
            for (Component sibling : message.getSiblings())
                sendLuaChatMessage((MutableComponent) sibling);
        }
    }

    public static void printChatFromQueue() {
        int i = MAX_CHARS_PER_TICK;
        int totalLen = 0;
        MutableComponent bigComponent = chatQueue.poll();
        while (chatQueue.size() > 1) {
            MutableComponent smallComponent = chatQueue.poll();

            int len = smallComponent.getContents().length();
            MutableComponent smallerComponent = chatQueue.poll();
            while (chatQueue.size() > 1 && !smallerComponent.getContents().equals("\n")) {
                smallComponent.append(smallerComponent);
                len += smallerComponent.getContents().length();
                smallerComponent = chatQueue.poll();
            }
            smallComponent.append(smallerComponent);
            len++;

            i -= len;
            if (i < 0) {
                charsQueued -= len;
                if (len > MAX_CHARS_PER_TICK) {
                    String s = "[Component too big, not printing]";
                    chatQueue.addFirst(new TextComponent(s));
                    charsQueued += s.length();
                } else {
                    chatQueue.addFirst(smallComponent);
                }
                break;
            }
            bigComponent.append(smallComponent);
            totalLen += len;
        }
        if (chatQueue.size() == 1)
            chatQueue.clear();
        if (bigComponent != null) {
            FiguraMod.sendChatMessage(bigComponent);
            charsQueued -= totalLen;
        }
    }

}
