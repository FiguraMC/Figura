package org.figuramc.figura.lua.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public abstract class FiguraDoc {

    public static final MutableComponent HEADER = Component.empty().withStyle(ColorUtils.Colors.AWESOME_BLUE.style)
                .append(Component.literal("\n•*+•* ").append(FiguraText.of()).append(" Docs *•+*•")
                        .withStyle(ChatFormatting.UNDERLINE));

    public final String name;
    public final String description;

    public FiguraDoc(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // -- Methods -- // 

    public abstract int print();

    public LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
        LiteralArgumentBuilder<FiguraClientCommandSource> command = LiteralArgumentBuilder.literal(name);
        command.executes(context -> print());

        return command;
    }

    public JsonObject toJson(boolean translate) {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("description", translate ? Language.getInstance().getOrDefault(FiguraText.of("docs." + description).getString()) : FiguraMod.MOD_ID + "." + "docs." + description);
        return json;
    }

    // -- Special prints :p -- // 

    public static int printRoot() {
        FiguraMod.sendChatMessage(HEADER.copy()
                .append("\n\n")
                .append(FiguraText.of("docs").withStyle(ColorUtils.Colors.BLUE.style)));

        return 1;
    }

    // -- Subtypes -- // 

    public static class ClassDoc extends FiguraDoc {

        public final ArrayList<MethodDoc> documentedMethods;
        public final ArrayList<FieldDoc> documentedFields;
        public final Class<?> thisClass, superclass;

        public ClassDoc(Class<?> clazz, LuaTypeDoc typeDoc) {
            this(clazz, typeDoc, null);
        }

        public ClassDoc(Class<?> clazz, LuaTypeDoc typeDoc, Map<String, List<FiguraDoc>> children) {
            super(typeDoc.name(), typeDoc.value());

            thisClass = clazz;

            if (clazz.getSuperclass().isAnnotationPresent(LuaTypeDoc.class))
                superclass = clazz.getSuperclass();
            else
                superclass = null;

            // Find methods
            documentedMethods = new ArrayList<>();
            Set<String> foundIndices = new HashSet<>();
            for (Method method : clazz.getDeclaredMethods())
                parseMethodIfNeeded(foundIndices, children, typeDoc, method);
            for (Method method : clazz.getMethods())
                parseMethodIfNeeded(foundIndices, children, typeDoc, method);

            // Find fields
            documentedFields = new ArrayList<>();
            for (Field field : clazz.getDeclaredFields())
                parseFieldIfNeeded(children, foundIndices, field);
            for (Field field : clazz.getFields())
                parseFieldIfNeeded(children, foundIndices, field);
        }

        // Parse docs for this method if none were already found and stored in "foundIndices".
        private void parseMethodIfNeeded(Set<String> foundIndices, Map<String, List<FiguraDoc>> children, LuaTypeDoc typeDoc, Method method) {
            String name = method.getName();
            if (foundIndices.contains(name) || !method.isAnnotationPresent(LuaMethodDoc.class))
                return;

            foundIndices.add(name);
            LuaMethodDoc doc = method.getAnnotation(LuaMethodDoc.class);
            List<FiguraDoc> childList = children == null ? null : children.get(name);
            documentedMethods.add(new MethodDoc(method, doc, childList, typeDoc.name()));
        }

        // Parse docs for this field if none were already found and stored in "foundIndices".
        private void parseFieldIfNeeded(Map<String, List<FiguraDoc>> children, Set<String> foundIndices, Field field) {
            String name = field.getName();
            if (foundIndices.contains(name) || !field.isAnnotationPresent(LuaFieldDoc.class))
                return;

            foundIndices.add(name);
            List<FiguraDoc> childList = children == null ? null : children.get(name);
            documentedFields.add(new FieldDoc(field, field.getAnnotation(LuaFieldDoc.class), childList));
        }


        @Override
        public int print() {
            // header
            MutableComponent message = HEADER.copy()
                    .append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.type"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.PURPLE.style));

            // type
            message.append("\n\t")
                    .append(Component.literal("• " + name).withStyle(ColorUtils.Colors.BLUE.style));

            if (superclass != null) {
                message.append(" (")
                        .append(FiguraText.of("docs.text.extends"))
                        .append(" ")
                        .append(FiguraDocsManager.getClassText(superclass).withStyle(ChatFormatting.YELLOW))
                        .append(")");
            }

            // description
            message.append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.description"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.PURPLE.style));

            MutableComponent descText = Component.empty().withStyle(ColorUtils.Colors.BLUE.style);
            for (Component component : TextUtils.splitText(FiguraText.of("docs." + description), "\n"))
                descText.append("\n\t").append("• ").append(component);
            message.append(descText);

            FiguraMod.sendChatMessage(message);
            return 1;
        }

        @Override
        public LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
            // this
            LiteralArgumentBuilder<FiguraClientCommandSource> command = super.getCommand();

            // methods
            for (FiguraDoc.MethodDoc methodDoc : documentedMethods)
                command.then(methodDoc.getCommand());

            // fields
            for (FiguraDoc.FieldDoc fieldDoc : documentedFields)
                command.then(fieldDoc.getCommand());

            return command;
        }

        @Override
        public JsonObject toJson(boolean translate) {
            JsonObject json = super.toJson(translate);

            if (superclass != null)
                json.addProperty("parent", FiguraDocsManager.getNameFor(superclass));

            JsonArray methods = new JsonArray();
            for (FiguraDoc.MethodDoc methodDoc : documentedMethods)
                methods.add(methodDoc.toJson(translate));
            json.add("methods", methods);

            JsonArray fields = new JsonArray();
            for (FiguraDoc.FieldDoc fieldDoc : documentedFields)
                fields.add(fieldDoc.toJson(translate));
            json.add("fields", fields);

            return json;
        }
    }

    public static class MethodDoc extends FiguraDoc {

        public final Class<?>[][] parameterTypes;
        public final String[][] parameterNames;
        public final Class<?>[] returnTypes;
        public final String typeName;
        public final String[] aliases;
        public final boolean isStatic;
        public final List<FiguraDoc> children;

        public MethodDoc(Method method, LuaMethodDoc methodDoc, List<FiguraDoc> children, String typeName) {
            super(method.getName(), methodDoc.value());

            LuaMethodOverload[] overloads = methodDoc.overloads();
            parameterTypes = new Class[overloads.length][];
            parameterNames = new String[overloads.length][];
            returnTypes = new Class[overloads.length];
            isStatic = Modifier.isStatic(method.getModifiers());
            aliases = methodDoc.aliases();
            this.typeName = typeName;
            this.children = children;

            for (int i = 0; i < overloads.length; i++) {
                parameterTypes[i] = overloads[i].argumentTypes();
                parameterNames[i] = overloads[i].argumentNames();

                if (overloads[i].returnType() == LuaMethodOverload.DEFAULT.class)
                    returnTypes[i] = method.getReturnType();
                else
                    returnTypes[i] = overloads[i].returnType();
            }
        }

        @Override
        public int print() {
            // header
            MutableComponent message = HEADER.copy();

            // type
            message.append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.function"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.PURPLE.style))
                    .append("\n\t")
                    .append(Component.literal("• " + name).withStyle(ColorUtils.Colors.BLUE.style));

            // aliases
            if (aliases.length > 0) {
                message.append("\n\n")
                        .append(Component.literal("• ")
                                .append(FiguraText.of("docs.text.aliases"))
                                .append(":")
                                .withStyle(ColorUtils.Colors.PURPLE.style));

                for (String alias : aliases) {
                    message.append("\n\t")
                            .append(Component.literal("• ")
                                    .append(alias)
                                    .withStyle(ColorUtils.Colors.BLUE.style));
                }
            }

            // syntax
            message.append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.syntax"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.PURPLE.style));

            for (int i = 0; i < parameterTypes.length; i++) {

                // name
                message.append("\n\t")
                        .append(Component.literal("• ").withStyle(ColorUtils.Colors.BLUE.style))
                        .append(Component.literal("<" + typeName + ">").withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(isStatic ? "." : ":").withStyle(ChatFormatting.BOLD))
                        .append(Component.literal(name).withStyle(ColorUtils.Colors.BLUE.style))
                        .append("(");

                for (int j = 0; j < parameterTypes[i].length; j++) {
                    // type and arg
                    message.append(FiguraDocsManager.getClassText(parameterTypes[i][j]).withStyle(ChatFormatting.YELLOW))
                            .append(" ")
                            .append(Component.literal(parameterNames[i][j]).withStyle(ChatFormatting.WHITE));

                    if (j != parameterTypes[i].length - 1)
                        message.append(", ");
                }

                // return
                message.append(") → ")
                        .append(FiguraText.of("docs.text.returns").append(" ").withStyle(ColorUtils.Colors.BLUE.style))
                        .append(FiguraDocsManager.getClassText(returnTypes[i]).withStyle(ChatFormatting.YELLOW));
            }

            // description
            message.append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.description"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.PURPLE.style));

            MutableComponent descText = Component.empty().withStyle(ColorUtils.Colors.BLUE.style);
            for (Component component : TextUtils.splitText(FiguraText.of("docs." + description), "\n"))
                descText.append("\n\t").append("• ").append(component);
            message.append(descText);

            FiguraMod.sendChatMessage(message);
            return 1;
        }

        @Override
        public LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
            LiteralArgumentBuilder<FiguraClientCommandSource> command = super.getCommand();

            if (children != null)
                for (FiguraDoc child : children)
                    command.then(child.getCommand());

            return command;
        }

        @Override
        public JsonObject toJson(boolean translate) {
            JsonObject json = super.toJson(translate);

            JsonArray params = new JsonArray();
            for (int i = 0; i < parameterNames.length; i++) {
                JsonArray param = new JsonArray();
                for (int j = 0; j < parameterNames[i].length; j++) {
                    JsonObject paramObj = new JsonObject();
                    paramObj.addProperty("name", parameterNames[i][j]);
                    paramObj.addProperty("type", FiguraDocsManager.getNameFor(parameterTypes[i][j]));
                    param.add(paramObj);
                }

                params.add(param);
            }
            json.add("parameters", params);

            JsonArray returns = new JsonArray();
            for (Class<?> returnType : returnTypes)
                returns.add(FiguraDocsManager.getNameFor(returnType));
            json.add("returns", returns);

            JsonArray aliases = new JsonArray();
            for (String alias : this.aliases)
                aliases.add(alias);
            json.add("aliases", aliases);

            JsonArray children = new JsonArray();
            if (this.children != null) {
                for (FiguraDoc child : this.children)
                    children.add(child.toJson(translate));
            }
            json.add("children", children);

            json.addProperty("static", isStatic);

            return json;
        }
    }

    public static class FieldDoc extends FiguraDoc {

        public final Class<?> type;
        public final boolean editable;
        public final List<FiguraDoc> children;

        public FieldDoc(Field field, LuaFieldDoc luaFieldDoc, List<FiguraDoc> children) {
            super(field.getName(), luaFieldDoc.value());
            type = field.getType();
            editable = !Modifier.isFinal(field.getModifiers());
            this.children = children;
        }

        @Override
        public int print() {
            // header
            MutableComponent message = HEADER.copy()

                    // type
                    .append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.field"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.PURPLE.style))
                    .append("\n\t")
                    .append(Component.literal("• ").withStyle(ColorUtils.Colors.BLUE.style))
                    .append(FiguraDocsManager.getClassText(type).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" " + name).withStyle(ColorUtils.Colors.BLUE.style))
                    .append(Component.literal(" (")
                            .append(FiguraText.of(editable ? "docs.text.editable" : "docs.text.not_editable"))
                            .append(")")
                            .withStyle(editable ? ChatFormatting.GREEN : ChatFormatting.DARK_RED));

            // description
            message.append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.description"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.PURPLE.style));

            MutableComponent descText = Component.empty().withStyle(ColorUtils.Colors.BLUE.style);
            for (Component component : TextUtils.splitText(FiguraText.of("docs." + description), "\n"))
                descText.append("\n\t").append("• ").append(component);
            message.append(descText);

            FiguraMod.sendChatMessage(message);
            return 1;
        }

        @Override
        public LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
            LiteralArgumentBuilder<FiguraClientCommandSource> command = super.getCommand();

            if (children != null)
                for (FiguraDoc child : children)
                    command.then(child.getCommand());

            return command;
        }

        @Override
        public JsonObject toJson(boolean translate) {
            JsonObject json = super.toJson(translate);
            json.addProperty("type", FiguraDocsManager.getNameFor(this.type));
            json.addProperty("editable", this.editable);

            JsonArray children = new JsonArray();
            if (this.children != null) {
                for (FiguraDoc child : this.children)
                    children.add(child.toJson(translate));
            }
            json.add("children", children);

            return json;
        }
    }
}
