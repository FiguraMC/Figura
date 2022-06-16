package org.moon.figura.lua.docs;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.gui.actionwheel.Action;
import org.moon.figura.gui.actionwheel.Page;
import org.moon.figura.lua.api.*;
import org.moon.figura.lua.api.entity.EntityWrapper;
import org.moon.figura.lua.api.entity.LivingEntityWrapper;
import org.moon.figura.lua.api.entity.PlayerEntityWrapper;
import org.moon.figura.lua.api.keybind.FiguraKeybind;
import org.moon.figura.lua.api.keybind.KeybindAPI;
import org.moon.figura.lua.api.math.MatricesAPI;
import org.moon.figura.lua.api.math.VectorsAPI;
import org.moon.figura.lua.api.model.VanillaModelAPI;
import org.moon.figura.lua.api.nameplate.NameplateAPI;
import org.moon.figura.lua.api.nameplate.NameplateCustomization;
import org.moon.figura.lua.api.world.BiomeWrapper;
import org.moon.figura.lua.api.world.BlockStateWrapper;
import org.moon.figura.lua.api.world.ItemStackWrapper;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.lua.types.LuaFunction;
import org.moon.figura.lua.types.LuaTable;
import org.moon.figura.math.matrix.FiguraMat2;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.*;
import org.moon.figura.utils.FiguraText;
import org.terasology.jnlua.JavaFunction;
import org.terasology.jnlua.TypedJavaObject;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FiguraDocsManager {

    /**
     * Update this list of classes manually. The docs manager will scan through
     * all the classes in this static set, and generate documentation for
     * all of them, based on the annotations of members within.
     * Entries of the map are organized by group names, because otherwise there
     * will be too many different APIs and autocomplete will get cluttered.
     */
    public static final Map<String, List<Class<?>>> DOCUMENTED_CLASSES = new HashMap<>() {{

        //Globals. Group name is an empty string, meaning they're not in any group at all.
        put("", List.of(
                FiguraGlobalsDocs.class
        ));

        put("action_wheel", List.of(
                ActionWheelAPI.class,
                Page.class,
                Action.class
        ));

        //misc (only 1 type)
        put("misc", List.of(
                ClientAPI.class,
                HostAPI.class,
                MetaAPI.class,
                RendererAPI.class,
                SoundAPI.class,
                ParticleAPI.class
        ));

        //nameplate
        put("nameplate", List.of(
                NameplateAPI.class,
                NameplateCustomization.class
        ));

        //World classes
        put("world", List.of(
                WorldAPI.class,
                BlockStateWrapper.class,
                ItemStackWrapper.class,
                BiomeWrapper.class
        ));

        //Model classes
        put("model", List.of(
                FiguraModelPart.class,
                VanillaModelAPI.class,
                VanillaModelAPI.VanillaModelPart.class
        ));

        //Entity classes
        put("entity", List.of(
                EntityWrapper.class,
                LivingEntityWrapper.class,
                PlayerEntityWrapper.class
        ));

        //Events
        put("event", List.of(
                EventsAPI.class,
                EventsAPI.LuaEvent.class
        ));

        put("keybind", List.of(
                KeybindAPI.class,
                FiguraKeybind.class
        ));

        //Math classes, including vectors and matrices
        put("math", List.of(
                //Vectors
                VectorsAPI.class,
                FiguraVec2.class,
                FiguraVec3.class,
                FiguraVec4.class,
                FiguraVec5.class,
                FiguraVec6.class,
                //Matrices
                MatricesAPI.class,
                FiguraMat2.class,
                FiguraMat3.class,
                FiguraMat4.class,
                //General math functions
                FiguraMathDocs.class
        ));

    }};

    public static final Map<Class<?>, String> NAME_MAP = new HashMap<>() {{
        //Built in type names, even for things that don't have docs
        put(Double.class, "Number");
        put(double.class, "Number");
        put(Float.class, "Number");
        put(float.class, "Number");

        put(Integer.class, "Integer");
        put(int.class, "Integer");
        put(Long.class, "Integer");
        put(long.class, "Integer");

        put(void.class, "nil");

        put(String.class, "String");

        put(Object.class, "AnyType"); //not sure if best name //Fran - yes it is
        put(TypedJavaObject.class, "Userdata");

        put(Boolean.class, "Boolean");
        put(boolean.class, "Boolean");

        //Lua things
        put(LuaFunction.class, "Function");
        put(JavaFunction.class, "Function");
        put(LuaTable.class, "Table");

        //Figura types
        put(FiguraVector.class, "Vector");
    }};

    private static final Map<String, List<FiguraDoc.ClassDoc>> GENERATED_CLASS_DOCS = new HashMap<>();

    public static void init() {
        //generate all docs
        for (Map.Entry<String, List<Class<?>>> packageEntry : DOCUMENTED_CLASSES.entrySet()) {
            for (Class<?> documentedClass : packageEntry.getValue()) {
                if (documentedClass.isAnnotationPresent(LuaTypeDoc.class)) {
                    FiguraDoc.ClassDoc doc = new FiguraDoc.ClassDoc(documentedClass, documentedClass.getAnnotation(LuaTypeDoc.class));
                    GENERATED_CLASS_DOCS.computeIfAbsent(packageEntry.getKey(), (key) -> new ArrayList<>()).add(doc);
                    NAME_MAP.put(documentedClass, doc.name);
                }
            }
        }
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
        //root
        LiteralArgumentBuilder<FabricClientCommandSource> root = LiteralArgumentBuilder.literal("docs");
        root.executes(context -> FiguraDoc.printRoot());

        //children
        for (Map.Entry<String, List<FiguraDoc.ClassDoc>> entry : GENERATED_CLASS_DOCS.entrySet()) {
            LiteralArgumentBuilder<FabricClientCommandSource> group = entry.getKey().length() > 0 ? LiteralArgumentBuilder.literal(entry.getKey()) : root;

            //add children
            for (FiguraDoc.ClassDoc classDoc : entry.getValue())
                group.then(classDoc.getCommand());

            //add group to root if it's not the root itself
            if (group != root) {
                group.executes(context -> FiguraDoc.printGroup(entry.getKey()));
                root.then(group);
            }
        }

        return root;
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> getExportCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> root = LiteralArgumentBuilder.literal("export_docs");
        root.executes(context -> {
            try {
                //get path
                Path targetPath = FiguraMod.getCacheDirectory().resolve("exported_docs.json");

                //create file
                if (!Files.exists(targetPath))
                    Files.createFile(targetPath);

                //write file
                FileOutputStream fs = new FileOutputStream(targetPath.toFile());
                fs.write(exportAsJsonString().getBytes());
                fs.close();

                //feedback
                context.getSource().sendFeedback(
                        FiguraText.of("command.docs_export.success")
                                .append(" ")
                                .append(FiguraText.of("command.click_to_open")
                                        .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, targetPath.toFile().toString())).withUnderlined(true))
                                )
                );
                return 1;
            } catch (Exception e) {
                context.getSource().sendError(FiguraText.of("command.docs_export.error"));
                FiguraMod.LOGGER.error("Failed to export docs!", e);
                return 0;
            }
        });

        return root;
    }

    public static String exportAsJsonString() {
        //root
        JsonObject root = new JsonObject();

        //children
        for (Map.Entry<String, List<FiguraDoc.ClassDoc>> entry : GENERATED_CLASS_DOCS.entrySet()) {
            JsonArray group = new JsonArray();
            for (FiguraDoc.ClassDoc classDoc : entry.getValue())
                group.add(classDoc.toJson());

            root.add(entry.getKey(), group);
        }

        //return as string
        return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(root);
    }
}
