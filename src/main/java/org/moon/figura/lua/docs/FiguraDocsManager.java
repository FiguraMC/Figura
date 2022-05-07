package org.moon.figura.lua.docs;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.EventsAPI;
import org.moon.figura.lua.api.entity.EntityWrapper;
import org.moon.figura.lua.api.entity.LivingEntityWrapper;
import org.moon.figura.lua.api.entity.PlayerEntityWrapper;
import org.moon.figura.lua.api.math.MatricesAPI;
import org.moon.figura.lua.api.math.VectorsAPI;
import org.moon.figura.lua.api.model.VanillaModelAPI;
import org.moon.figura.lua.types.LuaFunction;
import org.moon.figura.lua.types.LuaTable;
import org.moon.figura.math.matrix.FiguraMat2;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FiguraDocsManager {
    /**
     * Update this list of classes manually. The docs manager will scan through
     * all the classes in this static set, and generate documentation for
     * all of them, based on the annotations of members within.
     * Entries of the map are group names, because otherwise there will be too
     * many different APIs and autocomplete will get cluttered.
     */
    public static Map<String, List<Class<?>>> DOCUMENTED_CLASSES = new HashMap<>() {{

        //Model classes
        put("model", new ArrayList<>() {{
            add(FiguraModelPart.class);
            add(VanillaModelAPI.class);
            add(VanillaModelAPI.VanillaModelPart.class);
        }});

        //Entity classes
        put("entity", new ArrayList<>() {{
            add(EntityWrapper.class);
            add(LivingEntityWrapper.class);
            add(PlayerEntityWrapper.class);
        }});

        //Events
        put("event", new ArrayList<>() {{
            add(EventsAPI.class);
            add(EventsAPI.LuaEvent.class);
        }});

        //Math classes, including vectors and matrices
        put("math", new ArrayList<>() {{
            //Vectors
            add(VectorsAPI.class);
            add(FiguraVec2.class);
            add(FiguraVec3.class);
            add(FiguraVec4.class);
            add(FiguraVec5.class);
            add(FiguraVec6.class);
            //Matrices
            add(MatricesAPI.class);
            add(FiguraMat2.class);
            add(FiguraMat3.class);
            add(FiguraMat4.class);
        }});


    }};

    private static final Map<String, List<ClassDoc>> GENERATED_CLASS_DOCS = new HashMap<>();
    private static final Map<Class<?>, String> NAME_MAP = new HashMap<>() {{
        //Built in type names, even for things that don't have docs
        put(Double.class, "number");
        put(double.class, "number");
        put(Float.class, "number");
        put(float.class, "number");

        put(Integer.class, "integer");
        put(int.class, "integer");
        put(Long.class, "integer");
        put(long.class, "integer");

        put(void.class, "None");

        put(String.class, "string");

        put(Boolean.class, "boolean");
        put(boolean.class, "boolean");

        //Lua things
        put(LuaFunction.class, "function");
        put(LuaTable.class, "table");
    }};

    private static class ClassDoc {

        public final String name;
        public final String description;
        public final List<MethodDoc> documentedMethods;

        public ClassDoc(Class<?> clazz) {
            LuaTypeDoc typeDoc = clazz.getAnnotation(LuaTypeDoc.class);
            name = typeDoc.name();
            description = typeDoc.description();
            NAME_MAP.put(clazz, name);
            documentedMethods = new ArrayList<>(clazz.getMethods().length);
            for (Method method : clazz.getMethods()) {
                if (method.isAnnotationPresent(LuaMethodDoc.class)) {
                    if (method.isAnnotationPresent(LuaWhitelist.class)) {
                        documentedMethods.add(new MethodDoc(method));
                    } else {
                        FiguraMod.LOGGER.warn("Docs manager found a method that " +
                                "had documentation, but wasn't whitelisted: " + method.getName() +
                                " in " + method.getDeclaringClass().getName());
                    }
                }
            }
        }

        /**
         * Over here Fran!
         */
        public void print() {
            StringBuilder builder = new StringBuilder();
            builder.append("\nTYPE DOC FOR TYPE: ");
            builder.append(name);
            builder.append("\nDESCRIPTION:\n\t");
            builder.append(description);
            System.out.println(builder);
        }
    }

    private static class MethodDoc {

        public final String name;
        public final String description;

        public final Class<?>[][] parameterTypes;
        public final String[][] parameterNames;
        public final Class<?>[] returnTypes;

        public MethodDoc(Method method) {
            name = method.getName();
            LuaMethodDoc methodDoc = method.getAnnotation(LuaMethodDoc.class);
            description = methodDoc.description();
            LuaFunctionOverload[] overloads = methodDoc.overloads();
            parameterTypes = new Class[overloads.length][];
            parameterNames = new String[overloads.length][];
            returnTypes = new Class[overloads.length];
            for (int i = 0; i < overloads.length; i++) {
                parameterTypes[i] = overloads[i].argumentTypes();
                parameterNames[i] = overloads[i].argumentNames();
                returnTypes[i] = overloads[i].returnType();
            }
        }

        /**
         * Over here Fran!
         * This format is just temporary, printing out to console.
         * Work your magic and get whatever colors or styles you want printed in chat :D
         */
        public void print() {
            StringBuilder builder = new StringBuilder();
            builder.append("\nMETHOD DOC FOR METHOD: ");
            builder.append(name);
            builder.append("\nSYNTAX:\n");
            for (int i = 0; i < parameterTypes.length; i++) {
                builder.append("\t").append(name).append("(");
                for (int j = 0; j < parameterTypes[i].length; j++) {
                    String typeName = NAME_MAP.get(parameterTypes[i][j]);
                    builder.append(typeName).append(" ").append(parameterNames[i][j]);
                    if (j != parameterTypes[i].length-1)
                        builder.append(", ");

                }
                builder.append("): Returns ").append(NAME_MAP.get(returnTypes[i])).append("\n");
            }
            builder.append("DESCRIPTION:\n\t");
            builder.append(description);
            System.out.println(builder);
        }
    }

    public static void init(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        //Initialize all the ClassDoc instances
        for (Map.Entry<String, List<Class<?>>> packageEntry : DOCUMENTED_CLASSES.entrySet())
            for (Class<?> documentedClass : packageEntry.getValue())
                if (documentedClass.isAnnotationPresent(LuaWhitelist.class)
                        && documentedClass.isAnnotationPresent(LuaTypeDoc.class))
                    GENERATED_CLASS_DOCS.computeIfAbsent(
                            packageEntry.getKey(), (key) -> new ArrayList<>()
                    ).add(new ClassDoc(documentedClass));

        initCommand(dispatcher);
    }

    private static void initCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        //TODO: move "figura" to another class for easy registration of other sub-commands
        LiteralArgumentBuilder<FabricClientCommandSource> root = LiteralArgumentBuilder.literal("figura");

        LiteralArgumentBuilder<FabricClientCommandSource> docs = LiteralArgumentBuilder.literal("docs");
        for (Map.Entry<String, List<ClassDoc>> entry : GENERATED_CLASS_DOCS.entrySet()) {
            LiteralArgumentBuilder<FabricClientCommandSource> group = LiteralArgumentBuilder.literal(entry.getKey());
            for (ClassDoc classDoc : entry.getValue()) {
                LiteralArgumentBuilder<FabricClientCommandSource> typeBranch = LiteralArgumentBuilder.literal(classDoc.name);
                typeBranch.executes(context -> {classDoc.print(); return 0;});
                for (MethodDoc methodDoc : classDoc.documentedMethods) {
                    LiteralArgumentBuilder<FabricClientCommandSource> methodBranch = LiteralArgumentBuilder.literal(methodDoc.name);
                    methodBranch.executes(context -> {methodDoc.print(); return 0;});
                    typeBranch.then(methodBranch);
                }
                group.then(typeBranch);
            }
            docs.then(group);
        }
        root.then(docs);

        //TODO: Same for this
        dispatcher.register(root);
    }
}
