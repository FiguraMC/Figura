package org.moon.figura.lua;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.VarArgFunction;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.config.Config;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.UUID;
import java.util.function.Function;

public class FiguraLuaPrinter {

    public static DecimalFormat df;
    static {
        loadDF();
    }

    public static void loadDF() {
        int config = Config.LOG_NUMBER_LENGTH.asInt();
        df = new DecimalFormat("0" + (config > 0 ? "." + "#".repeat(config) : ""));
        df.setRoundingMode(RoundingMode.DOWN);
    }

    public static void loadPrintFunctions(FiguraLuaRuntime runtime) {
        LuaValue print = PRINT_FUNCTION.apply(runtime);
        runtime.setGlobal("print", print);
        runtime.setGlobal("log", print);

        LuaValue printJson = PRINT_JSON_FUNCTION.apply(runtime);
        runtime.setGlobal("printJson", printJson);
        runtime.setGlobal("logJson", printJson);

        LuaValue printTable = PRINT_TABLE_FUNCTION.apply(runtime);
        runtime.setGlobal("printTable", printTable);
        runtime.setGlobal("logTable", printTable);
    }

    //print a string either on chat or console
    public static void sendLuaMessage(Object message, String owner) {
        MutableComponent component = Component.empty()
                .append(Component.literal("[lua] ").withStyle(ColorUtils.Colors.LUA_LOG.style))
                .append(Component.literal(owner))
                .append(Component.literal(" : ").withStyle(ColorUtils.Colors.LUA_LOG.style))
                .append(message instanceof Component c ? c : Component.literal(message.toString()))
                .append(Component.literal("\n"));

        if (Config.LOG_LOCATION.asInt() == 0)
            sendLuaChatMessage(component);
        else
            FiguraMod.LOGGER.info(component.getString());
    }

    //print an error, errors should always show up on chat
    public static void sendLuaError(Exception error, String name, UUID owner) {
        if (!Config.LOG_OTHERS.asBool() && !FiguraMod.isLocal(owner))
            return;

        //Jank as hell
        String message = error.toString().replace("org.luaj.vm2.LuaError: ", "");
        message = message.replace("\n\tautoScripts:1: in main chunk", "");
        message = message.replace("\n\t[Java]: in ?", "");

        message = message.replace("autoScripts:1 [string ", "Script [");
        message = message.replace(": syntax error\nstack traceback:", ": syntax error");

        MutableComponent component = Component.empty()
                .append(Component.literal("[error] ").withStyle(ColorUtils.Colors.LUA_ERROR.style))
                .append(Component.literal(name))
                .append(Component.literal(" : " + message).withStyle(ColorUtils.Colors.LUA_ERROR.style))
                .append(Component.literal("\n"));

        sendLuaChatMessage(component);
        FiguraMod.LOGGER.error("", error);
    }

    //print an ping!
    public static void sendPingMessage(Avatar owner, String ping, int size, Varargs args) {
        int config = Config.LOG_PINGS.asInt();

        //no ping? *megamind.png*
        if (config == 0)
            return;

        MutableComponent text = Component.empty()
                .append(Component.literal("[ping] ").withStyle(ColorUtils.Colors.LUA_PING.style))
                .append(Component.literal(owner.entityName))
                .append(Component.literal(" : ").withStyle(ColorUtils.Colors.LUA_PING.style))
                .append(ping)
                .append(Component.literal(" :: ").withStyle(ColorUtils.Colors.LUA_PING.style))
                .append(size + " bytes")
                .append(Component.literal(" :: ").withStyle(ColorUtils.Colors.LUA_PING.style));

        for (int i = 0; i < args.narg(); i++)
            text.append(getPrintText(owner.luaRuntime.typeManager, args.arg(i + 1), true, false)).append("\t");

        text.append(Component.literal("\n"));

        if (config == 1)
            sendLuaChatMessage(text);
        else
            FiguraMod.LOGGER.info(text.getString());
    }

