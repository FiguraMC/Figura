package org.moon.figura.lua.docs;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Pose;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.avatars.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.mixin.input.KeyMappingAccessor;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;

import java.util.LinkedHashSet;
import java.util.function.Supplier;

/**
 * Adds docs for functions that have specific set of String names.
 */
public class FiguraListDocs {

    //-- custom types --//

    public static final LinkedHashSet<String> KEYBINDS = new LinkedHashSet<>();
    private static final LinkedHashSet<Component> PARENT_TYPES = new LinkedHashSet<>() {{
        for (FiguraModelPart.ParentType value : FiguraModelPart.ParentType.values()) {
            add(Component.literal(value.name()).withStyle(ChatFormatting.WHITE));
            for (String alias : value.aliases) {
                add(Component.literal(alias).withStyle(ChatFormatting.GRAY));
            }
        }
    }};
    private static final LinkedHashSet<String> RENDER_TYPES = new LinkedHashSet<>() {{
        for (FiguraTextureSet.RenderTypes value : FiguraTextureSet.RenderTypes.values()) {
            add(value.name());
        }
    }};
    private static final LinkedHashSet<String> TEXTURE_TYPES = new LinkedHashSet<>() {{
        add("skin");
        add("cape");
        add("elytra");
        add("resource");
        add("texture");
    }};
    private static final LinkedHashSet<String> ENTITY_POSES = new LinkedHashSet<>() {{
        for (Pose value : Pose.values()) {
            add(value.name());
        }
    }};

    // -- main method -- //

    public static LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
        //self
        LiteralArgumentBuilder<FabricClientCommandSource> root = LiteralArgumentBuilder.literal("lists");
        root.executes(context -> {
            FiguraMod.sendChatMessage(FiguraDoc.HEADER.copy()
                    .append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.description"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(FiguraText.of("docs.group.lists")
                            .withStyle(ColorUtils.Colors.MAYA_BLUE.style))
            );
            return 1;
        });

        root.then(generateCommand(() -> KEYBINDS, "keybinds", 2));
        root.then(generateCommand(() -> PARENT_TYPES, "parent_types", 2));
        root.then(generateCommand(() -> RENDER_TYPES, "render_types", 1));
        root.then(generateCommand(() -> TEXTURE_TYPES, "texture_types", 1));
        root.then(generateCommand(() -> new LinkedHashSet<>() {{this.addAll(KeyMappingAccessor.getAll().keySet());}}, "key_list", 2));
        root.then(generateCommand(() -> ENTITY_POSES, "entity_poses", 2));

        return root;
    }

    // -- helper functions -- //

    private static LiteralArgumentBuilder<FabricClientCommandSource> generateCommand(Supplier<LinkedHashSet<?>> supplier, String name, int split) {
        //command
        LiteralArgumentBuilder<FabricClientCommandSource> command = LiteralArgumentBuilder.literal(name);

        //display everything
        command.executes(context -> {
            LinkedHashSet<?> coll = supplier.get();

            if (coll.size() == 0) {
                FiguraMod.sendChatMessage(FiguraText.of("docs.list.empty"));
                return 0;
            }

            MutableComponent text = FiguraDoc.HEADER.copy()
                    .append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.description"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.list." + name))
                            .withStyle(ColorUtils.Colors.MAYA_BLUE.style))
                    .append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.entries"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style));

            int i = 0;
            for (Object element : coll) {
                Component component = element instanceof Component c ? c : Component.literal(element.toString()).withStyle(ChatFormatting.WHITE);
                text.append(i % split == 0 ? "\n\t" : "\t");
                text.append(Component.literal("• ").withStyle(ChatFormatting.YELLOW)).append(component);
                i++;
            }

            FiguraMod.sendChatMessage(text);
            return 1;
        });

        //add collection as child for easy navigation
        for (Object element : supplier.get()) {
            String text = element instanceof Component c ? c.getString() : element.toString();
            LiteralArgumentBuilder<FabricClientCommandSource> child = LiteralArgumentBuilder.literal(text);
            child.executes(context -> {
                FiguraMod.sendChatMessage(Component.literal(text).withStyle(ColorUtils.Colors.FRAN_PINK.style));
                return 1;
            });
            command.then(child);
        }

        //return
        return command;
    }
}
