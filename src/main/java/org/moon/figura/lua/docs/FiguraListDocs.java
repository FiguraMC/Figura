package org.moon.figura.lua.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import org.moon.figura.FiguraMod;
import org.moon.figura.animation.Animation;
import org.moon.figura.avatars.model.ParentType;
import org.moon.figura.avatars.model.rendering.texture.RenderTypes;
import org.moon.figura.mixin.input.KeyMappingAccessor;
import org.moon.figura.mixin.render.GameRendererAccessor;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;

import java.util.LinkedHashSet;
import java.util.function.Supplier;

/**
 * Adds docs for functions that have specific set of String names.
 */
public class FiguraListDocs {

    //-- types --//

    public static final LinkedHashSet<String> KEYBINDS = new LinkedHashSet<>();
    private static final LinkedHashSet<Component> PARENT_TYPES = new LinkedHashSet<>() {{
        for (ParentType value : ParentType.values()) {
            add(Component.literal(value.name()).withStyle(ChatFormatting.WHITE));
            for (String alias : value.aliases) {
                add(Component.literal(alias).withStyle(ChatFormatting.GRAY));
            }
        }
    }};
    private static final LinkedHashSet<String> RENDER_TYPES = new LinkedHashSet<>() {{
        for (RenderTypes value : RenderTypes.values()) {
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
    private static final LinkedHashSet<String> ITEM_RENDER_TYPES = new LinkedHashSet<>() {{
        for (ItemTransforms.TransformType value : ItemTransforms.TransformType.values()) {
            add(value.name());
        }
    }};
    private static final LinkedHashSet<String> POST_EFFECTS = new LinkedHashSet<>() {{
        for (ResourceLocation effect : GameRendererAccessor.getEffects()) {
            String[] split = effect.getPath().split("/");
            String name = split[split.length - 1];
            add(name.split("\\.")[0]);
        }
    }};
    private static final LinkedHashSet<String> PLAY_STATES = new LinkedHashSet<>() {{
        for (Animation.PlayState value : Animation.PlayState.values()) {
            add(value.name());
        }
    }};
    private static final LinkedHashSet<String> LOOP_MODES = new LinkedHashSet<>() {{
        for (Animation.LoopMode value : Animation.LoopMode.values()) {
            add(value.name());
        }
    }};

    private enum ListDoc {
        KEYBINDS(() -> FiguraListDocs.KEYBINDS, "Keybinds", "keybinds", 2),
        PARENT_TYPES(() -> FiguraListDocs.PARENT_TYPES, "ParentTypes", "parent_types", 2),
        RENDER_TYPES(() -> FiguraListDocs.RENDER_TYPES, "RenderTypes", "render_types", 1),
        TEXTURE_TYPES(() -> FiguraListDocs.TEXTURE_TYPES, "TextureTypes", "texture_types", 1),
        KEY_IDS(() -> new LinkedHashSet<>() {{this.addAll(KeyMappingAccessor.getAll().keySet());}}, "KeyIDs", "key_ids", 2),
        ENTITY_POSES(() -> FiguraListDocs.ENTITY_POSES, "EntityPoses", "entity_poses", 2),
        ITEM_RENDER_TYPES(() -> FiguraListDocs.ITEM_RENDER_TYPES, "ItemRenderTypes", "item_render_types", 1),
        POST_EFFECTS(() -> FiguraListDocs.POST_EFFECTS, "PostEffects", "post_effects", 2),
        PLAY_STATES(() -> FiguraListDocs.PLAY_STATES, "PlayStates", "play_states", 1),
        LOOP_MODES(() -> FiguraListDocs.LOOP_MODES, "LoopModes", "loop_modes", 1);

        private final Supplier<LinkedHashSet<?>> supplier;
        private final String name, id;
        private final int split;

        ListDoc(Supplier<LinkedHashSet<?>> supplier, String name, String id, int split) {
            this.supplier = supplier;
            this.name = name;
            this.id = id;
            this.split = split;
        }

        private JsonElement generateJson(boolean translate) {
            JsonObject object = new JsonObject();

            //list properties
            object.addProperty("name", name);
            object.addProperty("description", translate ? Language.getInstance().getOrDefault(FiguraText.of("docs.list." + id).getString()) : FiguraMod.MOD_ID + "." + "docs.list." + id);

            //list entries
            LinkedHashSet<?> coll = supplier.get();
            if (coll.size() == 0)
                return object;

            JsonArray entries = new JsonArray();
            for (Object element : supplier.get())
                entries.add(element instanceof Component c ? c.getString() : element.toString());

            object.add("entries", entries);
            return object;
        }

        private LiteralArgumentBuilder<FabricClientCommandSource> generateCommand() {
            //command
            LiteralArgumentBuilder<FabricClientCommandSource> command = LiteralArgumentBuilder.literal(id);

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
                                .append(FiguraText.of("docs.list." + id))
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

    // -- doc methods -- //

    public static LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
        //self
        LiteralArgumentBuilder<FabricClientCommandSource> root = LiteralArgumentBuilder.literal("lists");
        root.executes(context -> {
            FiguraMod.sendChatMessage(FiguraDoc.HEADER.copy()
                    .append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.type"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(Component.literal("• ")
                            .append(Component.literal("lists"))
                            .withStyle(ColorUtils.Colors.MAYA_BLUE.style))

                    .append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.description"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.list"))
                            .withStyle(ColorUtils.Colors.MAYA_BLUE.style))
            );
            return 1;
        });

        for (ListDoc value : ListDoc.values())
            root.then(value.generateCommand());

        return root;
    }

    public static JsonElement toJson(boolean translate) {
        JsonArray array = new JsonArray();
        for (ListDoc value : ListDoc.values())
            array.add(value.generateJson(translate));
        return array;
    }
}
