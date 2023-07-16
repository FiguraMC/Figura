package org.moon.figura.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import org.moon.figura.FiguraMod;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;

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

    public static LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
        //get links
        LiteralArgumentBuilder<FabricClientCommandSource> links = LiteralArgumentBuilder.literal("links");
        links.executes(context -> {
            //header
            MutableComponent message = Component.empty().withStyle(ColorUtils.Colors.FRAN_PINK.style)
                    .append(Component.literal("•*+•* ")
                            .append(FiguraText.of())
                            .append(" Links *•+*•").withStyle(ChatFormatting.UNDERLINE))
                    .append("\n");

            //add links
            for (FiguraMod.Links link : LINKS) {
                message.append("\n");

                if (link == null)
                    continue;

                message.append(Component.literal("• [" + link.name() + "]")
                        .withStyle(link.style)
                        .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link.url)))
                        .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(link.url)))));
            }

            FiguraMod.sendChatMessage(message);
            return 1;
        });

        return links;
    }
}