    //print functions
    private static final Function<FiguraLuaRuntime, LuaValue> PRINT_FUNCTION = runtime -> new VarArgFunction() {
        @Override
        public Varargs invoke(Varargs args) {
            if (!Config.LOG_OTHERS.asBool() && !FiguraMod.isLocal(runtime.owner.owner))
                return NIL;

            MutableComponent text = Component.empty();
            for (int i = 0; i < args.narg(); i++)
                text.append(getPrintText(runtime.typeManager, args.arg(i + 1), true, false)).append("\t");

            //prints the value, either on chat or console
            sendLuaMessage(text, runtime.owner.entityName);

            return NIL;
        }

        @Override
        public String tojstring() {
            return "function: print";
        }
    };

    private static final Function<FiguraLuaRuntime, LuaValue> PRINT_JSON_FUNCTION = runtime -> new VarArgFunction() {
        @Override
        public Varargs invoke(Varargs args) {
            if (!Config.LOG_OTHERS.asBool() && !FiguraMod.isLocal(runtime.owner.owner))
                return NIL;

            MutableComponent text = Component.empty();
            for (int i = 0; i < args.narg(); i++)
                text.append(TextUtils.tryParseJson(args.arg(i + 1).tojstring()));

            sendLuaMessage(text, runtime.owner.entityName);

            return NIL;
        }

        @Override
        public String tojstring() {
            return "function: printJson";
        }
    };

    private static final Function<FiguraLuaRuntime, LuaValue> PRINT_TABLE_FUNCTION = runtime -> new VarArgFunction() {
        @Override
        public Varargs invoke(Varargs args) {
            if (!Config.LOG_OTHERS.asBool() && !FiguraMod.isLocal(runtime.owner.owner))
                return NIL;

            MutableComponent text = Component.empty();

            if (args.narg() > 0) {
                int depth = args.arg(2).isnumber() ? args.arg(2).checkint() : 1;
                text.append(tableToText(runtime.typeManager, args.arg(1), depth, 1, true));
            }

            sendLuaMessage(text, runtime.owner.entityName);

            return NIL;
        }

        @Override
        public String tojstring() {
            return "function: printTable";
        }
    };

    private static Component tableToText(LuaTypeManager typeManager, LuaValue value, int depth, int indent, boolean hasTooltip) {
        //attempt to parse top
        if (value.isuserdata())
            return userdataToText(typeManager, value, depth, indent, hasTooltip);

        //normal print when invalid type or depth limit
        if (!value.istable() || depth <= 0)
            return getPrintText(typeManager, value, hasTooltip, true);

        //format text
        MutableComponent text = Component.empty()
                .append(Component.literal("table:").withStyle(getTypeColor(value)))
                .append(Component.literal(" {\n").withStyle(ChatFormatting.GRAY));

        String spacing = "\t".repeat(indent - 1);

        LuaTable table = value.checktable();
        for (LuaValue key : table.keys())
            text.append(getTableEntry(typeManager, spacing, key, table.get(key), hasTooltip, depth, indent));

        text.append(spacing).append(Component.literal("}").withStyle(ChatFormatting.GRAY));
        return text;
    }

    //needs a special print because we want to also print NIL values
    private static Component userdataToText(LuaTypeManager typeManager, LuaValue value, int depth, int indent, boolean hasTooltip) {
        //normal print when failed to parse userdata or depth limit
        if (!value.isuserdata() || depth <= 0)
            return getPrintText(typeManager, value, hasTooltip, true);

        //format text
        MutableComponent text = Component.empty()
                .append(Component.literal("userdata:").withStyle(getTypeColor(value)))
                .append(Component.literal(" {\n").withStyle(ChatFormatting.GRAY));

        String spacing = "\t".repeat(indent - 1);

        Object data = value.checkuserdata();
        Class<?> clazz = data.getClass();
        if (clazz.isAnnotationPresent(LuaWhitelist.class)) {
            //fields
            for (Field field : clazz.getFields()) {
                if (!field.isAnnotationPresent(LuaWhitelist.class))
                    continue;

                try {
                    Object obj = field.get(data);
                    text.append(getTableEntry(typeManager, spacing, LuaValue.valueOf(field.getName()), typeManager.javaToLua(obj), hasTooltip, depth, indent));
                } catch (Exception e) {
                    FiguraMod.LOGGER.error("", e);
                }
            }

            //methods
            for (Method method : clazz.getMethods()) {
                if (method.isAnnotationPresent(LuaWhitelist.class) && !method.getName().startsWith("__"))
                    text.append(getTableEntry(typeManager, spacing, LuaValue.valueOf(method.getName()), typeManager.getWrapper(method), hasTooltip, depth, indent));
            }
        }

        text.append(spacing).append(Component.literal("}").withStyle(ChatFormatting.GRAY));
        return text;
    }

