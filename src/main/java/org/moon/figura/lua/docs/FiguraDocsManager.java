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
import org.moon.figura.lua.api.*;
import org.moon.figura.lua.api.entity.EntityWrapper;
import org.moon.figura.lua.api.entity.LivingEntityWrapper;
import org.moon.figura.lua.api.entity.PlayerEntityWrapper;
import org.moon.figura.lua.api.keybind.FiguraKeybind;
import org.moon.figura.lua.api.keybind.KeybindAPI;
import org.moon.figura.lua.api.math.MatricesAPI;
import org.moon.figura.lua.api.math.VectorsAPI;
import org.moon.figura.lua.api.model.VanillaGroupPart;
import org.moon.figura.lua.api.model.VanillaModelAPI;
import org.moon.figura.lua.api.model.VanillaModelPart;
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
                ActionWheelAPI.class,
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
                NameplateAPI.class,
                NameplateCustomization.class
        ));

        put("world", List.of(
                WorldAPI.class,
                BlockStateWrapper.class,
                ItemStackWrapper.class,
                BiomeWrapper.class
        ));

        put("vanilla_model", List.of(
                VanillaModelAPI.class,
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

        put("player", List.of(
                EntityWrapper.class,
                LivingEntityWrapper.class,
                PlayerEntityWrapper.class
        ));

        put("events", List.of(
                EventsAPI.class,
                EventsAPI.LuaEvent.class
        ));

        put("keybind", List.of(
                KeybindAPI.class,
                FiguraKeybind.class
        ));

        put("vectors", List.of(
                VectorsAPI.class,
                FiguraVec2.class,
                FiguraVec3.class,
                FiguraVec4.class,
                FiguraVec5.class,
                FiguraVec6.class
        ));

        put("matrices", List.of(
                MatricesAPI.class,
                FiguraMat2.class,
                FiguraMat3.class,
                FiguraMat4.class
        ));

        put("client", List.of(
                ClientAPI.class
        ));

        put("host", List.of(
                HostAPI.class
        ));

        put("meta", List.of(
                MetaAPI.class
        ));

        put("particle", List.of(
                ParticleAPI.class
        ));

        put("sound", List.of(
                SoundAPI.class
        ));

        put("renderer", List.of(
                RendererAPI.class
        ));
    }};
    private static final Map<String, List<FiguraDoc>> GENERATED_GLOBALS = new HashMap<>();

    private static final FiguraDoc.ClassDoc GENERATED_GLOBAL = generateDocFor(FiguraGlobalsDocs.class);

    private static final List<Class<?>> LUA_LIBRARIES = List.of(
            FiguraMathDocs.class
    );
    private static final List<FiguraDoc> GENERATED_LUA_LIB = new ArrayList<>();

    public static void init() {
        //generate type docs
        for (Map.Entry<String, List<Class<?>>> packageEntry : TYPES.entrySet()) {
            for (Class<?> documentedClass : packageEntry.getValue()) {
                FiguraDoc.ClassDoc doc = generateDocFor(documentedClass);
                if (doc != null)
                    GENERATED_GLOBALS.computeIfAbsent(packageEntry.getKey(), s -> new ArrayList<>()).add(doc);
            }
        }

        //generate library overloads
        for (Class<?> lib : LUA_LIBRARIES) {
            FiguraDoc.ClassDoc libDoc = generateDocFor(lib);
            if (libDoc != null)
                GENERATED_LUA_LIB.add(libDoc);
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

        //globals
        LiteralArgumentBuilder<FabricClientCommandSource> globals = GENERATED_GLOBAL == null ? LiteralArgumentBuilder.literal("globals") : GENERATED_GLOBAL.getCommand();

        for (Map.Entry<String, List<FiguraDoc>> entry : GENERATED_GLOBALS.entrySet()) {
            LiteralArgumentBuilder<FabricClientCommandSource> group = LiteralArgumentBuilder.literal(entry.getKey());

            //add children
            for (FiguraDoc doc : entry.getValue())
                group.then(doc.getCommand());

            group.executes(context -> FiguraDoc.printGlobal(entry.getKey()));
            globals.then(group);
        }

        root.then(globals);

        //library overrides
        for (FiguraDoc figuraDoc : GENERATED_LUA_LIB)
            root.then(figuraDoc.getCommand());

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

        //globals
        JsonObject globals = new JsonObject();

        if (GENERATED_GLOBAL != null) {
            JsonArray prop = new JsonArray();

            for (FiguraDoc.FieldDoc field : GENERATED_GLOBAL.documentedFields)
                prop.add(field.toJson(translate));
            for (FiguraDoc.MethodDoc method : GENERATED_GLOBAL.documentedMethods)
                prop.add(method.toJson(translate));

            globals.add("globalProperties", prop);
        }

        for (Map.Entry<String, List<FiguraDoc>> entry : GENERATED_GLOBALS.entrySet()) {
            JsonArray group = new JsonArray();

            for (FiguraDoc doc : entry.getValue())
                group.add(doc.toJson(translate));

            globals.add(entry.getKey(), group);
        }

        root.add("globals", globals);

        //library overrides
        for (FiguraDoc figuraDoc : GENERATED_LUA_LIB)
            root.add(figuraDoc.name, figuraDoc.toJson(translate));

        //lists
        root.add("lists", FiguraListDocs.toJson(translate));

        //return as string
        return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(root);
    }
}
