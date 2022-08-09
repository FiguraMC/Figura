package org.moon.figura.lua.api.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

@LuaWhitelist
@LuaTypeDoc(
        name = "PlayerAPI",
        description = "player"
)
public class PlayerAPI extends LivingEntityAPI<Player> {
    public PlayerAPI(Player entity) {
        super(entity);
    }

    private String cachedModelType;

    @LuaWhitelist
    @LuaMethodDoc(description = "player.get_food")
    public int getFood() {
        return entity.getFoodData().getFoodLevel();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "player.get_saturation")
    public float getSaturation() {
        return entity.getFoodData().getSaturationLevel();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "player.get_experience_progress")
    public float getExperienceProgress() {
        return entity.experienceProgress;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "player.get_experience_level")
    public float getExperienceLevel() {
        return entity.experienceLevel;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "player.is_flying")
    public boolean isFlying() {
        return entity.getAbilities().flying;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "player.get_model_type")
    public String getModelType() {
        if (cachedModelType == null) {
            if (Minecraft.getInstance().player == null)
                return null;

            PlayerInfo info = Minecraft.getInstance().player.connection.getPlayerInfo(entity.getUUID());
            if (info == null)
                return null;

            cachedModelType = info.getModelName().toUpperCase();
        }
        return cachedModelType;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "player.get_gamemode")
    public String getGamemode() {
        if (Minecraft.getInstance().player == null)
            return null;

        PlayerInfo info = Minecraft.getInstance().player.connection.getPlayerInfo(entity.getUUID());
        if (info == null)
            return null;

        return info.getGameMode() == null ? null : info.getGameMode().getName().toUpperCase();
    }

    @Override
    public String toString() {
        return entity.getName().getString() + " (Player)";
    }
}
