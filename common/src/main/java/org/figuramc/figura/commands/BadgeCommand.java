package org.figuramc.figura.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.utils.FiguraClientCommandSource;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class BadgeCommand {
    public static LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
        LiteralArgumentBuilder<FiguraClientCommandSource> result = literal("badge");
        for (Badges.Pride badge : Badges.Pride.values()) {
            LiteralArgumentBuilder<FiguraClientCommandSource> option = literal(badge.name().toLowerCase());
            option.executes(new ExecutionTarget(badge));
            result.then(option);
        }

        LiteralArgumentBuilder<FiguraClientCommandSource> clear = literal("clear");
        clear.executes(BadgeCommand::clear);
        result.then(clear);

        return result;
    }

    private static int clear(CommandContext<FiguraClientCommandSource> context) {
        NetworkStuff.clearBadge();
        return 0;
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static class ExecutionTarget implements Command<FiguraClientCommandSource> {
        public final Badges.Pride badge;

        private ExecutionTarget(Badges.Pride badge) {
            this.badge = badge;
        }

        @Override
        public int run(CommandContext<FiguraClientCommandSource> context) {
            NetworkStuff.setBadge(badge.ordinal());
            return 0;
        }
    }
}
