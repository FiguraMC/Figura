package org.moon.figura.lua.docs;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.math.MatricesAPI;
import org.moon.figura.lua.api.math.VectorsAPI;
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
     */
    public static List<Class<?>> DOCUMENTED_CLASSES = new ArrayList<>() {{


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
    }};

    private static final List<ClassDoc> GENERATED_CLASS_DOCS = new ArrayList<>();
    private static final Map<Class<?>, String> NAME_MAP = new HashMap<>() {{
        //Default java values
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
                if (method.isAnnotationPresent(LuaWhitelist.class)
                && method.isAnnotationPresent(LuaMethodDoc.class)) {
                    documentedMethods.add(new MethodDoc(method));
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
                    else
                        builder.append("): ");
                }
                builder.append("Returns " + NAME_MAP.get(returnTypes[i])).append("\n");
            }
            builder.append("DESCRIPTION:\n\t");
            builder.append(description);
            System.out.println(builder);
        }
    }

    public static void init(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        //Initialize all the ClassDoc instances
        for (Class<?> documentedClass : DOCUMENTED_CLASSES) {
            if (documentedClass.isAnnotationPresent(LuaWhitelist.class)
                    && documentedClass.isAnnotationPresent(LuaTypeDoc.class)) {
                ClassDoc doc = new ClassDoc(documentedClass);
                GENERATED_CLASS_DOCS.add(doc);
            }
        }

        initCommand(dispatcher);
    }

    private static void initCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        //TODO: move "figura" to another class for easy registration of other sub-commands
        LiteralArgumentBuilder<FabricClientCommandSource> root = LiteralArgumentBuilder.literal("figura");

        LiteralArgumentBuilder<FabricClientCommandSource> docs = LiteralArgumentBuilder.literal("docs");
        for (ClassDoc classDoc : GENERATED_CLASS_DOCS) {
            LiteralArgumentBuilder<FabricClientCommandSource> typeBranch = LiteralArgumentBuilder.literal(classDoc.name);
            typeBranch.executes(context -> {classDoc.print(); return 0;});
            for (MethodDoc methodDoc : classDoc.documentedMethods) {
                LiteralArgumentBuilder<FabricClientCommandSource> methodBranch = LiteralArgumentBuilder.literal(methodDoc.name);
                methodBranch.executes(context -> {methodDoc.print(); return 0;});
                typeBranch.then(methodBranch);
            }
            docs.then(typeBranch);
        }
        root.then(docs);

        //TODO: Same for this
        dispatcher.register(root);
    }
}
