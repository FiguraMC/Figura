package org.moon.figura.lua.api;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.moon.figura.FiguraMod;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.entity.EntityWrapper;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;
import org.moon.figura.utils.TextUtils;

import java.util.UUID;

@LuaWhitelist
@LuaTypeDoc(
        name = "HostAPI",
        description = "host"
)
public class HostAPI {

    private final UUID owner;
    private final Minecraft minecraft;

    public HostAPI(UUID owner) {
        this.owner = owner;
        this.minecraft = Minecraft.getInstance();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = HostAPI.class,
                    argumentNames = "host"
            ),
            description = "host.is_host"
    )
    private static boolean isHost(HostAPI api) {
        return api != null && api.owner.compareTo(FiguraMod.getLocalPlayerUUID()) == 0;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = HostAPI.class,
                    argumentNames = "host"
            ),
            description = "host.get_targeted_entity"
    )
    public static EntityWrapper<?> getTargetedEntity(HostAPI api) {
        if (!isHost(api)) return null;
        Entity entity = api.minecraft.crosshairPickEntity;

        if (entity != null && Minecraft.getInstance().player != null && !entity.isInvisibleTo(Minecraft.getInstance().player))
            return EntityWrapper.fromEntity(entity);

        return null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {HostAPI.class, FiguraVec3.class},
                            argumentNames = {"host", "timesData"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {HostAPI.class, Integer.class, Integer.class, Integer.class},
                            argumentNames = {"host", "fadeInTime", "stayTime", "fadeOutTime"}
                    )
            },
            description = "host.set_title_times"
    )
    public static void setTitleTimes(HostAPI api, Object x, Double y, Double z) {
        if (!isHost(api)) return;
        FiguraVec3 times = LuaUtils.parseVec3("setTitleTimes", x, y, z);
        api.minecraft.gui.setTimes((int) times.x, (int) times.y, (int) times.z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = HostAPI.class,
                    argumentNames = "host"
            ),
            description = "host.clear_title"
    )
    public static void clearTitle(HostAPI api) {
        if (!isHost(api)) return;
        api.minecraft.gui.clear();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {HostAPI.class, String.class},
                    argumentNames = {"host", "text"}
            ),
            description = "host.set_title"
    )
    public static void setTitle(HostAPI api, String text) {
        if (!isHost(api)) return;
        api.minecraft.gui.setTitle(TextUtils.tryParseJson(text));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {HostAPI.class, String.class},
                    argumentNames = {"host", "text"}
            ),
            description = "host.set_subtitle"
    )
    public static void setSubtitle(HostAPI api, String text) {
        if (!isHost(api)) return;
        api.minecraft.gui.setSubtitle(TextUtils.tryParseJson(text));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {HostAPI.class, String.class},
                            argumentNames = {"host", "text"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {HostAPI.class, String.class, Boolean.class},
                            argumentNames = {"host", "text", "animated"}
                    )
            },
            description = "host.set_actionbar"
    )
    public static void setActionbar(HostAPI api, String text, Boolean animated) {
        if (!isHost(api)) return;
        if (animated == null) animated = false;
        api.minecraft.gui.setOverlayMessage(TextUtils.tryParseJson(text), animated);
    }

    @Override
    public String toString() {
        return "HostAPI";
    }
}
