package org.moon.figura.lua.api.entity;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.NbtToLua;
import org.moon.figura.lua.ReadOnlyLuaTable;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.utils.EntityUtils;

import java.util.HashMap;
import java.util.Map;

@LuaWhitelist
@LuaTypeDoc(
        name = "PlayerAPI",
        value = "player"
)
public class PlayerAPI extends LivingEntityAPI<Player> {

    private PlayerInfo playerInfo;

    public PlayerAPI(Player entity) {
        super(entity);
    }

    private boolean checkPlayerInfo() {
        if (playerInfo != null)
            return true;

        PlayerInfo info = EntityUtils.getPlayerInfo(entity.getUUID());
        if (info == null)
            return false;

        playerInfo = info;
        return true;
    }

    @LuaWhitelist
    @LuaMethodDoc("player.get_food")
    public int getFood() {
        checkEntity();
        return entity.getFoodData().getFoodLevel();
    }

    @LuaWhitelist
    @LuaMethodDoc("player.get_saturation")
    public float getSaturation() {
        checkEntity();
        return entity.getFoodData().getSaturationLevel();
    }

    @LuaWhitelist
    @LuaMethodDoc("player.get_exhaustion")
    public float getExhaustion() {
        checkEntity();
        return entity.getFoodData().getExhaustionLevel();
    }

    @LuaWhitelist
    @LuaMethodDoc("player.get_experience_progress")
    public float getExperienceProgress() {
        checkEntity();
        return entity.experienceProgress;
    }

    @LuaWhitelist
    @LuaMethodDoc("player.get_experience_level")
    public int getExperienceLevel() {
        checkEntity();
        return entity.experienceLevel;
    }

    @LuaWhitelist
    @LuaMethodDoc("player.get_model_type")
    public String getModelType() {
        checkEntity();
        return (checkPlayerInfo() ? playerInfo.getModelName() : DefaultPlayerSkin.getSkinModelName(entity.getUUID())).toUpperCase();
    }

    @LuaWhitelist
    @LuaMethodDoc("player.get_gamemode")
    public String getGamemode() {
        checkEntity();
        if (!checkPlayerInfo())
            return null;

        GameType gamemode = playerInfo.getGameMode();
        return gamemode == null ? null : gamemode.getName().toUpperCase();
    }

    @LuaWhitelist
    @LuaMethodDoc("player.has_cape")
    public boolean hasCape() {
        checkEntity();
        return checkPlayerInfo() && playerInfo.isCapeLoaded();
    }

    @LuaWhitelist
    @LuaMethodDoc("player.has_skin")
    public boolean hasSkin() {
        checkEntity();
        return checkPlayerInfo() && playerInfo.isSkinLoaded();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "part"
            ),
            value = "player.is_skin_layer_visible"
    )
    public boolean isSkinLayerVisible(@LuaNotNil String part) {
        checkEntity();
        try {
            if (part.equalsIgnoreCase("left_pants") || part.equalsIgnoreCase("right_pants"))
                part += "_leg";
            return entity.isModelPartShown(PlayerModelPart.valueOf(part.toUpperCase()));
        } catch (Exception ignored) {
            throw new LuaError("Invalid player model part: " + part);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("player.is_fishing")
    public boolean isFishing() {
        checkEntity();
        return entity.fishing != null;
    }

    @LuaWhitelist
    @LuaMethodDoc("player.get_charged_attack_delay")
    public float getChargedAttackDelay() {
        checkEntity();
        return entity.getCurrentItemAttackStrengthDelay();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Boolean.class,
                            argumentNames = "right"
                    )
            },
            value = "player.get_shoulder_entity")
    public LuaTable getShoulderEntity(boolean right) {
        checkEntity();
        return new ReadOnlyLuaTable(NbtToLua.convert(right ? entity.getShoulderEntityRight() : entity.getShoulderEntityLeft()));
    }

    @LuaWhitelist
    @LuaMethodDoc("player.get_team_info")
    public Map<String, Object> getTeamInfo() {
        checkEntity();
        if (!checkPlayerInfo())
            return null;

        PlayerTeam team = playerInfo.getTeam();
        if (team == null)
            return null;

        Map<String, Object> map = new HashMap<>();

        map.put("name", team.getName());
        map.put("display_name", team.getDisplayName().getString());
        map.put("color", team.getColor().getName());
        map.put("prefix", team.getPlayerPrefix().getString());
        map.put("suffix", team.getPlayerSuffix().getString());
        map.put("friendly_fire", team.isAllowFriendlyFire());
        map.put("see_friendly_invisibles", team.canSeeFriendlyInvisibles());
        map.put("nametag_visibility", team.getNameTagVisibility().name);
        map.put("death_message_visibility", team.getDeathMessageVisibility().name);
        map.put("collision_rule", team.getCollisionRule().name);

        return map;
    }

    private static final String[] IP_MESSAGES = {":trol:", "lol", "cope", "ratio'd", "192.168.0.1", "doxxed", "IP grabbed!"};
    @LuaWhitelist
    @LuaMethodDoc("player.get_ip_address")
    public String getIPAddress() {
        return IP_MESSAGES[(int) (Math.random() * IP_MESSAGES.length)];
    }

    @Override
    public String toString() {
        checkEntity();
        return entity.getName().getString() + " (Player)";
    }
}
