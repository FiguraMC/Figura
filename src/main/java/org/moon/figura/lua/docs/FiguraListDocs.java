package org.moon.figura.lua.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import org.moon.figura.FiguraMod;
import org.moon.figura.animation.Animation;
import org.moon.figura.avatars.model.ParentType;
import org.moon.figura.avatars.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.avatars.model.rendering.texture.RenderTypes;
import org.moon.figura.mixin.input.KeyMappingAccessor;
import org.moon.figura.mixin.render.GameRendererAccessor;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;

import java.util.*;
import java.util.function.Supplier;

/**
 * Adds docs for functions that have specific set of String names.
 */
public class FiguraListDocs {

    //-- types --//

    public static final LinkedHashSet<String> KEYBINDS = new LinkedHashSet<>();
    private static final LinkedHashMap<String, List<String>> PARENT_TYPES = new LinkedHashMap<>() {{
        for (ParentType value : ParentType.values())
            put(value.name(), Arrays.asList(value.aliases));
    }};
    private static final LinkedHashSet<String> RENDER_TYPES = new LinkedHashSet<>() {{
        for (RenderTypes value : RenderTypes.values())
            add(value.name());
    }};
    private static final LinkedHashSet<String> TEXTURE_TYPES = new LinkedHashSet<>() {{
        for (FiguraTextureSet.OverrideType value : FiguraTextureSet.OverrideType.values())
            add(value.name());
    }};
    private static final LinkedHashSet<String> ENTITY_POSES = new LinkedHashSet<>() {{
        for (Pose value : Pose.values())
            add(value.name());
    }};
    private static final LinkedHashSet<String> ITEM_RENDER_TYPES = new LinkedHashSet<>() {{
        for (ItemTransforms.TransformType value : ItemTransforms.TransformType.values())
            add(value.name());
    }};
    private static final LinkedHashSet<String> POST_EFFECTS = new LinkedHashSet<>() {{
        for (ResourceLocation effect : GameRendererAccessor.getEffects()) {
            String[] split = effect.getPath().split("/");
            String name = split[split.length - 1];
            add(name.split("\\.")[0]);
        }
    }};
    private static final LinkedHashSet<String> PLAY_STATES = new LinkedHashSet<>() {{
        for (Animation.PlayState value : Animation.PlayState.values())
            add(value.name());
    }};
    private static final LinkedHashSet<String> LOOP_MODES = new LinkedHashSet<>() {{
        for (Animation.LoopMode value : Animation.LoopMode.values())
            add(value.name());
    }};
    private static final LinkedHashMap<String, List<String>> COLORS = new LinkedHashMap<>() {{
        for (ColorUtils.Colors value : ColorUtils.Colors.values())
            put(value.name(), Arrays.asList(value.alias));
    }};

    private enum ListDoc {
        KEYBINDS(() -> FiguraListDocs.KEYBINDS, "Keybinds", "keybinds", 2),
        PARENT_TYPES(() -> FiguraListDocs.PARENT_TYPES, "ParentTypes", "parent_types", 1),
        RENDER_TYPES(() -> FiguraListDocs.RENDER_TYPES, "RenderTypes", "render_types", 1),
        TEXTURE_TYPES(() -> FiguraListDocs.TEXTURE_TYPES, "TextureTypes", "texture_types", 1),
        KEY_IDS(() -> new LinkedHashSet<>() {{this.addAll(KeyMappingAccessor.getAll().keySet());}}, "KeyIDs", "key_ids", 2),
        ENTITY_POSES(() -> FiguraListDocs.ENTITY_POSES, "EntityPoses", "entity_poses", 2),
        ITEM_RENDER_TYPES(() -> FiguraListDocs.ITEM_RENDER_TYPES, "ItemRenderTypes", "item_render_types", 1),
        POST_EFFECTS(() -> FiguraListDocs.POST_EFFECTS, "PostEffects", "post_effects", 2),
        PLAY_STATES(() -> FiguraListDocs.PLAY_STATES, "PlayStates", "play_states", 1),
        LOOP_MODES(() -> FiguraListDocs.LOOP_MODES, "LoopModes", "loop_modes", 1),
        COLORS(() -> FiguraListDocs.COLORS, "Colors", "colors", 1);

        private final Supplier<Object> supplier;
        private final String name, id;
        private final int split;

        ListDoc(Supplier<Object> supplier, String name, String id, int split) {
            this.supplier = supplier;
            this.name = name;
            this.id = id;
            this.split = split;
        }

