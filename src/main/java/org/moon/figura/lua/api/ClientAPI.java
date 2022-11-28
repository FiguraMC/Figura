package org.moon.figura.lua.api;

import com.mojang.blaze3d.platform.Window;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.phys.Vec3;
import org.luaj.vm2.LuaError;
import org.moon.figura.FiguraMod;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.entity.EntityAPI;
import org.moon.figura.lua.api.entity.PlayerAPI;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.MathUtils;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.Version;

import java.util.*;

@LuaWhitelist
@LuaTypeDoc(
        name = "ClientAPI",
        value = "client"
)
public class ClientAPI {

    public static final ClientAPI INSTANCE = new ClientAPI();
    private static final boolean hasIris = FabricLoader.getInstance().isModLoaded("iris");

    @LuaWhitelist
    @LuaMethodDoc("client.get_fps")
    public static int getFPS() {
        String s = getFPSString();
        if (s.length() == 0)
            return 0;
        return Integer.parseInt(s.split(" ")[0]);
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_fps_string")
    public static String getFPSString() {
        return Minecraft.getInstance().fpsString;
    }

    @LuaWhitelist
    @LuaMethodDoc("client.is_paused")
    public static boolean isPaused() {
        return Minecraft.getInstance().isPaused();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_version")
    public static String getVersion() {
        return Minecraft.getInstance().getLaunchedVersion();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_version_type")
    public static String getVersionType() {
        return Minecraft.getInstance().getVersionType();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_server_brand")
    public static String getServerBrand() {
        if (Minecraft.getInstance().player == null)
            return null;

        return Minecraft.getInstance().getSingleplayerServer() == null ? Minecraft.getInstance().player.getServerBrand() : "Integrated";
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_chunk_statistics")
    public static String getChunkStatistics() {
        return Minecraft.getInstance().levelRenderer.getChunkStatistics();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_entity_statistics")
    public static String getEntityStatistics() {
        return Minecraft.getInstance().levelRenderer.getEntityStatistics();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_sound_statistics")
    public static String getSoundStatistics() {
        return Minecraft.getInstance().getSoundManager().getDebugString();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_entity_count")
    public static int getEntityCount() {
        if (Minecraft.getInstance().level == null)
            return 0;

        return Minecraft.getInstance().level.getEntityCount();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_particle_count")
    public static String getParticleCount() {
        return Minecraft.getInstance().particleEngine.countParticles();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_current_effect")
    public static String getCurrentEffect() {
        if (Minecraft.getInstance().gameRenderer.currentEffect() == null)
            return null;

        return Minecraft.getInstance().gameRenderer.currentEffect().getName();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_java_version")
    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_used_memory")
    public static long getUsedMemory() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_max_memory")
    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_allocated_memory")
    public static long getAllocatedMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.is_window_focused")
    public static boolean isWindowFocused() {
        return Minecraft.getInstance().isWindowActive();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.is_hud_enabled")
    public static boolean isHudEnabled() {
        return Minecraft.renderNames();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.is_debug_overlay_enabled")
    public static boolean isDebugOverlayEnabled() {
        return Minecraft.getInstance().options.renderDebug;
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_window_size")
    public static FiguraVec2 getWindowSize() {
        Window window = Minecraft.getInstance().getWindow();
        return FiguraVec2.of(window.getWidth(), window.getHeight());
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_fov")
    public static double getFOV() {
        return Minecraft.getInstance().options.fov().get();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_system_time")
    public static long getSystemTime() {
        return System.currentTimeMillis();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_mouse_pos")
    public static FiguraVec2 getMousePos() {
        MouseHandler mouse = Minecraft.getInstance().mouseHandler;
        return FiguraVec2.of(mouse.xpos(), mouse.ypos());
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_scaled_window_size")
    public static FiguraVec2 getScaledWindowSize() {
        Window window = Minecraft.getInstance().getWindow();
        return FiguraVec2.of(window.getGuiScaledWidth(), window.getGuiScaledHeight());
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_gui_scale")
    public static double getGuiScale() {
        return Minecraft.getInstance().getWindow().getGuiScale();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_camera_pos")
    public static FiguraVec3 getCameraPos() {
        Vec3 pos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        return FiguraVec3.fromVec3(pos);
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_camera_rot")
    public static FiguraVec3 getCameraRot() {
        double f = 180d / Math.PI;
        return MathUtils.quaternionToYXZ(Minecraft.getInstance().gameRenderer.getMainCamera().rotation()).multiply(f, -f, f); //degrees, and negate y
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "text"
            ),
            value = "client.get_text_width"
    )
    public static int getTextWidth(@LuaNotNil String text) {
        return TextUtils.getWidth(TextUtils.splitText(TextUtils.tryParseJson(text), "\n"), Minecraft.getInstance().font);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "text"
            ),
            value = "client.get_text_height"
    )
    public static int getTextHeight(@LuaNotNil String text) {
        return Minecraft.getInstance().font.lineHeight * TextUtils.splitText(TextUtils.tryParseJson(text), "\n").size();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_active_lang")
    public static String getActiveLang() {
        return Minecraft.getInstance().options.languageCode;
    }

    @LuaWhitelist
    @LuaMethodDoc("client.has_iris")
    public static boolean hasIris() {
        return hasIris;
    }

    @LuaWhitelist
    @LuaMethodDoc("client.has_iris_shader")
    public static boolean hasIrisShader() {
        return hasIris && net.irisshaders.iris.api.v0.IrisApi.getInstance().isShaderPackInUse();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "path"
            ),
            value = "client.has_resource"
    )
    public static boolean hasResource(@LuaNotNil String path) {
        try {
            ResourceLocation resource = new ResourceLocation(path);
            return Minecraft.getInstance().getResourceManager().getResource(resource).isPresent();
        } catch (Exception ignored) {
            return false;
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_active_resource_packs")
    public static List<String> getActiveResourcePacks() {
        List<String> list = new ArrayList<>();

        for (Pack pack : Minecraft.getInstance().getResourcePackRepository().getSelectedPacks())
            list.add(pack.getTitle().getString());

        return list;
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_figura_version")
    public static String getFiguraVersion() {
        return FiguraMod.VERSION.toString();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {String.class, String.class},
                    argumentNames = {"version1", "version2"}
            ),
            value = "client.compare_versions")
    public static int compareVersions(@LuaNotNil String ver1, @LuaNotNil String ver2) {
        Version v1 = new Version(ver1);
        Version v2 = new Version(ver2);

        if (v1.invalid)
            throw new LuaError("Cannot parse version " + "\"" + ver1 + "\"");
        if (v2.invalid)
            throw new LuaError("Cannot parse version " + "\"" + ver2 + "\"");

        return v1.compareTo(v2);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Integer.class, Integer.class, Integer.class, Integer.class},
                    argumentNames = {"a", "b", "c", "d"}
            ),
            value = "client.int_uuid_to_string")
    public static String intUUIDToString(int a, int b, int c, int d) {
        try {
            UUID uuid = UUIDUtil.uuidFromIntArray(new int[]{a, b, c, d});
            return uuid.toString();
        } catch (Exception ignored) {
            throw new LuaError("Failed to parse uuid");
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_viewer")
    public static EntityAPI<?> getViewer() {
        return PlayerAPI.wrap(Minecraft.getInstance().player);
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_server_data")
    public static Map<String, String> getServerData() {
        Map<String, String> list = new HashMap<>();

        IntegratedServer iServer = Minecraft.getInstance().getSingleplayerServer();
        if (iServer != null) {
            list.put("name", iServer.getWorldData().getLevelName());
            list.put("ip", iServer.getLocalIp());
            list.put("motd", iServer.getMotd());
            return list;
        }

        ServerData mServer = Minecraft.getInstance().getCurrentServer();
        if (mServer != null) {
            list.put("name", mServer.name);
            list.put("ip", mServer.ip);
            list.put("motd", mServer.motd.getString());
        }

        return list;
    }

    @Override
    public String toString() {
        return "ClientAPI";
    }
}
