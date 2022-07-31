package org.moon.figura.lua.docs;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.moon.figura.FiguraMod;
import org.moon.figura.animation.Animation;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.avatars.model.rendertasks.BlockTask;
import org.moon.figura.avatars.model.rendertasks.ItemTask;
import org.moon.figura.avatars.model.rendertasks.RenderTask;
import org.moon.figura.avatars.model.rendertasks.TextTask;
import org.moon.figura.gui.actionwheel.*;
import org.moon.figura.math.matrix.FiguraMat2;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.*;
import org.moon.figura.lua.api.*;
import org.moon.figura.lua.api.entity.EntityAPI;
import org.moon.figura.lua.api.entity.LivingEntityAPI;
import org.moon.figura.lua.api.entity.PlayerAPI;
import org.moon.figura.lua.api.event.EventsAPI;
import org.moon.figura.lua.api.event.LuaEvent;
import org.moon.figura.lua.api.keybind.FiguraKeybind;
import org.moon.figura.lua.api.keybind.KeybindAPI;
import org.moon.figura.lua.api.math.MatricesAPI;
import org.moon.figura.lua.api.math.VectorsAPI;
import org.moon.figura.lua.api.nameplate.NameplateAPI;
import org.moon.figura.lua.api.nameplate.NameplateCustomization;
import org.moon.figura.lua.api.vanilla_model.VanillaGroupPart;
import org.moon.figura.lua.api.vanilla_model.VanillaModelAPI;
import org.moon.figura.lua.api.vanilla_model.VanillaModelPart;
import org.moon.figura.lua.api.world.BiomeAPI;
import org.moon.figura.lua.api.world.BlockStateAPI;
import org.moon.figura.lua.api.world.ItemStackAPI;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.utils.FiguraText;

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
        put(LuaUserdata.class, "Userdata");

        put(Boolean.class, "Boolean");
        put(boolean.class, "Boolean");

        //Lua things
        put(LuaFunction.class, "Function");
        put(LuaTable.class, "Table");

        //Figura types
        put(FiguraVector.class, "Vector");
    }};

    // -- docs generator data -- //

    private static final Map<String, List<Class<?>>> GLOBAL_CHILDREN = new HashMap<>() {{
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
                BiomeAPI.class,
                BlockStateAPI.class,
                ItemStackAPI.class
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
                EntityAPI.class,
                LivingEntityAPI.class,
                PlayerAPI.class
        ));

        put("events", List.of(
                EventsAPI.class,
                LuaEvent.class
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
    private static final Map<String, List<FiguraDoc>> GENERATED_CHILDREN = new HashMap<>();

    private static FiguraDoc.ClassDoc global;

    private static final List<Class<?>> LUA_LIB_OVERRIDES = List.of(
            FiguraMathDocs.class
    );
    private static final List<FiguraDoc> GENERATED_LIB_OVERRIDES = new ArrayList<>();

    public static void init() {
        //generate children override
        for (Map.Entry<String, List<Class<?>>> packageEntry : GLOBAL_CHILDREN.entrySet()) {
            for (Class<?> documentedClass : packageEntry.getValue()) {
                FiguraDoc.ClassDoc doc = generateDocFor(documentedClass);
                if (doc != null)
                    GENERATED_CHILDREN.computeIfAbsent(packageEntry.getKey(), s -> new ArrayList<>()).add(doc);
            }
        }

        //generate standard libraries overrides
        for (Class<?> lib : LUA_LIB_OVERRIDES) {
            FiguraDoc.ClassDoc libDoc = generateDocFor(lib);
            if (libDoc != null)
                GENERATED_LIB_OVERRIDES.add(libDoc);
        }

        //generate globals
        Class<?> globalClass = FiguraGlobalsDocs.class;
        global = new FiguraDoc.ClassDoc(globalClass, globalClass.getAnnotation(LuaTypeDoc.class), GENERATED_CHILDREN);
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
        LiteralArgumentBuilder<FabricClientCommandSource> globals = global == null ? LiteralArgumentBuilder.literal("globals") : global.getCommand();
        root.then(globals);

        //library overrides
        for (FiguraDoc figuraDoc : GENERATED_LIB_OVERRIDES)
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
        JsonObject globals = global == null ? new JsonObject() : global.toJson(translate);
        root.add("globals", globals);

        //library overrides
        for (FiguraDoc figuraDoc : GENERATED_LIB_OVERRIDES)
            root.add(figuraDoc.name, figuraDoc.toJson(translate));

        //lists
        root.add("lists", FiguraListDocs.toJson(translate));

        //return as string
        return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(root);
    }
}