    private static MutableComponent getTableEntry(LuaTypeManager typeManager, String spacing, LuaValue key, LuaValue value, boolean hasTooltip, int depth, int indent) {
        MutableComponent text = Component.empty()
                .append(spacing).append("\t");

        //key
        text.append(Component.literal("[").withStyle(ChatFormatting.GRAY))
                .append(getPrintText(typeManager, key, hasTooltip, true))
                .append(Component.literal("] = ").withStyle(ChatFormatting.GRAY));

        //value
        if (value.istable() || value.isuserdata())
            text.append(tableToText(typeManager, value, depth - 1, indent + 1, hasTooltip));
        else
            text.append(getPrintText(typeManager, value, hasTooltip, true));

        text.append("\n");
        return text;
    }

    //fancyString just means to add quotation marks around strings.
    private static MutableComponent getPrintText(LuaTypeManager typeManager, LuaValue value, boolean hasTooltip, boolean quoteStrings) {
        String ret;

        //format value
        if (!(value instanceof LuaString) && value.isnumber()) {
            Double d = value.checkdouble();
            ret = d == Math.rint(d) ? String.valueOf(d.longValue()) : df.format(d);
        } else {
            ret = value.tojstring();
            if (value.isstring() && quoteStrings)
                ret = "\"" + ret + "\"";
        }

        MutableComponent text = Component.literal(ret).withStyle(getTypeColor(value));

        //table tooltip
        if (hasTooltip && (value.istable() || value.isuserdata())) {
            Component table = TextUtils.replaceTabs(tableToText(typeManager, value, 1, 1, false));
            text.withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, table)));
        }

        return text;
    }

    private static Style getTypeColor(LuaValue value) {
        if (value.istable())
            return ColorUtils.Colors.FRAN_PINK.style;
        else if (!(value instanceof LuaString) && value.isnumber())
            return ColorUtils.Colors.MAYA_BLUE.style;
        else if (value.isnil())
            return ColorUtils.Colors.LUA_ERROR.style;
        else if (value.isboolean())
            return ColorUtils.Colors.LUA_PING.style;
        else if (value.isfunction())
            return Style.EMPTY.withColor(ChatFormatting.GREEN);
        else if (value.isuserdata())
            return Style.EMPTY.withColor(ChatFormatting.YELLOW);
        else if (value.isthread())
            return Style.EMPTY.withColor(ChatFormatting.GOLD);
        else
            return Style.EMPTY.withColor(ChatFormatting.WHITE);
    }

    //-- SLOW PRINTING OF LOG --//

    //Log safety
    private static final LinkedList<MutableComponent> chatQueue = new LinkedList<>();
    private static final int MAX_CHARS_QUEUED = 10000000;
    private static int charsQueued = 0;
    private static final int MAX_CHARS_PER_TICK = 10000;

    /**
     * Sends a message making use of the queue
     * @param message to send
     * @throws org.luaj.vm2.LuaError if the message could not fit in the queue
     */
    private static void sendLuaChatMessage(MutableComponent message) throws LuaError {
        if (message.getSiblings().isEmpty()) {
            charsQueued += message.getString().length();
            if (charsQueued > MAX_CHARS_QUEUED) {
                chatQueue.clear();
                charsQueued = 0;
                throw new LuaError("Chat overflow: printing too much!");
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
