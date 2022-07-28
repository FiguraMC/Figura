package org.moon.figura.lua.docs;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.moon.figura.FiguraMod;
import org.moon.figura.animation.Animation;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.avatars.model.rendertasks.BlockTask;
import org.moon.figura.avatars.model.rendertasks.ItemTask;
import org.moon.figura.avatars.model.rendertasks.RenderTask;
import org.moon.figura.avatars.model.rendertasks.TextTask;
import org.moon.figura.gui.actionwheel.*;
import org.moon.figura.lua.api.EventsAPI;
import org.moon.figura.lua.api.entity.EntityWrapper;
import org.moon.figura.lua.api.entity.LivingEntityWrapper;
import org.moon.figura.lua.api.entity.PlayerEntityWrapper;
import org.moon.figura.lua.api.keybind.FiguraKeybind;
import org.moon.figura.lua.api.model.VanillaGroupPart;
import org.moon.figura.lua.api.model.VanillaModelPart;
import org.moon.figura.lua.api.nameplate.NameplateCustomization;
import org.moon.figura.lua.api.world.BiomeWrapper;
import org.moon.figura.lua.api.world.BlockStateWrapper;
import org.moon.figura.lua.api.world.ItemStackWrapper;
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

    //class name map
    private static final Map<Class<?>, String> NAME_MAP = new HashMap<>() {{
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

    // -- docs generator data -- //

    private static final Map<String, List<Class<?>>> TYPES = new HashMap<>() {{
        put("action_wheel", List.of(
                Page.class,
                Action.class,
                ClickAction.class,
                ToggleAction.class,
                ScrollAction.class
        ));

        put("animations", List.of(
                Animation.class
        ));

        put("nameplate", List.of(
                NameplateCustomization.class
        ));

        put("world", List.of(
                BlockStateWrapper.class,
                ItemStackWrapper.class,
                BiomeWrapper.class
        ));

        put("vanilla_model", List.of(
                VanillaModelPart.class,
                VanillaGroupPart.class
        ));

        put("models", List.of(
                FiguraModelPart.class,
                RenderTask.class,
                BlockTask.class,
                ItemTask.class,
                TextTask.class
        ));

        put("entity", List.of(
                EntityWrapper.class,
                LivingEntityWrapper.class,
                PlayerEntityWrapper.class
        ));

        put("events", List.of(
                EventsAPI.LuaEvent.class
        ));

        put("keybind", List.of(
                FiguraKeybind.class
        ));

        put("vectors", List.of(
                FiguraVec2.class,
                FiguraVec3.class,
                FiguraVec4.class,
                FiguraVec5.class,
                FiguraVec6.class
        ));

        put("matrices", List.of(
                FiguraMat2.class,
                FiguraMat3.class,
                FiguraMat4.class
        ));
    }};
    private static final Map<String, List<FiguraDoc.ClassDoc>> GENERATED_TYPE_DOCS = new HashMap<>();

    private static final List<Class<?>> GLOBALS = List.of(FiguraGlobalsDocs.class, FiguraMathDocs.class);
    private static final List<FiguraDoc.ClassDoc> GENERATED_GLOBALS_DOCS = new ArrayList<>();

    public static void init() {
        //generate type docs
        for (Map.Entry<String, List<Class<?>>> packageEntry : TYPES.entrySet()) {
            for (Class<?> documentedClass : packageEntry.getValue()) {
                FiguraDoc.ClassDoc doc = generateDocFor(documentedClass);
                if (doc != null)
                    GENERATED_TYPE_DOCS.computeIfAbsent(packageEntry.getKey(), s -> new ArrayList<>()).add(doc);
            }
        }

        //generate globals docs
        for (Class<?> global : GLOBALS) {
            FiguraDoc.ClassDoc doc = generateDocFor(global);
            if (doc != null)
                GENERATED_GLOBALS_DOCS.add(doc);
        }
    }

    private static FiguraDoc.ClassDoc generateDocFor(Class<?> documentedClass) {
        if (!documentedClass.isAnnotationPresent(LuaTypeDoc.class))
            return null;

        FiguraDoc.ClassDoc doc = new FiguraDoc.ClassDoc(documentedClass, documentedClass.getAnnotation(LuaTypeDoc.class));
        NAME_MAP.put(documentedClass, doc.name);
        return doc;
    }

    public static String getNameFor(Class<?> clazz) {
        return NAME_MAP.computeIfAbsent(clazz, aClass -> {
            if (clazz.isAnnotationPresent(LuaTypeDoc.class))
                return clazz.getAnnotation(LuaTypeDoc.class).name();
            else
                return clazz.getName();
        });
    }

    // -- commands -- //

    public static LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
        //root
        LiteralArgumentBuilder<FabricClientCommandSource> root = LiteralArgumentBuilder.literal("docs");
        root.executes(context -> FiguraDoc.printRoot());

        //types
        LiteralArgumentBuilder<FabricClientCommandSource> types = LiteralArgumentBuilder.literal("types");
        types.executes(context -> FiguraDoc.printTypesRoot());

        for (Map.Entry<String, List<FiguraDoc.ClassDoc>> entry : GENERATED_TYPE_DOCS.entrySet()) {
            LiteralArgumentBuilder<FabricClientCommandSource> group = LiteralArgumentBuilder.literal(entry.getKey());

            //add children
            for (FiguraDoc.ClassDoc classDoc : entry.getValue())
                group.then(classDoc.getCommand());

            group.executes(context -> FiguraDoc.printGroup(entry.getKey()));
            types.then(group);
        }

        root.then(types);

        //globals
        for (FiguraDoc.ClassDoc classDoc : GENERATED_GLOBALS_DOCS)
            root.then(classDoc.getCommand());

        //list docs
        root.then(FiguraListDocs.getCommand());

        return root;
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> getExportCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> root = LiteralArgumentBuilder.literal("export_docs");
        root.executes(context -> exportDocsFunction(context, true));

        RequiredArgumentBuilder<FabricClientCommandSource, Boolean> e = RequiredArgumentBuilder.argument("translate", BoolArgumentType.bool());
        e.executes(context -> exportDocsFunction(context, BoolArgumentType.getBool(context, "translate")));
        root.then(e);

        return root;
    }

    // -- export -- //

    private static int exportDocsFunction(CommandContext<FabricClientCommandSource> context, boolean translate) {
        try {
            //get path
            Path targetPath = FiguraMod.getFiguraDirectory().resolve("exported_docs.json");

            //create file
            if (!Files.exists(targetPath))
                Files.createFile(targetPath);

            //write file
            FileOutputStream fs = new FileOutputStream(targetPath.toFile());
            fs.write(exportAsJsonString(translate).getBytes());
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
    }

    public static String exportAsJsonString(boolean translate) {
        //root
        JsonObject root = new JsonObject();

        //types
        JsonObject types = new JsonObject();
        for (Map.Entry<String, List<FiguraDoc.ClassDoc>> entry : GENERATED_TYPE_DOCS.entrySet()) {
            JsonArray group = new JsonArray();
            for (FiguraDoc.ClassDoc classDoc : entry.getValue())
                group.add(classDoc.toJson(translate));

            types.add(entry.getKey(), group);
        }
        root.add("types", types);

        //globals
        JsonArray globals = new JsonArray();
        for (FiguraDoc.ClassDoc classDoc : GENERATED_GLOBALS_DOCS)
            globals.add(classDoc.toJson(translate));
        root.add("globals", globals);

        //lists
        root.add("lists", FiguraListDocs.toJson(translate));

        //return as string
        return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(root);
    }
}
