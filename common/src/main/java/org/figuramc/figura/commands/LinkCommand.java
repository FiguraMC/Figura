package org.figuramc.figura.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.FiguraMod;

import java.util.ArrayList;
import java.util.List;

class LinkCommand {

    private static final List<FiguraMod.Links> LINKS = new ArrayList<>() {{
            add(FiguraMod.Links.Wiki);
            add(FiguraMod.Links.Kofi);
            add(null);
            add(FiguraMod.Links.Discord);
            add(FiguraMod.Links.Github);
            add(null);
            add(FiguraMod.Links.Modrinth);
            add(FiguraMod.Links.Curseforge);
    }};

    public static LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
        //get links
        LiteralArgumentBuilder<FiguraClientCommandSource> links = LiteralArgumentBuilder.literal("links");
        links.executes(context -> {
            //header
            MutableComponent message = TextComponent.EMPTY.copy().withStyle(ColorUtils.Colors.PINK.style)
                    .append(new TextComponent("•*+•* ")
                            .append(new FiguraText())
                            .append(" Links *•+*•").withStyle(ChatFormatting.UNDERLINE))
                    .append("\n");

            //add links
            for (FiguraMod.Links link : LINKS) {
                message.append("\n");

                if (link == null)
                    continue;

                message.append(new TextComponent("• [" + link.name() + "]")
                        .withStyle(link.style)
                        .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link.url)))
                        .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(link.url)))));
            }

            FiguraMod.sendChatMessage(message);
            return 1;
        });

        return links;
    }
}
