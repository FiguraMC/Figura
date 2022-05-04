package org.moon.figura.lua.docs;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.math.vector.FiguraVec3;

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
       add(FiguraVec3.class);
    }};

    private static final List<ClassDoc> GENERATED_CLASS_DOCS = new ArrayList<>();
    private static final Map<Class<?>, String> NAME_MAP = new HashMap<>();

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
         * Right here Fran!
         */
        public void print() {
            System.out.println(name);
        }
    }

    private static class MethodDoc {

        public final String name;
        public final Class<?> returnType;
        public final String description;
        public final Class<?>[][] parameters;

        public MethodDoc(Method method) {
            name = method.getName();
            LuaMethodDoc methodDoc = method.getAnnotation(LuaMethodDoc.class);
            returnType = methodDoc.returnType();
            description = methodDoc.description();
            LuaParameterList[] parameterLists = methodDoc.parameterTypeOptions();
            parameters = new Class[parameterLists.length][];
            for (int i = 0; i < parameterLists.length; i++)
                parameters[i] = parameterLists[i].types();
        }

        /**
         * Right here Fran!
         */
        public void print() {
            System.out.println(name);
        }
    }

    public static void initialize(CommandDispatcher<FabricClientCommandSource> dispatcher) {
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
