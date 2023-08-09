package org.figuramc.figura.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.font.EmojiContainer;
import org.figuramc.figura.font.EmojiUnicodeLookup;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraClientCommandSource;

import java.util.Arrays;
import java.util.function.Consumer;

import static net.minecraft.network.chat.Component.literal;

class EmojiListCommand {

    public static LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
        LiteralArgumentBuilder<FiguraClientCommandSource> load = LiteralArgumentBuilder.literal("emojis");

        RequiredArgumentBuilder<FiguraClientCommandSource, String> path = RequiredArgumentBuilder.argument("category", StringArgumentType.greedyString());
        path.executes(EmojiListCommand::listCategory);

        LiteralArgumentBuilder<FiguraClientCommandSource> all = LiteralArgumentBuilder.literal("all");
        all.executes(EmojiListCommand::listAll);

        return load.then(path).then(all);
    }

    private static int listCategory(CommandContext<FiguraClientCommandSource> context) {
        FiguraClientCommandSource src = context.getSource();
        return printEmojis(context.getArgument("category", String.class), src::figura$sendFeedback, src::figura$sendError) ? 1 : 0;
    }

    private static int listAll(CommandContext<FiguraClientCommandSource> context) {
        FiguraClientCommandSource src = context.getSource();
        for (String category : Emojis.getCategoryNames()) {
            if (!printEmojis(category, src::figura$sendFeedback, src::figura$sendError)) {
                return 0;
            }
        }

        return 1;
    }

    private static boolean printEmojis(String category, Consumer<Component> feedback, Consumer<Component> error) {
        if (!Emojis.hasCategory(category)) {
            error.accept(literal("Emoji category \"" + category + "\" doesn't exist!"));
            return false;
        }

        EmojiContainer container = Emojis.getCategory(category);

        // give the category a title
        feedback.accept(literal("--- " + container.name + " ---").withStyle(ColorUtils.Colors.AWESOME_BLUE.style));

        EmojiUnicodeLookup lookup = container.getLookup();

        StringBuilder builder = new StringBuilder();

        // Gather each emoji name and append it into one big string
        lookup.aliasValues().stream().sorted().forEach(key -> Arrays.stream(lookup.getAliases(key)).forEach(name -> builder.append(':').append(name).append(':').append(' ')));

        feedback.accept(literal(builder.toString()));

        return true;
    }
}
