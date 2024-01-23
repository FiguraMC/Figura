package org.figuramc.figura.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.animation.Animation;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.local.CacheAvatarLoader;
import org.figuramc.figura.avatar.local.LocalAvatarFetcher;
import org.figuramc.figura.avatar.local.LocalAvatarLoader;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.config.ConfigManager;
import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.lua.api.ConfigAPI;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.permissions.PermissionPack;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.resources.FiguraRuntimeResources;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.IOUtils;
import org.figuramc.figura.utils.MathUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;

class DebugCommand {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();

    public static LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
        LiteralArgumentBuilder<FiguraClientCommandSource> debug = LiteralArgumentBuilder.literal("debug");
        debug.executes(DebugCommand::commandAction);
        return debug;
    }

    private static int commandAction(CommandContext<FiguraClientCommandSource> context) {
        try {
            // get path
            Path targetPath = FiguraMod.getFiguraDirectory().resolve("debug_data.json");

            // create file
            if (!Files.exists(targetPath))
                Files.createFile(targetPath);

            // write file
            OutputStream fs = Files.newOutputStream(targetPath);
            fs.write(fetchStatus(AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID())).getBytes());
            fs.close();

            // feedback
            context.getSource().figura$sendFeedback(
                    FiguraText.of("command.debug.success")
                            .append(" ")
                            .append(FiguraText.of("command.click_to_open")
                                    .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, targetPath.toString())).withUnderlined(true))
                            )
            );
            return 1;
        } catch (Exception e) {
            context.getSource().figura$sendError(FiguraText.of("command.debug.error"));
            FiguraMod.LOGGER.error("Failed to save " + FiguraMod.MOD_NAME + " debug data!", e);
            return 0;
        }
    }

    public static String fetchStatus(Avatar avatar) {
        // root
        JsonObject root = new JsonObject();

        // mod meta
        JsonObject meta = new JsonObject();

        meta.addProperty("version", FiguraMod.VERSION.toString());
        meta.addProperty("localUUID", FiguraMod.getLocalPlayerUUID().toString());
        meta.addProperty("ticks", FiguraMod.ticks);
        meta.addProperty("figuraDirectory", FiguraMod.getFiguraDirectory().toString());
        meta.addProperty("figuraAvatarDirectory", LocalAvatarFetcher.getLocalAvatarDirectory().toString());
        meta.addProperty("figuraAvatarDataDirectory", ConfigAPI.getConfigDataDir().toString());
        meta.addProperty("figuraCacheDirectory", FiguraMod.getCacheDirectory().toString());
        meta.addProperty("figuraAvatarCacheDirectory", CacheAvatarLoader.getAvatarCacheDirectory().toString());
        meta.addProperty("figuraResourcesDirectory", FiguraRuntimeResources.getRootDirectory().toString());
        meta.addProperty("figuraAssetsDirectory", FiguraRuntimeResources.getAssetsDirectory().toString());
        meta.addProperty("backendStatus", NetworkStuff.backendStatus);
        meta.addProperty("backendConnected", NetworkStuff.isConnected());
        meta.addProperty("backendDisconnectedReason", NetworkStuff.disconnectedReason);
        meta.addProperty("uploaded", AvatarManager.localUploaded);
        meta.addProperty("lastLoadedPath", Objects.toString(LocalAvatarLoader.getLastLoadedPath(), null));
        meta.addProperty("panicMode", AvatarManager.panic);

        root.add("meta", meta);

        // config
        JsonObject config = new JsonObject();

        for (ConfigType<?> value : ConfigManager.REGISTRY)
            if (value.value != null)
                config.addProperty(value.id, value.value.toString());

        root.add("config", config);

        // all permissions
        JsonObject permissions = new JsonObject();

        for (PermissionPack.CategoryPermissionPack group : PermissionManager.CATEGORIES.values()) {
            JsonObject allPermissions = new JsonObject();

            JsonObject standard = new JsonObject();
            for (Map.Entry<Permissions, Integer> entry : group.getPermissions().entrySet())
                standard.addProperty(entry.getKey().name, entry.getValue());

            allPermissions.add("standard", standard);

            JsonObject customPermissions = new JsonObject();
            for (Map.Entry<String, Map<Permissions, Integer>> entry : group.getCustomPermissions().entrySet()) {
                JsonObject obj = new JsonObject();
                for (Map.Entry<Permissions, Integer> entry1 : entry.getValue().entrySet())
                    obj.addProperty(entry1.getKey().name, entry1.getValue());

                customPermissions.add(entry.getKey(), obj);
            }

            allPermissions.add("custom", customPermissions);

            permissions.add(group.name, allPermissions);
        }

        root.add("permissions", permissions);

        // avatars
        LocalAvatarFetcher.reloadAvatars().join();
        root.add("avatars", getAvatarsPaths(LocalAvatarFetcher.ALL_AVATARS));


        // -- avatar -- // 


        if (avatar == null)
            return GSON.toJson(root);

        JsonObject a = new JsonObject();

        // permissions
        JsonObject aPermissions = new JsonObject();

        aPermissions.addProperty("category", avatar.permissions.category.name);

        JsonObject standard = new JsonObject();
        for (Map.Entry<Permissions, Integer> entry : avatar.permissions.getPermissions().entrySet())
            standard.addProperty(entry.getKey().name, entry.getValue());

        aPermissions.add("standard", standard);

        JsonObject customPermissions = new JsonObject();
        for (Map.Entry<String, Map<Permissions, Integer>> entry : avatar.permissions.getCustomPermissions().entrySet()) {
            JsonObject obj = new JsonObject();
            for (Map.Entry<Permissions, Integer> entry1 : entry.getValue().entrySet())
                obj.addProperty(entry1.getKey().name, entry1.getValue());

            customPermissions.add(entry.getKey(), obj);
        }

        aPermissions.add("custom", customPermissions);

        a.add("permissions", aPermissions);

        // avatar metadata
        JsonObject aMeta = new JsonObject();

        aMeta.addProperty("version", avatar.version.toString());
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
        for (Map.Entry<String, String> entry: avatar.badgeToColor.entrySet()) {
            aMeta.addProperty(entry.getKey(), entry.getValue());
        }

        a.add("meta", aMeta);

        // avatar complexity
        JsonObject inst = new JsonObject();

        inst.addProperty("animationComplexity", avatar.animationComplexity);
        inst.addProperty("animationInstructions", avatar.animation.pre);
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

        // sounds
        JsonArray sounds = new JsonArray();

        for (String s : avatar.customSounds.keySet())
            sounds.add(s);

        a.add("sounds", sounds);

        // animations
        JsonArray animations = new JsonArray();

        for (Animation animation : avatar.animations.values())
            animations.add(animation.modelName + "/" + animation.name);

        a.add("animations", animations);

        // sizes
        if (avatar.nbt != null)
            a.add("sizes", parseNbtSizes(avatar.nbt));

        // return as string
        root.add("avatar", a);
        return GSON.toJson(root);
    }

    private static JsonObject getAvatarsPaths(List<LocalAvatarFetcher.AvatarPath> list) {
        JsonObject avatar = new JsonObject();

        for (LocalAvatarFetcher.AvatarPath path : list) {
            String name = IOUtils.getFileNameOrEmpty(path.getPath());

            if (path instanceof LocalAvatarFetcher.FolderPath folder)
                avatar.add(name, getAvatarsPaths(folder.getChildren()));
            else
                avatar.addProperty(name, path.getName());
        }

        return avatar;
    }

    private static JsonObject parseNbtSizes(CompoundTag nbt) {
        JsonObject sizes = new JsonObject();

        // metadata
        sizes.addProperty("metadata", parseSize(getBytesFromNbt(nbt.getCompound("metadata"))));

        // models
        CompoundTag modelsNbt = nbt.getCompound("models");
        ListTag childrenNbt = modelsNbt.getList("chld", Tag.TAG_COMPOUND);
        JsonObject models = parseListSize(childrenNbt, tag -> tag.getString("name"));
        sizes.add("models", models);
        sizes.addProperty("models_total", parseSize(getBytesFromNbt(modelsNbt)));

        // animations
        ListTag animationsNbt = nbt.getList("animations", Tag.TAG_COMPOUND);
        JsonObject animations = parseListSize(animationsNbt, tag -> tag.getString("mdl") + "." + tag.getString("name"));
        sizes.add("animations", animations);
        sizes.addProperty("animations_total", parseSize(getBytesFromNbt(animationsNbt)));

        // textures
        CompoundTag texturesNbt = nbt.getCompound("textures");
        CompoundTag textureSrc = texturesNbt.getCompound("src");
        JsonObject textures = parseCompoundSize(textureSrc);
        sizes.add("textures", textures);
        sizes.addProperty("textures_total", parseSize(getBytesFromNbt(texturesNbt)));

        // scripts
        CompoundTag scriptsNbt = nbt.getCompound("scripts");
        JsonElement scripts = parseCompoundSize(scriptsNbt);
        sizes.add("scripts", scripts);
        sizes.addProperty("scripts_total", parseSize(getBytesFromNbt(scriptsNbt)));

        // sounds
        CompoundTag soundsNbt = nbt.getCompound("sounds");
        JsonObject sounds = parseCompoundSize(soundsNbt);
        sizes.add("sounds", sounds);
        sizes.addProperty("sounds_total", parseSize(getBytesFromNbt(soundsNbt)));

        // total
        sizes.addProperty("total", parseSize(getBytesFromNbt(nbt)));
        return sizes;
    }

    private static int getBytesFromNbt(Tag nbt) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(baos)));
            NbtIo.writeUnnamedTag(nbt, dos);
            dos.close();

            int size = baos.size();
            baos.close();

            return size;
        } catch (Exception ignored) {
            return -1;
        }
    }

    private static String parseSize(int size) {
        return size < 1000 ? size + "b" : MathUtils.asFileSize(size) + " (" + size + "b)";
    }

    private static JsonObject parseListSize(ListTag listNbt, Function<CompoundTag, String> function) {
        JsonObject target = new JsonObject();
        HashMap<String, Integer> sizesMap = new HashMap<>();

        for (Tag tag : listNbt) {
            CompoundTag compound = (CompoundTag) tag;
            sizesMap.put(function.apply(compound), getBytesFromNbt(compound));
        }
        insertJsonSortedData(sizesMap, target);

        return target;
    }

    private static JsonObject parseCompoundSize(CompoundTag compoundNbt) {
        JsonObject target = new JsonObject();
        HashMap<String, Integer> sizesMap = new HashMap<>();

        for (String key : compoundNbt.getAllKeys())
            sizesMap.put(key, getBytesFromNbt(compoundNbt.get(key)));
        insertJsonSortedData(sizesMap, target);

        return target;
    }

    private static JsonElement parseTagRecursive(Tag tag) {
        if (tag instanceof CompoundTag compoundTag) {
            JsonObject obj = new JsonObject();
            HashMap<String, Integer> sizesMap = new HashMap<>();
            for (String key : compoundTag.getAllKeys()) {
                JsonElement value = parseTagRecursive(compoundTag.get(key));
                if (value instanceof JsonPrimitive size && size.isNumber())
                    sizesMap.put(key, size.getAsInt());
                else
                    obj.add(key, value);
            }
            insertJsonSortedData(sizesMap, obj);
            return obj;
        }
        else {
            return new JsonPrimitive(getBytesFromNbt(tag));
        }
    }

    private static void insertJsonSortedData(HashMap<String, Integer> sizesMap, JsonObject json) {
        sizesMap.entrySet().stream().sorted((Map.Entry.<String, Integer>comparingByValue().reversed())).forEach(e -> json.addProperty(e.getKey(), parseSize(e.getValue())));
    }
}
