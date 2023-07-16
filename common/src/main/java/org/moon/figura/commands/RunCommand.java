package org.moon.figura.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import org.moon.figura.lua.FiguraLuaPrinter;
import org.moon.figura.lua.FiguraLuaRuntime;
import org.moon.figura.utils.FiguraClientCommandSource;

class RunCommand {

    public static LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
        LiteralArgumentBuilder<FiguraClientCommandSource> run = LiteralArgumentBuilder.literal("run");
        RequiredArgumentBuilder<FiguraClientCommandSource, String> arg = RequiredArgumentBuilder.argument("code", StringArgumentType.greedyString());
        arg.executes(RunCommand::executeCode);
        run.then(arg);
        return run;
    }

    private static int executeCode(CommandContext<FiguraClientCommandSource> context) {
        String lua = StringArgumentType.getString(context, "code");
        FiguraLuaRuntime luaRuntime = FiguraCommands.getRuntime(context);
        if (luaRuntime == null)
            return 0;

        try {
            luaRuntime.load("runCommand", lua).call();
            return 1;
        } catch (Exception | StackOverflowError e) {
            FiguraLuaPrinter.sendLuaError(FiguraLuaRuntime.parseError(e), luaRuntime.owner);
            return 0;
        }
    }
}
