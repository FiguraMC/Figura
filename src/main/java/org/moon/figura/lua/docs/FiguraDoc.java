package org.moon.figura.lua.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class FiguraDoc {

    public static final MutableComponent HEADER = Component.empty().withStyle(ColorUtils.Colors.FRAN_PINK.style)
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

    public LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> command = LiteralArgumentBuilder.literal(name);
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
                .append(FiguraText.of("docs").withStyle(ColorUtils.Colors.MAYA_BLUE.style)));

        return 1;
    }

    // -- Subtypes -- //

    public static class ClassDoc extends FiguraDoc {

        public final ArrayList<MethodDoc> documentedMethods;
        public final ArrayList<FieldDoc> documentedFields;
        public final Class<?> superclass;

        public ClassDoc(Class<?> clazz, LuaTypeDoc typeDoc) {
            this(clazz, typeDoc, null);
        }

        public ClassDoc(Class<?> clazz, LuaTypeDoc typeDoc, Map<String, List<FiguraDoc>> children) {
            super(typeDoc.name(), typeDoc.description());

            if (clazz.getSuperclass().isAnnotationPresent(LuaWhitelist.class) && clazz.getSuperclass().isAnnotationPresent(LuaTypeDoc.class))
                superclass = clazz.getSuperclass();
            else
                superclass = null;

            //Find methods
            documentedMethods = new ArrayList<>();
            for (Method method : clazz.getMethods())
                if (method.isAnnotationPresent(LuaMethodDoc.class)) {
                    List<FiguraDoc> childList = children == null ? null : children.get(method.getName());
                    documentedMethods.add(new MethodDoc(method, method.getAnnotation(LuaMethodDoc.class), childList));
                }

            //Find fields
            documentedFields = new ArrayList<>();
            for (Field field : clazz.getFields())
                if (field.isAnnotationPresent(LuaFieldDoc.class)) {
                    List<FiguraDoc> childList = children == null ? null : children.get(field.getName());
                    documentedFields.add(new FieldDoc(field, field.getAnnotation(LuaFieldDoc.class), childList));
                }
        }

        @Override
        public int print() {
            //header
            MutableComponent message = HEADER.copy()
                    .append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.type"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style));

            //type
            message.append("\n\t")
                    .append(Component.literal("• " + name).withStyle(ColorUtils.Colors.MAYA_BLUE.style));

            if (superclass != null) {
                message.append(" (")
                        .append(FiguraText.of("docs.text.extends"))
                        .append(" ")
                        .append(Component.literal(FiguraDocsManager.getNameFor(superclass)).withStyle(ColorUtils.Colors.MAYA_BLUE.style))
                        .append(")");
            }

            //description
            message.append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.description"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs." + description))
                            .withStyle(ColorUtils.Colors.MAYA_BLUE.style));

            FiguraMod.sendChatMessage(message);
            return 1;
        }

        @Override
        public LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
            //this
            LiteralArgumentBuilder<FabricClientCommandSource> command = super.getCommand();

            //methods
            for (FiguraDoc.MethodDoc methodDoc : documentedMethods)
                command.then(methodDoc.getCommand());

            //fields
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
        public final List<FiguraDoc> children;

        public MethodDoc(Method method, LuaMethodDoc methodDoc, List<FiguraDoc> children) {
            super(method.getName(), methodDoc.description());

            LuaFunctionOverload[] overloads = methodDoc.overloads();
            parameterTypes = new Class[overloads.length][];
            parameterNames = new String[overloads.length][];
            returnTypes = new Class[overloads.length];
            this.children = children;

            for (int i = 0; i < overloads.length; i++) {
                parameterTypes[i] = overloads[i].argumentTypes();
                parameterNames[i] = overloads[i].argumentNames();

                if (overloads[i].returnType() == LuaFunctionOverload.DEFAULT.class)
                    returnTypes[i] = method.getReturnType();
                else
                    returnTypes[i] = overloads[i].returnType();
            }
        }

        @Override
        public int print() {
            //header
            MutableComponent message = HEADER.copy()

                    //type
                    .append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.function"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(Component.literal("• " + name).withStyle(ColorUtils.Colors.MAYA_BLUE.style))

                    //syntax
                    .append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.syntax"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style));

            for (int i = 0; i < parameterTypes.length; i++) {
                //name
                message.append("\n\t").append(Component.literal("• " + name).withStyle(ColorUtils.Colors.MAYA_BLUE.style))
                        .append("(");

                for (int j = 0; j < parameterTypes[i].length; j++) {
                    //type and arg
                    String typeName = FiguraDocsManager.getNameFor(parameterTypes[i][j]);
                    message.append(Component.literal(typeName).withStyle(ChatFormatting.YELLOW))
                            .append(" ")
                            .append(Component.literal(parameterNames[i][j]).withStyle(ChatFormatting.WHITE));

                    if (j != parameterTypes[i].length - 1)
                        message.append(", ");
                }

                //return
                message.append("): ")
                        .append(FiguraText.of("docs.text.returns").append(" ").withStyle(ColorUtils.Colors.MAYA_BLUE.style))
                        .append(Component.literal(FiguraDocsManager.getNameFor(returnTypes[i])).withStyle(ChatFormatting.YELLOW));
            }

            //description
            message.append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.description"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs." + description))
                            .withStyle(ColorUtils.Colors.MAYA_BLUE.style));

            FiguraMod.sendChatMessage(message);
            return 1;
        }

        @Override
        public LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
            LiteralArgumentBuilder<FabricClientCommandSource> command = super.getCommand();

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

            JsonArray children = new JsonArray();
            if (this.children != null) {
                for (FiguraDoc child : this.children)
                    children.add(child.toJson(translate));
            }
            json.add("children", children);

            return json;
        }
    }

    public static class FieldDoc extends FiguraDoc {

        public final Class<?> type;
        public final boolean editable;
        public final List<FiguraDoc> children;

        public FieldDoc(Field field, LuaFieldDoc luaFieldDoc, List<FiguraDoc> children) {
            super(field.getName(), luaFieldDoc.description());
            type = field.getType();
            editable = !Modifier.isFinal(field.getModifiers());
            this.children = children;
        }

        @Override
        public int print() {
            //header
            FiguraMod.sendChatMessage(HEADER.copy()

                    //type
                    .append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.field"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(Component.literal("• ").withStyle(ColorUtils.Colors.MAYA_BLUE.style))
                    .append(Component.literal(FiguraDocsManager.getNameFor(type)).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" " + name).withStyle(ColorUtils.Colors.MAYA_BLUE.style))
                    .append(Component.literal(" (")
                            .append(FiguraText.of(editable ? "docs.text.editable" : "docs.text.not_editable"))
                            .append(")")
                            .withStyle(editable ? ChatFormatting.GREEN : ChatFormatting.DARK_RED))

                    //description
                    .append("\n\n")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs.text.description"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(Component.literal("• ")
                            .append(FiguraText.of("docs." + description))
                            .withStyle(ColorUtils.Colors.MAYA_BLUE.style)));

            return 1;
        }

        @Override
        public LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
            LiteralArgumentBuilder<FabricClientCommandSource> command = super.getCommand();

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
