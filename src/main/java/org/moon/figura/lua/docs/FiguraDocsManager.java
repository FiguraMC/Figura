package org.moon.figura.lua.docs;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.model.FiguraModelPart;
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
import org.moon.figura.utils.ColorUtils.Colors;
import org.moon.figura.utils.FiguraText;
import org.terasology.jnlua.JavaFunction;
import org.terasology.jnlua.TypedJavaObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class FiguraDocsManager {

    //print header
    private static final MutableComponent HEADER = TextComponent.EMPTY.copy().withStyle(Colors.FRAN_PINK.style)
            .append(new TextComponent("\n•*+•* ").append(new FiguraText()).append(" Docs *•+*•")
                    .withStyle(ChatFormatting.UNDERLINE));

    /**
     * Update this list of classes manually. The docs manager will scan through
     * all the classes in this static set, and generate documentation for
     * all of them, based on the annotations of members within.
     * Entries of the map are organized by group names, because otherwise there
     * will be too many different APIs and autocomplete will get cluttered.
     */
    public static Map<String, List<Class<?>>> DOCUMENTED_CLASSES = new HashMap<>() {{

        //Globals. Group name is an empty string, meaning they're not in any group at all.
        put("", List.of(
                FiguraGlobalsDocs.class
        ));

        //client
        put("client", List.of(
                ClientAPI.class
        ));

        //host
        put("host", List.of(
                HostAPI.class
        ));

        //meta
        put("meta", List.of(
                MetaAPI.class
        ));

        //sound
        put("sound", List.of(
                SoundAPI.class
        ));

        //particle
        put("particle", List.of(
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
                FiguraMat4.class
        ));

    }};

    private static final Map<String, List<ClassDoc>> GENERATED_CLASS_DOCS = new HashMap<>();
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

        put(Object.class, "AnyType"); //not sure if best name
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

    private static class ClassDoc {

        public final String name;
        public final String description;
        public final List<MethodDoc> documentedMethods;
        public final List<FieldDoc> documentedFields;

        public ClassDoc(Class<?> clazz) {
            LuaTypeDoc typeDoc = clazz.getAnnotation(LuaTypeDoc.class);
            name = typeDoc.name();
            description = typeDoc.description();
            NAME_MAP.put(clazz, name);

            //Find methods
            documentedMethods = new ArrayList<>();
            for (Method method : clazz.getMethods())
                if (method.isAnnotationPresent(LuaMethodDoc.class))
                        documentedMethods.add(new MethodDoc(method));

            //Find fields
            documentedFields = new ArrayList<>();
            for (Field field : clazz.getFields())
                if (field.isAnnotationPresent(LuaFieldDoc.class))
                    documentedFields.add(new FieldDoc(field));
        }

        /**
         * Over here Fran!
         * <3
         */
        public void print() {
            //header
            MutableComponent message = HEADER.copy()

            //type
                    .append("\n\n")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs.text.type"))
                            .append(":")
                            .withStyle(Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(new TextComponent("• " + name).withStyle(Colors.MAYA_BLUE.style))

            //description
                    .append("\n\n")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs.text.description"))
                            .append(":")
                            .withStyle(Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs." + description))
                            .withStyle(Colors.MAYA_BLUE.style));

            FiguraMod.sendChatMessage(message);
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
                if (overloads[i].returnType() == LuaFunctionOverload.DEFAULT.class)
                    returnTypes[i] = method.getReturnType();
                else
                    returnTypes[i] = overloads[i].returnType();
            }
        }

        /**
         * Over here Fran!
         * This format is just temporary, printing out to console.
         * Work your magic and get whatever colors or styles you want printed in chat :D
         * ❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤
         */
        public void print() {
            //header
            MutableComponent message = HEADER.copy()

            //type
                    .append("\n\n")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs.text.function"))
                            .append(":")
                            .withStyle(Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(new TextComponent("• " + name).withStyle(Colors.MAYA_BLUE.style))

            //syntax
                    .append("\n\n")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs.text.syntax"))
                            .append(":")
                            .withStyle(Colors.CHLOE_PURPLE.style));

            for (int i = 0; i < parameterTypes.length; i++) {
                //name
                message.append("\n\t").append(new TextComponent("• " + name).withStyle(Colors.MAYA_BLUE.style))
                        .append("(");

                for (int j = 0; j < parameterTypes[i].length; j++) {
                    //type and arg
                    String typeName = NAME_MAP.getOrDefault(parameterTypes[i][j], parameterTypes[i][j].getName());
                    message.append(new TextComponent(typeName).withStyle(ChatFormatting.YELLOW))
                            .append(" ")
                            .append(new TextComponent(parameterNames[i][j]).withStyle(ChatFormatting.WHITE));

                    if (j != parameterTypes[i].length - 1)
                        message.append(", ");
                }

                //return
                message.append("): ")
                        .append(new FiguraText("docs.text.returns").append(" ").withStyle(Colors.MAYA_BLUE.style))
                        .append(new TextComponent(NAME_MAP.getOrDefault(returnTypes[i], returnTypes[i].getName())).withStyle(ChatFormatting.YELLOW));
            }

            //description
            message.append("\n\n")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs.text.description"))
                            .append(":")
                            .withStyle(Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs." + description))
                            .withStyle(Colors.MAYA_BLUE.style));

            FiguraMod.sendChatMessage(message);
        }
    }

    private static class FieldDoc {

        public final String name;
        public final String description;

        public final Class<?> type;
        public final boolean editable;

        public FieldDoc(Field field) {
            name = field.getName();
            type = field.getType();
            LuaFieldDoc luaFieldDoc = field.getAnnotation(LuaFieldDoc.class);
            description = luaFieldDoc.description();
            editable = !Modifier.isFinal(field.getModifiers());
        }

        public void print() {
            //header
            MutableComponent message = HEADER.copy()

            //type
            .append("\n\n")
            .append(new TextComponent("• ")
                    .append(new FiguraText("docs.text.field"))
                    .append(":")
                    .withStyle(Colors.CHLOE_PURPLE.style))
            .append("\n\t")
                    .append(new TextComponent("• " + NAME_MAP.getOrDefault(type, type.getName())).withStyle(ChatFormatting.YELLOW))
            .append(new TextComponent(" " + name).withStyle(Colors.MAYA_BLUE.style))
            .append(new TextComponent(" (")
                    .append(new FiguraText(editable ? "docs.text.editable" : "docs.text.not_editable"))
                    .append(")")
                    .withStyle(editable ? ChatFormatting.GREEN : ChatFormatting.DARK_RED))

            //description
            .append("\n\n")
            .append(new TextComponent("• ")
                    .append(new FiguraText("docs.text.description"))
                    .append(":")
                    .withStyle(Colors.CHLOE_PURPLE.style))
            .append("\n\t")
            .append(new TextComponent("• ")
                    .append(new FiguraText("docs." + description))
                    .withStyle(Colors.MAYA_BLUE.style));

            FiguraMod.sendChatMessage(message);
        }

    }

    private static void printGroup(String groupName) {
        String capitalized = groupName.substring(0,1).toUpperCase(Locale.ROOT) + groupName.substring(1).toLowerCase(Locale.ROOT);

        MutableComponent message = HEADER.copy()

        .append("\n\n")
        .append(new TextComponent("• ")
                .append(new FiguraText("docs.text.group"))
                .append(":")
                .withStyle(Colors.CHLOE_PURPLE.style))
        .append("\n\t")
            .append(new TextComponent(capitalized).withStyle(Colors.MAYA_BLUE.style))

        .append("\n\n")
        .append(new TextComponent("• ")
                .append(new FiguraText("docs.text.description"))
                .append(":")
                .withStyle(Colors.CHLOE_PURPLE.style))
        .append("\n\t")
        .append(new TextComponent("• ")
                .append(new FiguraText("docs.group." + groupName))
                .withStyle(Colors.MAYA_BLUE.style));

        FiguraMod.sendChatMessage(message);
    }

    private static void printDocs() {
        MutableComponent message = HEADER.copy().append("\n\n")
        .append(new FiguraText("docs").withStyle(Colors.MAYA_BLUE.style));

        FiguraMod.sendChatMessage(message);
    }


    public static void init() {
        //Initialize all the ClassDoc instances
        for (Map.Entry<String, List<Class<?>>> packageEntry : DOCUMENTED_CLASSES.entrySet())
            for (Class<?> documentedClass : packageEntry.getValue())
                if (documentedClass.isAnnotationPresent(LuaTypeDoc.class))
                    GENERATED_CLASS_DOCS.computeIfAbsent(
                            packageEntry.getKey(), (key) -> new ArrayList<>()
                    ).add(new ClassDoc(documentedClass));
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> get() {
        LiteralArgumentBuilder<FabricClientCommandSource> docs = LiteralArgumentBuilder.literal("docs");
        for (Map.Entry<String, List<ClassDoc>> entry : GENERATED_CLASS_DOCS.entrySet()) {
            LiteralArgumentBuilder<FabricClientCommandSource> group;
            if (entry.getKey().length() > 0)
                group = LiteralArgumentBuilder.literal(entry.getKey());
            else
                group = docs;
            for (ClassDoc classDoc : entry.getValue()) {
                LiteralArgumentBuilder<FabricClientCommandSource> typeBranch = LiteralArgumentBuilder.literal(classDoc.name);
                typeBranch.executes(context -> {classDoc.print(); return 1;});
                for (MethodDoc methodDoc : classDoc.documentedMethods) {
                    LiteralArgumentBuilder<FabricClientCommandSource> methodBranch = LiteralArgumentBuilder.literal(methodDoc.name);
                    methodBranch.executes(context -> {methodDoc.print(); return 1;});
                    typeBranch.then(methodBranch);
                }
                for (FieldDoc fieldDoc : classDoc.documentedFields) {
                    LiteralArgumentBuilder<FabricClientCommandSource> fieldBranch = LiteralArgumentBuilder.literal(fieldDoc.name);
                    fieldBranch.executes(context -> {fieldDoc.print(); return 1;});
                    typeBranch.then(fieldBranch);
                }
                group.then(typeBranch);
            }
            if (docs != group) {
                group.executes(context -> {printGroup(entry.getKey()); return 1;});
                docs.then(group);
            }
        }
        docs.executes(context -> {printDocs(); return 1;});
        return docs;
    }
}
