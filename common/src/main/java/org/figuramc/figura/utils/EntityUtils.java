package org.figuramc.figura.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.mixin.ClientLevelInvoker;
import org.figuramc.figura.mixin.EntityAccessor;
import org.figuramc.figura.mixin.gui.PlayerTabOverlayAccessor;

import java.util.*;

public class EntityUtils {

    protected static Map<UUID, Integer> uuidToNetworkID = new HashMap<>();
    private static Level level;
    public static Entity getEntityByUUID(UUID uuid) {
        if (Minecraft.getInstance().level == null)
            return null;
        // Invalidate if the level has changed
        if (level == null)
            level = Minecraft.getInstance().level;
        if (level != Minecraft.getInstance().level)
            uuidToNetworkID.clear();

        if (uuidToNetworkID.containsKey(uuid))
            return Minecraft.getInstance().level.getEntity(uuidToNetworkID.get(uuid));

        for (Entity entity : ((ClientLevelInvoker) Minecraft.getInstance().level).getEntityIds().values())
            if (uuid.equals(entity.getUUID())) {
                uuidToNetworkID.put(uuid, entity.getId());
                return entity;
            }

        return null;
    }

    public static Entity getViewedEntity(float distance) {
        Entity entity = Minecraft.getInstance().getCameraEntity();
        if (entity == null) return null;

        float tickDelta = Minecraft.getInstance().getFrameTime();
        Vec3 entityEye = entity.getEyePosition(tickDelta);
        Vec3 viewVec = entity.getViewVector(tickDelta).scale(distance);
        AABB box = entity.getBoundingBox().expandTowards(viewVec).inflate(1f, 1f, 1f);

        Vec3 raycastEnd = entityEye.add(viewVec);

        double raycastDistanceSquared; // Has to be squared for some reason, thanks minecraft for not making that clear
        BlockHitResult blockResult = ((EntityAccessor) entity).getLevel().clip(new ClipContext(entityEye, raycastEnd, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));
        if (blockResult != null)
            raycastDistanceSquared = blockResult.getLocation().distanceToSqr(entityEye);
        else
            raycastDistanceSquared = distance * distance;

        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity, entityEye, raycastEnd, box, entity1 -> !entity1.isSpectator() && entity1.isPickable(), raycastDistanceSquared);
        if (entityHitResult != null)
            return entityHitResult.getEntity();
        return null;
    }

    public static PlayerInfo getPlayerInfo(UUID uuid) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection == null ? null : connection.getPlayerInfo(uuid);
    }

    public static String getNameForUUID(UUID uuid) {
        PlayerInfo player = getPlayerInfo(uuid);
        if (player != null)
            return player.getProfile().getName();

        Entity e = getEntityByUUID(uuid);
        if (e != null)
            return e.getName().getString();

        return null;
    }

    public static Map<String, UUID> getPlayerList() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null || connection.getOnlinePlayerIds().isEmpty())
            return new HashMap<>();

        Map<String, UUID> playerList = new HashMap<>();

        for (UUID uuid : connection.getOnlinePlayerIds()) {
            PlayerInfo player = connection.getPlayerInfo(uuid);
            if (player != null)
                playerList.put(player.getProfile().getName(), uuid);
        }

        return playerList;
    }

    public static List<PlayerInfo> getTabList() {
        ClientPacketListener clientPacketListener = Minecraft.getInstance().getConnection();
        if (clientPacketListener == null)
            return new ArrayList<>();

        return PlayerTabOverlayAccessor.getPlayerOrdering().sortedCopy(clientPacketListener.getOnlinePlayers());
    }

    public static boolean checkInvalidPlayer(UUID id) {
        if (id.version() != 4)
            return true;

        PlayerInfo playerInfo = getPlayerInfo(id);
        if (playerInfo == null)
            return false;

        GameProfile profile = playerInfo.getProfile();
        String name = profile.getName();
        return name != null && (name.trim().isEmpty() || name.charAt(0) == '\u0000');
    }

    public static boolean isEntityUpsideDown(LivingEntity livingEntity) {
        String string;
        if ((livingEntity instanceof Player || livingEntity.hasCustomName()) && ("Dinnerbone".equals(string = ChatFormatting.stripFormatting(livingEntity.getName().getString())) || "Grumm".equals(string))) {
            return !(livingEntity instanceof Player) || ((Player)livingEntity).isModelPartShown(PlayerModelPart.CAPE);
        }
        return false;
    }
}
