package org.moon.figura.avatar;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.local.LocalAvatarLoader;
import org.moon.figura.backend2.NetworkStuff;
import org.moon.figura.gui.widgets.lists.AvatarList;
import org.moon.figura.utils.EntityUtils;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all the avatars that are currently loaded in memory, and also
 * handles getting the avatars of entities. If an entity does not have a loaded avatar,
 * the AvatarManager will fetch the avatar and cache it.
 */
public class AvatarManager {

    private static final Map<UUID, UserData> LOADED_USERS = new ConcurrentHashMap<>();
    private static final Set<UUID> FETCHED_USERS = new HashSet<>();

    private static final Map<Entity, Avatar> LOADED_CEM = new ConcurrentHashMap<>();

    public static boolean localUploaded = true; //init as true :3
    public static boolean panic = false;

    // -- avatar events -- //

    public static void tickLoadedAvatars() {
        if (panic)
            return;

        //unload avatars from disconnected players
        //needs to actually be an event, otherwise some things like skulls will try to download the avatar every tick
        /*
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        Set<UUID> toRemove = new HashSet<>();
        for (UUID id : LOADED_AVATARS.keySet()) {
            if (connection != null && connection.getPlayerInfo(id) == null)
                toRemove.add(id);
        }
        for (UUID id : toRemove)
            clearAvatar(id);
        */

        //tick the avatars
        for (UserData user : LOADED_USERS.values()) {
            Avatar avatar = user.getMainAvatar();
            if (avatar != null) {
                FiguraMod.pushProfiler(avatar);
                avatar.tick();
                FiguraMod.popProfiler();
            }
        }

        for (Avatar avatar : LOADED_CEM.values()) {
            if (avatar != null) {
                FiguraMod.pushProfiler(avatar);
                avatar.tick();
                FiguraMod.popProfiler();
            }
        }
    }

    public static void onWorldRender(float tickDelta) {
        if (panic)
            return;

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler("worldRender");

        for (UserData user : LOADED_USERS.values()) {
            Avatar avatar = user.getMainAvatar();
            if (avatar != null) {
                FiguraMod.pushProfiler(avatar);
                avatar.render(tickDelta);
                FiguraMod.popProfiler();
            }
        }

        for (Avatar avatar : LOADED_CEM.values()) {
            if (avatar != null) {
                FiguraMod.pushProfiler(avatar);
                avatar.render(tickDelta);
                FiguraMod.popProfiler();
            }
        }

        FiguraMod.popProfiler(2);
    }

    public static void afterWorldRender(float tickDelta) {
        if (panic)
            return;

        FiguraMod.pushProfiler(FiguraMod.MOD_ID );
        FiguraMod.pushProfiler("postWorldRender");

        for (UserData user : LOADED_USERS.values()) {
            Avatar avatar = user.getMainAvatar();
            if (avatar != null) {
                FiguraMod.pushProfiler(avatar);
                avatar.postWorldRenderEvent(tickDelta);
                FiguraMod.popProfiler();
            }
        }

        for (Avatar avatar : LOADED_CEM.values()) {
            if (avatar != null) {
                FiguraMod.pushProfiler(avatar);
                avatar.postWorldRenderEvent(tickDelta);
                FiguraMod.popProfiler();
            }
        }

        FiguraMod.popProfiler(2);
    }

    public static void applyAnimations() {
        if (panic)
            return;

        for (UserData user : LOADED_USERS.values()) {
            Avatar avatar = user.getMainAvatar();
            if (avatar != null) {
                FiguraMod.pushProfiler(avatar);
                avatar.applyAnimations();
                FiguraMod.popProfiler();
            }
        }

        for (Avatar avatar : LOADED_CEM.values()) {
            if (avatar != null) {
                FiguraMod.pushProfiler(avatar);
                avatar.applyAnimations();
                FiguraMod.popProfiler();
            }
        }
    }

    public static void clearAnimations() {
        if (panic)
            return;

        for (UserData user : LOADED_USERS.values()) {
            Avatar avatar = user.getMainAvatar();
            if (avatar != null) {
                FiguraMod.pushProfiler(avatar);
                avatar.clearAnimations();
                FiguraMod.popProfiler();
            }
        }

        for (Avatar avatar : LOADED_CEM.values()) {
            if (avatar != null) {
                FiguraMod.pushProfiler(avatar);
                avatar.clearAnimations();
                FiguraMod.popProfiler();
            }
        }
    }

    // -- avatar getters -- //

    //player will also attempt to load from network, if possible
    public static Avatar getAvatarForPlayer(UUID player) {
        if (panic || Minecraft.getInstance().level == null)
            return null;

        fetchBackend(player);

        UserData user = LOADED_USERS.get(player);
        return user == null ? null : user.getMainAvatar();
    }

    //tries to get data from an entity
    public static Avatar getAvatar(Entity entity) {
        if (panic || Minecraft.getInstance().level == null)
            return null;

        UUID uuid = entity.getUUID();

        //load from player (fetch backend) if is a player
        if (entity instanceof Player)
            return getAvatarForPlayer(uuid);

        //TODO - CEM
        //otherwise, returns the avatar from the entity pool (cem)
        return LOADED_CEM.get(entity);
    }