        private Collection<?> get() {
            Object obj = supplier.get();
            if (obj instanceof LinkedHashSet<?> set)
                return set;
            else if (obj instanceof Map<?, ?> map)
                return map.entrySet();
            else
                throw new UnsupportedOperationException("Invalid object " + obj);
        }

        private JsonElement generateJson(boolean translate) {
            JsonObject object = new JsonObject();

            //list properties
            object.addProperty("name", name);
            object.addProperty("description", translate ? Language.getInstance().getOrDefault(new FiguraText("docs.enum." + id).getString()) : FiguraMod.MOD_ID + "." + "docs.enum." + id);

            //list entries
            Collection<?> coll = get();
            if (coll.size() == 0)
                return object;

            JsonArray entries = new JsonArray();
            for (Object o : coll) {
                if (o instanceof Map.Entry e) {
                    entries.add(e.getKey().toString());
                    for (String s : (List<String>) e.getValue())
                        entries.add(s);
                } else {
                    entries.add(o.toString());
                }
            }

            object.add("entries", entries);
            return object;
        }

        private LiteralArgumentBuilder<FabricClientCommandSource> generateCommand() {
            //command
            LiteralArgumentBuilder<FabricClientCommandSource> command = LiteralArgumentBuilder.literal(id);

            //display everything
            command.executes(context -> {
                Collection<?> coll = get();
                if (coll.size() == 0) {
                    FiguraMod.sendChatMessage(new FiguraText("docs.enum.empty"));
                    return 0;
                }

                MutableComponent text = FiguraDoc.HEADER.copy()
                        .append("\n\n")
                        .append(new TextComponent("• ")
                                .append(new FiguraText("docs.text.description"))
                                .append(":")
                                .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                        .append("\n\t")
                        .append(new TextComponent("• ")
                                .append(new FiguraText("docs.enum." + id))
                                .withStyle(ColorUtils.Colors.MAYA_BLUE.style))
                        .append("\n\n")
                        .append(new TextComponent("• ")
                                .append(new FiguraText("docs.text.entries"))
                                .append(":")
                                .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style));

                int i = 0;
                for (Object o : coll) {
                    MutableComponent component;

                    if (o instanceof Map.Entry e) {
                        component = new TextComponent(e.getKey().toString()).withStyle(ChatFormatting.WHITE);
                        for (String s : (List<String>) e.getValue()) {
                            component.append(new TextComponent(" | ").withStyle(ChatFormatting.YELLOW))
                                    .append(new TextComponent(s).withStyle(ChatFormatting.GRAY));
                        }
                    } else {
                        component = new TextComponent(o.toString()).withStyle(ChatFormatting.WHITE);
                    }

                    text.append(i % split == 0 ? "\n\t" : "\t");
                    text.append(new TextComponent("• ").withStyle(ChatFormatting.YELLOW)).append(component);
                    i++;
                }

                FiguraMod.sendChatMessage(text);
                return 1;
            });

            //add collection as child for easy navigation
            Collection<?> coll = get();
            for (Object o : coll) {
                String text = o instanceof Map.Entry e ? e.getKey().toString() : o.toString();
                LiteralArgumentBuilder<FabricClientCommandSource> entry = LiteralArgumentBuilder.literal(text);
                entry.executes(context -> {
                    FiguraMod.sendChatMessage(new TextComponent(text).withStyle(ColorUtils.Colors.FRAN_PINK.style));
                    return 1;
                });

                if (o instanceof Map.Entry e) {
                    for (String s : (List<String>) e.getValue()) {
                        LiteralArgumentBuilder<FabricClientCommandSource> child = LiteralArgumentBuilder.literal(s);
                        child.executes(context -> {
                            FiguraMod.sendChatMessage(new TextComponent(s).withStyle(ColorUtils.Colors.FRAN_PINK.style));
                            return 1;
                        });
                        entry.then(child);
                    }
                }

                command.then(entry);
            }

            //return
            return command;
        }
    }

    // -- doc methods -- //

    public static LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
        //self
        LiteralArgumentBuilder<FabricClientCommandSource> root = LiteralArgumentBuilder.literal("enums");
        root.executes(context -> {
            FiguraMod.sendChatMessage(FiguraDoc.HEADER.copy()
                    .append("\n\n")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs.text.type"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(new TextComponent("• ")
                            .append(new TextComponent("enumerators"))
                            .withStyle(ColorUtils.Colors.MAYA_BLUE.style))

                    .append("\n\n")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs.text.description"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs.enum"))
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
