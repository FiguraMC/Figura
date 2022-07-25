package org.moon.figura.newlua.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.newlua.LuaType;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

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

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("description", Language.getInstance().getOrDefault(FiguraText.of("docs." + description).getString()));
        return json;
    }

    // -- Special prints :p -- //

    public static int printGroup(String name) {
        String capitalised = name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase();

        FiguraMod.sendChatMessage(HEADER.copy()
                .append("\n\n")
                .append(Component.literal("• ")
                        .append(FiguraText.of("docs.text.group"))
                        .append(":")
                        .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                .append("\n\t")
                .append(Component.literal(capitalised).withStyle(ColorUtils.Colors.MAYA_BLUE.style))

                .append("\n\n")
                .append(Component.literal("• ")
                        .append(FiguraText.of("docs.text.description"))
                        .append(":")
                        .withStyle(ColorUtils.Colors.CHLOE_PURPLE.style))
                .append("\n\t")
                .append(Component.literal("• ")
                        .append(FiguraText.of("docs.group." + name))
                        .withStyle(ColorUtils.Colors.MAYA_BLUE.style)));

        return 1;
    }

    public static int printRoot() {
        FiguraMod.sendChatMessage(HEADER.copy()
                .append("\n\n")
                .append(FiguraText.of("docs").withStyle(ColorUtils.Colors.MAYA_BLUE.style)));

        return 1;
    }

    // -- Subtypes -- //

    public static class ClassDoc extends FiguraDoc {

        public final ArrayList<MethodDoc> documentedMethods;
        public final Class<?> superclass;

        public ClassDoc(Class<?> clazz, LuaTypeDoc typeDoc) {
            super(typeDoc.name(), typeDoc.description());

            if (clazz.getSuperclass().isAnnotationPresent(LuaType.class) && clazz.getSuperclass().isAnnotationPresent(LuaTypeDoc.class))
                superclass = clazz.getSuperclass();
            else
                superclass = null;

            //Find methods
            documentedMethods = new ArrayList<>();
            for (Method method : clazz.getMethods())
                if (method.isAnnotationPresent(LuaMethodDoc.class))
                    documentedMethods.add(new MethodDoc(method, method.getAnnotation(LuaMethodDoc.class), typeDoc.name()));

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
                        .append(Component.literal(FiguraDocsManager.NAME_MAP.getOrDefault(superclass, superclass.getName())).withStyle(ColorUtils.Colors.MAYA_BLUE.style))
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
            for (MethodDoc methodDoc : documentedMethods)
                command.then(methodDoc.getCommand());

            return command;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = super.toJson();

            if (superclass != null)
                json.addProperty("parent", FiguraDocsManager.NAME_MAP.getOrDefault(superclass, superclass.getName()));

            JsonArray methods = new JsonArray();
            for (MethodDoc methodDoc : documentedMethods)
                methods.add(methodDoc.toJson());
            json.add("methods", methods);

            return json;
        }
    }

    public static class MethodDoc extends FiguraDoc {

        public final Class<?>[][] parameterTypes;
        public final String[][] parameterNames;
        public final Class<?>[] returnTypes;
        public final String typeName;
        public final boolean isStatic;

        public MethodDoc(Method method, LuaMethodDoc methodDoc, String typeName) {
            super(method.getName(), methodDoc.description());

            LuaFunctionOverload[] overloads = methodDoc.overloads();
            parameterTypes = new Class[overloads.length][];
            parameterNames = new String[overloads.length][];
            returnTypes = new Class[overloads.length];
            isStatic = Modifier.isStatic(method.getModifiers());
            this.typeName = typeName;

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
                String prefix = "• " + typeName + (isStatic ? "." : ":") + name;
                message.append("\n\t").append(Component.literal(prefix).withStyle(ColorUtils.Colors.MAYA_BLUE.style))
                        .append("(");

                for (int j = 0; j < parameterTypes[i].length; j++) {
                    //type and arg
                    String typeName = FiguraDocsManager.NAME_MAP.getOrDefault(parameterTypes[i][j], parameterTypes[i][j].getName());
                    message.append(Component.literal(typeName).withStyle(ChatFormatting.YELLOW))
                            .append(" ")
                            .append(Component.literal(parameterNames[i][j]).withStyle(ChatFormatting.WHITE));

                    if (j != parameterTypes[i].length - 1)
                        message.append(", ");
                }

                //return
                message.append("): ")
                        .append(FiguraText.of("docs.text.returns").append(" ").withStyle(ColorUtils.Colors.MAYA_BLUE.style))
                        .append(Component.literal(FiguraDocsManager.NAME_MAP.getOrDefault(returnTypes[i], returnTypes[i].getName())).withStyle(ChatFormatting.YELLOW));
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

}
