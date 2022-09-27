package org.moon.figura.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.FiguraMod;
import org.moon.figura.animation.Animation;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.avatars.providers.LocalAvatarFetcher;
import org.moon.figura.backend.NetworkManager;
import org.moon.figura.config.Config;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.FiguraText;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class FiguraDebugCommand {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();

    public static LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> debug = LiteralArgumentBuilder.literal("debug");
        debug.executes(FiguraDebugCommand::commandAction);
        return debug;
    }

    private static int commandAction(CommandContext<FabricClientCommandSource> context) {
        try {
            //get path
            Path targetPath = FiguraMod.getFiguraDirectory().resolve("debug_data.json");

            //create file
            if (!Files.exists(targetPath))
                Files.createFile(targetPath);

            //write file
            FileOutputStream fs = new FileOutputStream(targetPath.toFile());
            fs.write(fetchStatus(AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID())).getBytes());
            fs.close();

            //feedback
            context.getSource().sendFeedback(
                    FiguraText.of("command.debug.success")
                            .append(" ")
                            .append(FiguraText.of("command.click_to_open")
                                    .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, targetPath.toFile().toString())).withUnderlined(true))
                            )
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(FiguraText.of("command.debug.error"));
            FiguraMod.LOGGER.error("Failed to save " + FiguraMod.MOD_NAME + " debug data!", e);
            return 0;
        }
    }

    public static String fetchStatus(Avatar avatar) {
        //root
        JsonObject root = new JsonObject();

        //mod meta
        JsonObject meta = new JsonObject();

        meta.addProperty("version", FiguraMod.VERSION);
        meta.addProperty("localUUID", FiguraMod.getLocalPlayerUUID().toString());
        meta.addProperty("ticks", FiguraMod.ticks);
        meta.addProperty("figuraDirectory", FiguraMod.getFiguraDirectory().toString());
        meta.addProperty("figuraCacheDirectory", FiguraMod.getCacheDirectory().toString());
        meta.addProperty("backendStatus", NetworkManager.backendStatus);
        meta.addProperty("hasBackend", NetworkManager.hasBackend());
        meta.addProperty("backendDisconnectedReason", NetworkManager.disconnectedReason);
        meta.addProperty("uploaded", AvatarManager.localUploaded);
        meta.addProperty("panicMode", AvatarManager.panic);

        root.add("meta", meta);

        //config
        JsonObject config = new JsonObject();

        for (Config value : Config.values())
            if (value.value != null)
                config.addProperty(value.name(), value.value.toString());

        root.add("config", config);

        //trust groups
        JsonObject trust = new JsonObject();

        for (Map.Entry<ResourceLocation, TrustContainer> entry : TrustManager.GROUPS.entrySet()) {
            JsonObject t = new JsonObject();

            for (Map.Entry<TrustContainer.Trust, Integer> entry1 : entry.getValue().getSettings().entrySet())
                t.addProperty(entry1.getKey().toString(), entry1.getValue());

            trust.add(entry.getKey().toString(), t);
        }

        root.add("trust", trust);

        //avatars
        LocalAvatarFetcher.load();
        root.add("avatars", getAvatarsPaths(LocalAvatarFetcher.ALL_AVATARS));


        // -- avatar -- //


        if (avatar == null)
            return GSON.toJson(root);

        JsonObject a = new JsonObject();

        //trust
        JsonObject aTrust = new JsonObject();

        aTrust.addProperty("parentTrust", avatar.trust.getParentGroup().name);

        for (Map.Entry<TrustContainer.Trust, Integer> entry : avatar.trust.getSettings().entrySet())
            aTrust.addProperty(entry.getKey().toString(), entry.getValue());

        a.add("trust", aTrust);

        //avatar metadata
        JsonObject aMeta = new JsonObject();

        aMeta.addProperty("version", avatar.version);
        aMeta.addProperty("versionStatus", avatar.versionStatus);
        aMeta.addProperty("color", avatar.color);
        aMeta.addProperty("authors", avatar.authors);
        aMeta.addProperty("name", avatar.name);
        aMeta.addProperty("entityName", avatar.entityName);
        aMeta.addProperty("fileSize", avatar.fileSize);
        aMeta.addProperty("isHost", avatar.isHost);
        aMeta.addProperty("loaded", avatar.loaded);
        aMeta.addProperty("owner", avatar.owner.toString());
        aMeta.addProperty("scriptError", avatar.scriptError);
        aMeta.addProperty("hasTexture", avatar.hasTexture);
        aMeta.addProperty("hasLuaRuntime", avatar.luaRuntime != null);
        aMeta.addProperty("hasRenderer", avatar.renderer != null);
        aMeta.addProperty("hasData", avatar.nbt != null);

        a.add("meta", aMeta);

        //avatar complexity
        JsonObject inst = new JsonObject();

        inst.addProperty("animationComplexity", avatar.animationComplexity);
        inst.addProperty("complexity", avatar.complexity.pre);
        inst.addProperty("entityInitInstructions", avatar.init.post);
        inst.addProperty("entityRenderInstructions", avatar.render.pre);
        inst.addProperty("entityTickInstructions", avatar.tick.pre);
        inst.addProperty("initInstructions", avatar.init.pre);
        inst.addProperty("postEntityRenderInstructions", avatar.render.post);
        inst.addProperty("postWorldRenderInstructions", avatar.worldRender.post);
        inst.addProperty("worldRenderInstructions", avatar.worldRender.pre);
        inst.addProperty("worldTickInstructions", avatar.worldTick.pre);
        inst.addProperty("particlesRemaining", avatar.particlesRemaining.peek());
        inst.addProperty("soundsRemaining", avatar.soundsRemaining.peek());

        a.add("instructions", inst);

        //sounds
        JsonArray sounds = new JsonArray();

        for (String s : avatar.customSounds.keySet())
            sounds.add(s);

        a.add("sounds", sounds);

        //animations
        JsonArray animations = new JsonArray();

        for (Animation animation : avatar.animations.values())
            animations.add(animation.modelName + "/" + animation.name);

        a.add("animations", animations);

        //return as string
        root.add("avatar", a);
        return GSON.toJson(root);
    }

    private static JsonObject getAvatarsPaths(List<LocalAvatarFetcher.AvatarPath> list) {
        JsonObject avatar = new JsonObject();

        for (LocalAvatarFetcher.AvatarPath path : list) {
            String name = path.getPath().getFileName().toString();

            if (path instanceof LocalAvatarFetcher.FolderPath folder)
                avatar.add(name, getAvatarsPaths(folder.getChildren()));
            else
                avatar.addProperty(name, path.getName());
        }

        return avatar;
    }
}