    //get a loaded avatar without fetching backend or creating a new one
    public static Avatar getLoadedAvatar(UUID owner) {
        if (panic || Minecraft.getInstance().level == null)
            return null;

        UserData user = LOADED_USERS.get(owner);
        return user == null ? null : user.getMainAvatar();
    }

    //get all main loaded avatars
    public static List<Avatar> getLoadedAvatars() {
        List<Avatar> list = new ArrayList<>();
        for (UserData user : LOADED_USERS.values()) {
            Avatar avatar = user.getMainAvatar();
            if (avatar != null && avatar.nbt != null)
                list.add(avatar);
        }
        return list;
    }

    // -- avatar management -- //

    //removes an loaded avatar
    public static void clearAvatars(UUID id) {
        FETCHED_USERS.remove(id);

        UserData user = LOADED_USERS.get(id);
        if (user != null) user.clear();

        NetworkStuff.clear(id);
        NetworkStuff.unsubscribe(id);
        FiguraMod.debug("Cleared avatars of " + id);
    }

    private static void clearCEMAvatars() {
        for (Avatar avatar : LOADED_CEM.values())
            avatar.clean();
        LOADED_CEM.clear();
    }

    //clears ALL loaded avatars, including local
    public static void clearAllAvatars() {
        for (UUID id : LOADED_USERS.keySet())
            clearAvatars(id);

        clearCEMAvatars();

        localUploaded = true;
        AvatarList.selectedEntry = null;
        FiguraMod.LOGGER.info("Cleared all avatars");
    }

    //reloads an avatar
    public static void reloadAvatar(UUID id) {
        if (!localUploaded && FiguraMod.isLocal(id))
            loadLocalAvatar(LocalAvatarLoader.getLastLoadedPath());
        else
            clearAvatars(id);
    }

    //load the local player avatar
    public static void loadLocalAvatar(Path path) {
        UUID id = FiguraMod.getLocalPlayerUUID();

        //clear
        clearAvatars(id);
        FETCHED_USERS.add(id);

        //load
        UserData user = LOADED_USERS.computeIfAbsent(id, UserData::new);
        LocalAvatarLoader.loadAvatar(path, user);

        //mark as not uploaded
        localUploaded = false;
    }

    //set an user's avatar
    public static void setAvatar(UUID id, CompoundTag nbt) {
        try {
            UserData user = LOADED_USERS.computeIfAbsent(id, UserData::new);
            user.clear();
            user.loadAvatar(nbt);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to set avatar for " + id, e);
        }
    }

    //get avatar from the backend
    private static void fetchBackend(UUID id) {
        if (FETCHED_USERS.contains(id))
            return;

        FETCHED_USERS.add(id);

        UserData user = LOADED_USERS.computeIfAbsent(id, UserData::new);

        FiguraMod.debug("Getting userdata for " + id);
        NetworkStuff.getUser(user);
    }

    // -- badges -- //

    public static Pair<BitSet, BitSet> getBadges(UUID id) {
        UserData user = LOADED_USERS.get(id);
        if (user == null)
            return null;

        Pair<BitSet, BitSet> badges = user.getBadges();
        if (badges != null)
            return badges;

        badges = Badges.emptyBadges();
        user.loadBadges(badges);
        return badges;
    }

    // -- command -- //

    public static LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
        //root
        LiteralArgumentBuilder<FabricClientCommandSource> root = LiteralArgumentBuilder.literal("setAvatar");

        //source
        RequiredArgumentBuilder<FabricClientCommandSource, String> target = RequiredArgumentBuilder.argument("target", StringArgumentType.word());

        //target
        RequiredArgumentBuilder<FabricClientCommandSource, String> source = RequiredArgumentBuilder.argument("source", StringArgumentType.word());
        source.executes(context -> {
            String s = StringArgumentType.getString(context, "source");
            String t = StringArgumentType.getString(context, "target");

            UUID sourceUUID, targetUUID;
            try {
                sourceUUID = UUID.fromString(s);
                targetUUID = UUID.fromString(t);
            } catch (Exception e) {
                context.getSource().sendError(Component.literal("Failed to parse uuids"));
                return 0;
            }

            UserData user = LOADED_USERS.get(sourceUUID);
            Avatar avatar = user == null ? null : user.getMainAvatar();
            if (avatar == null || avatar.nbt == null) {
                context.getSource().sendError(Component.literal("No source Avatar found"));
                return 0;
            }

            if (LOADED_USERS.get(targetUUID) != null) {
                setAvatar(targetUUID, avatar.nbt);
                context.getSource().sendFeedback(Component.literal("Set avatar for " + t));
                return 1;
            }

            Entity targetEntity = EntityUtils.getEntityByUUID(targetUUID);
            if (targetEntity == null) {
                context.getSource().sendError(Component.literal("Target entity not found"));
                return 0;
            }

            try {
                Avatar targetAvatar = new Avatar(targetEntity);
                targetAvatar.load(avatar.nbt);
                LOADED_CEM.put(targetEntity, targetAvatar);
                return 1;
            } catch (Exception e) {
                context.getSource().sendError(Component.literal("Failed to load avatar"));
                return 0;
            }
        });
        target.then(source);

        //build root
        root.then(target);
        return root;
    }
}
