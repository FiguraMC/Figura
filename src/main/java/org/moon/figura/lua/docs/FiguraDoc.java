package org.moon.figura.lua.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public abstract class FiguraDoc {

    public static final MutableComponent HEADER = TextComponent.EMPTY.copy().withStyle(ColorUtils.Colors.FRAN_PINK.style)
                .append(new TextComponent("\n•*+•* ").append(new FiguraText()).append(" Docs *•+*•")
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

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("description", Language.getInstance().getOrDefault(new FiguraText("docs." + description).getString()));
        return json;
    }

    // -- Special prints :p -- //

    public static int printGroup(String name) {
        String capitalised = name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase();

        FiguraMod.sendChatMessage(HEADER.copy()
                .append("\n\n")
                .append(new TextComponent("• ")
                        .append(new FiguraText("docs.text.group"))
                        .append(":")
                        .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                .append("\n\t")
                .append(new TextComponent(capitalised).withStyle(ColorUtils.Colors.MAYA_BLUE.style))

                .append("\n\n")
                .append(new TextComponent("• ")
                        .append(new FiguraText("docs.text.description"))
                        .append(":")
                        .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                .append("\n\t")
                .append(new TextComponent("• ")
                        .append(new FiguraText("docs.group." + name))
                        .withStyle(ColorUtils.Colors.MAYA_BLUE.style)));

        return 1;
    }

    public static int printRoot() {
        FiguraMod.sendChatMessage(HEADER.copy()
                .append("\n\n")
                .append(new FiguraText("docs").withStyle(ColorUtils.Colors.MAYA_BLUE.style)));

        return 1;
    }

    // -- Subtypes -- //

    public static class ClassDoc extends FiguraDoc {

        public final ArrayList<MethodDoc> documentedMethods;
        public final ArrayList<FieldDoc> documentedFields;
        public final Class<?> superclass;

        public ClassDoc(Class<?> clazz, LuaTypeDoc typeDoc) {
            super(typeDoc.name(), typeDoc.description());
            superclass = clazz.getSuperclass();

            //Find methods
            documentedMethods = new ArrayList<>();
            for (Method method : clazz.getMethods())
                if (method.isAnnotationPresent(LuaMethodDoc.class))
                    documentedMethods.add(new MethodDoc(method, method.getAnnotation(LuaMethodDoc.class)));

            //Find fields
            documentedFields = new ArrayList<>();
            for (Field field : clazz.getFields())
                if (field.isAnnotationPresent(LuaFieldDoc.class))
                    documentedFields.add(new FieldDoc(field, field.getAnnotation(LuaFieldDoc.class)));
        }

        @Override
        public int print() {
            //header
            FiguraMod.sendChatMessage(HEADER.copy()

                    //type
                    .append("\n\n")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs.text.type"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(new TextComponent("• " + name).withStyle(ColorUtils.Colors.MAYA_BLUE.style))

                    //description
                    .append("\n\n")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs.text.description"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs." + description))
                            .withStyle(ColorUtils.Colors.MAYA_BLUE.style)));

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
        public JsonObject toJson() {
            JsonObject json = super.toJson();

            if (superclass != Object.class)
                json.addProperty("parent", FiguraDocsManager.NAME_MAP.getOrDefault(superclass, superclass.getName()));

            JsonArray methods = new JsonArray();
            for (FiguraDoc.MethodDoc methodDoc : documentedMethods)
                methods.add(methodDoc.toJson());
            json.add("methods", methods);

            JsonArray fields = new JsonArray();
            for (FiguraDoc.FieldDoc fieldDoc : documentedFields)
                fields.add(fieldDoc.toJson());
            json.add("fields", fields);

            return json;
        }
    }

    public static class MethodDoc extends FiguraDoc {

        public final Class<?>[][] parameterTypes;
        public final String[][] parameterNames;
        public final Class<?>[] returnTypes;

        public MethodDoc(Method method, LuaMethodDoc methodDoc) {
            super(method.getName(), methodDoc.description());

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

        @Override
        public int print() {
            //header
            MutableComponent message = HEADER.copy()

                    //type
                    .append("\n\n")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs.text.function"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(new TextComponent("• " + name).withStyle(ColorUtils.Colors.MAYA_BLUE.style))

                    //syntax
                    .append("\n\n")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs.text.syntax"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style));

            for (int i = 0; i < parameterTypes.length; i++) {
                //name
                message.append("\n\t").append(new TextComponent("• " + name).withStyle(ColorUtils.Colors.MAYA_BLUE.style))
                        .append("(");

                for (int j = 0; j < parameterTypes[i].length; j++) {
                    //type and arg
                    String typeName = FiguraDocsManager.NAME_MAP.getOrDefault(parameterTypes[i][j], parameterTypes[i][j].getName());
                    message.append(new TextComponent(typeName).withStyle(ChatFormatting.YELLOW))
                            .append(" ")
                            .append(new TextComponent(parameterNames[i][j]).withStyle(ChatFormatting.WHITE));

                    if (j != parameterTypes[i].length - 1)
                        message.append(", ");
                }

                //return
                message.append("): ")
                        .append(new FiguraText("docs.text.returns").append(" ").withStyle(ColorUtils.Colors.MAYA_BLUE.style))
                        .append(new TextComponent(FiguraDocsManager.NAME_MAP.getOrDefault(returnTypes[i], returnTypes[i].getName())).withStyle(ChatFormatting.YELLOW));
            }

            //description
            message.append("\n\n")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs.text.description"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs." + description))
                            .withStyle(ColorUtils.Colors.MAYA_BLUE.style));

            FiguraMod.sendChatMessage(message);
            return 1;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = super.toJson();

            JsonArray params = new JsonArray();
            for (int i = 0; i < parameterNames.length; i++) {
                JsonArray param = new JsonArray();
                for (int j = 0; j < parameterNames[i].length; j++) {
                    JsonObject paramObj = new JsonObject();
                    paramObj.addProperty("name", parameterNames[i][j]);
                    paramObj.addProperty("type", FiguraDocsManager.NAME_MAP.getOrDefault(parameterTypes[i][j], parameterTypes[i][j].getName()));
                    param.add(paramObj);
                }

                params.add(param);
            }
            json.add("parameters", params);

            JsonArray returns = new JsonArray();
            for (Class<?> returnType : returnTypes)
                returns.add(FiguraDocsManager.NAME_MAP.getOrDefault(returnType, returnType.getName()));
            json.add("returns", returns);

            return json;
        }
    }

    public static class FieldDoc extends FiguraDoc {

        public final Class<?> type;
        public final boolean editable;

        public FieldDoc(Field field, LuaFieldDoc luaFieldDoc) {
            super(field.getName(), luaFieldDoc.description());
            type = field.getType();
            editable = !Modifier.isFinal(field.getModifiers());
        }

        @Override
        public int print() {
            //header
            FiguraMod.sendChatMessage(HEADER.copy()

                    //type
                    .append("\n\n")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs.text.field"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(new TextComponent("• " + FiguraDocsManager.NAME_MAP.getOrDefault(type, type.getName())).withStyle(ChatFormatting.YELLOW))
                    .append(new TextComponent(" " + name).withStyle(ColorUtils.Colors.MAYA_BLUE.style))
                    .append(new TextComponent(" (")
                            .append(new FiguraText(editable ? "docs.text.editable" : "docs.text.not_editable"))
                            .append(")")
                            .withStyle(editable ? ChatFormatting.GREEN : ChatFormatting.DARK_RED))

                    //description
                    .append("\n\n")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs.text.description"))
                            .append(":")
                            .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                    .append("\n\t")
                    .append(new TextComponent("• ")
                            .append(new FiguraText("docs." + description))
                            .withStyle(ColorUtils.Colors.MAYA_BLUE.style)));

            return 1;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = super.toJson();
            json.addProperty("type", FiguraDocsManager.NAME_MAP.getOrDefault(this.type, this.type.getName()));
            json.addProperty("editable", this.editable);
            return json;
        }
    }
}
