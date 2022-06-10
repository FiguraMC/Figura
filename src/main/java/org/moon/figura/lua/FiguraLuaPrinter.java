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
import java.util.UUID;

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
        MutableComponent component = Component.empty()
                .append(Component.literal("[lua] ").withStyle(ColorUtils.Colors.LUA_LOG.style))
                .append(Component.literal(owner).withStyle(ChatFormatting.ITALIC))
                .append(Component.literal(" : ").withStyle(ColorUtils.Colors.LUA_LOG.style))
                .append(message instanceof Component c ? c : Component.literal(message.toString()))
                .append(Component.literal("\n"));

        if ((int) Config.LOG_LOCATION.value == 0)
            sendLuaChatMessage(component);
        else
            FiguraMod.LOGGER.info(component.getString());
    }

    //print an error, errors should always show up on chat
    public static void sendLuaError(Exception error, String name, UUID owner) {
        if (!(boolean) Config.LOG_OTHERS.value && !FiguraMod.isLocal(owner))
            return;

        //Jank as hell
        String message = error.toString().replace("org.terasology.jnlua.LuaRuntimeException: ", "");
        message = message.replace("org.terasology.jnlua.LuaMemoryAllocationException: ", "Memory error: ");
        //Might do something unexpected in the extremely niche circumstance that someone has their own script named "autoScripts" and has an error on line 1.
        message = message.replace("[string \"autoScripts\"]:1: ", "");

        MutableComponent component = Component.empty()
                .append(Component.literal("[error] ").withStyle(ColorUtils.Colors.LUA_ERROR.style))
                .append(Component.literal(name).withStyle(ChatFormatting.ITALIC))
                .append(Component.literal(" : " + message).withStyle(ColorUtils.Colors.LUA_ERROR.style))
                .append(Component.literal("\n"));

        sendLuaChatMessage(component);
        FiguraMod.LOGGER.error("", error);
    }

    //print an ping!
    public static void sendPingMessage(String ping, int size, String owner) {
        int config = 0; //(int) Config.LOG_PINGS.value;

        //no ping? *megamind.png*
        if (config == 0)
            return;

        MutableComponent component = Component.empty()
                .append(Component.literal("[ping] ").withStyle(ColorUtils.Colors.LUA_PING.style))
                .append(Component.literal(owner).withStyle(ChatFormatting.ITALIC))
                .append(Component.literal(" : ").withStyle(ColorUtils.Colors.LUA_PING.style))
                .append(size + "b")
                .append(Component.literal(" : ").withStyle(ColorUtils.Colors.LUA_PING.style))
                .append(ping)
                .append(Component.literal("\n"));

        if (config == 1)
            sendLuaChatMessage(component);
        else
            FiguraMod.LOGGER.info(component.getString());
    }

    //print functions
    private static final JavaFunction PRINT_FUNCTION = luaState -> {
        if (!(boolean) Config.LOG_OTHERS.value && !FiguraMod.isLocal(((FiguraLuaState) luaState).getOwner().owner))
            return 0;

        MutableComponent text = Component.empty();

        //execute if the stack has entries
        while (luaState.getTop() > 0) {
            text.append(getPrintText(luaState, 1, true, false)).append("\t");
            luaState.remove(1);
        }

        //prints the value, either on chat or console
        sendLuaMessage(text, ((FiguraLuaState) luaState).getOwner().name);

        return 0;
    };

    private static final JavaFunction PRINT_JSON_FUNCTION = luaState -> {
        if (!(boolean) Config.LOG_OTHERS.value && !FiguraMod.isLocal(((FiguraLuaState) luaState).getOwner().owner))
            return 0;

        MutableComponent text = Component.empty();

        if (luaState.getTop() > 0)
            text.append(TextUtils.tryParseJson(luaToString(luaState, 1)));

        sendLuaMessage(text, ((FiguraLuaState) luaState).getOwner().name);

        return 0;
    };

    private static final JavaFunction PRINT_TABLE_FUNCTION = luaState -> {
        if (!(boolean) Config.LOG_OTHERS.value && !FiguraMod.isLocal(((FiguraLuaState) luaState).getOwner().owner))
            return 0;

        MutableComponent text = Component.empty();

        if (luaState.getTop() > 0) {
            int depth = luaState.getTop() > 1 ? (int) luaState.checkInteger(2) : 1;
            text.append(tableToText(luaState, 1, depth, 1, true));
        }

        sendLuaMessage(text, ((FiguraLuaState) luaState).getOwner().name);

        return 0;
    };

    private static Component tableToText(LuaState luaState, int index, int depth, int indent, boolean hasTooltip) {
        //copy the value to top
        luaState.pushValue(index);
        int top = luaState.getTop();

        //attempt to parse top
        LuaType type = luaState.type(index);
        if (type == LuaType.USERDATA)
            tryParseUserdata(luaState, top);

        //normal print when failed to parse userdata or depth limit
        if (luaState.type(top) != LuaType.TABLE || depth <= 0) {
            Component text = getPrintText(luaState, index, hasTooltip, true);
            luaState.pop(1);
            return text;
        }

        //format text
        MutableComponent text = Component.empty();
        text.append(Component.literal(type == LuaType.USERDATA ? "userdata:" : "table:").withStyle(getTypeColor(type)));
        text.append(Component.literal(" {\n").withStyle(ChatFormatting.GRAY));

        String spacing = "\t".repeat(indent - 1);

        luaState.pushNil();
        while (luaState.next(top)) {
            int tableTop = luaState.getTop();

            //add indentation
            text.append(spacing).append("\t");

            //add key
            text.append(Component.literal("[").withStyle(ChatFormatting.GRAY));
            text.append(getPrintText(luaState, tableTop - 1, hasTooltip, true));
            text.append(Component.literal("] = ").withStyle(ChatFormatting.GRAY));

            //add value
            type = luaState.type(tableTop);
            if (type == LuaType.TABLE || type == LuaType.USERDATA)
                text.append(tableToText(luaState, tableTop, depth - 1, indent + 1, hasTooltip));
            else
                text.append(getPrintText(luaState, tableTop, hasTooltip, true));

            //pop value
            luaState.pop(1);
            text.append("\n");
        }

        //pop copied value
        luaState.pop(1);

        text.append(spacing).append(Component.literal("}").withStyle(ChatFormatting.GRAY));
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

    //fancyString just means to add quotation marks around strings.
    private static MutableComponent getPrintText(LuaState luaState, int index, boolean hasTooltip, boolean quoteStrings) {
        LuaType type = luaState.type(index);
        String ret;

        //format value
        switch (type) {
            case STRING -> {
                if (!quoteStrings)
                    ret = luaToString(luaState, index);
                else
                    ret = "\"" + luaToString(luaState, index) + "\"";
            }
            case NUMBER -> {
                Double d = luaState.toJavaObject(index, Double.class);
                ret = d == Math.rint(d) ? String.valueOf(d.longValue()) : String.valueOf(d);
            }
            default -> ret = luaToString(luaState, index);
        }

        MutableComponent text = Component.literal(ret).withStyle(getTypeColor(type));

        //table tooltip
        if (hasTooltip && (type == LuaType.TABLE || type == LuaType.USERDATA)) {
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
            charsQueued += message.getString().length();
            if (charsQueued > MAX_CHARS_QUEUED) {
                chatQueue.clear();
                charsQueued = 0;
                throw new LuaRuntimeException("Chat overflow: printing too much!");
            }
            chatQueue.offer(message);
        } else {
            MutableComponent withoutSiblings = message.plainCopy().withStyle(message.getStyle());
            sendLuaChatMessage(withoutSiblings);
            for (Component sibling : message.getSiblings()) {
                ((MutableComponent) sibling).setStyle(sibling.getStyle().applyTo(message.getStyle()));
                sendLuaChatMessage((MutableComponent) sibling);
            }

        }
    }

    public static void printChatFromQueue() {
        int i = MAX_CHARS_PER_TICK;
        int totalLen = 0;
        MutableComponent bigComponent = chatQueue.poll();
        while (chatQueue.size() > 1) {
            MutableComponent smallComponent = Component.empty();

            int len = 0;
            MutableComponent smallerComponent = chatQueue.poll();
            while (chatQueue.size() > 1 && !smallerComponent.getString().equals("\n")) {
                smallComponent.append(smallerComponent);
                len += smallerComponent.getString().length();
                smallerComponent = chatQueue.poll();
            }
            smallComponent.append(smallerComponent);
            len++;

            i -= len;
            if (i < 0) {
                charsQueued -= len;
                if (len > MAX_CHARS_PER_TICK) {
                    String s = "[Component too big, not printing]";
                    chatQueue.addFirst(Component.literal(s));
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
