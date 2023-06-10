package org.moon.figura.lua.api;

import com.mojang.blaze3d.platform.Window;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.luaj.vm2.LuaError;
import org.moon.figura.FiguraMod;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.entity.ViewerAPI;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.mixin.gui.PlayerTabOverlayAccessor;
import org.moon.figura.mixin.render.ModelManagerAccessor;
import org.moon.figura.utils.EntityUtils;
import org.moon.figura.utils.LuaUtils;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.Version;

import java.text.SimpleDateFormat;
import java.util.*;

@LuaWhitelist
@LuaTypeDoc(
        name = "ClientAPI",
        value = "client"
)
public class ClientAPI {

    public static final ClientAPI INSTANCE = new ClientAPI();
    private static final HashMap<String, Boolean> LOADED_MODS = new HashMap<>();
    private static final boolean HAS_IRIS = FabricLoader.getInstance().isModLoaded("iris"); //separated to avoid indexing the list every frame

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
        return SharedConstants.getCurrentVersion().getId();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_version_name")
    public static String getVersionName() {
        return SharedConstants.getCurrentVersion().getName();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.is_snapshot")
    public static boolean isSnapshot() {
        return !SharedConstants.getCurrentVersion().isStable();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_client_brand")
    public static String getClientBrand() {
        return ClientBrandRetriever.getClientModName();
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
        var quaternion = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        Vector3f vec = new Vector3f();
        quaternion.getEulerAnglesYXZ(vec);
        double f = 180d / Math.PI;
        return FiguraVec3.fromVec3f(vec).multiply(f, -f, f); //degrees, and negate y
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_camera_dir")
    public static FiguraVec3 getCameraDir() {
        return FiguraVec3.fromVec3f(Minecraft.getInstance().gameRenderer.getMainCamera().getLookVector());
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
    public static int getTextHeight(String text) {
        int lineHeight = Minecraft.getInstance().font.lineHeight;
        return text == null ? lineHeight : lineHeight * TextUtils.splitText(TextUtils.tryParseJson(text), "\n").size();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "text"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, Integer.class, Boolean.class},
                            argumentNames = {"text", "maxWidth", "wrap"}
                    )
            },
            value = "client.get_text_dimensions"
    )
    public static FiguraVec2 getTextDimensions(@LuaNotNil String text, int maxWidth, Boolean wrap) {
        Component component = TextUtils.tryParseJson(text);
        Font font = Minecraft.getInstance().font;
        List<Component> list = TextUtils.formatInBounds(component, font, maxWidth, wrap == null || wrap);
        int x = TextUtils.getWidth(list, font);
        int y = list.size() * font.lineHeight;
        return FiguraVec2.of(x, y);
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_active_lang")
    public static String getActiveLang() {
        return Minecraft.getInstance().options.languageCode;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "modID"
            ),
            value = "client.is_mod_loaded"
    )
    public static boolean isModLoaded(String id) {
        LOADED_MODS.putIfAbsent(id, FabricLoader.getInstance().isModLoaded(id));
        return LOADED_MODS.get(id);
    }

    @LuaWhitelist
    @LuaMethodDoc("client.has_iris")
    public static boolean hasIris() {
        return HAS_IRIS;
    }

    @LuaWhitelist
    @LuaMethodDoc("client.has_iris_shader")
    public static boolean hasIrisShader() {
        return HAS_IRIS && net.irisshaders.iris.api.v0.IrisApi.getInstance().isShaderPackInUse();
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
        ResourceLocation resource = LuaUtils.parsePath(path);
        try {
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
    public static ViewerAPI getViewer() {
        return new ViewerAPI(Minecraft.getInstance().player);
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_server_data")
    public static Map<String, String> getServerData() {
        Map<String, String> map = new HashMap<>();

        IntegratedServer iServer = Minecraft.getInstance().getSingleplayerServer();
        if (iServer != null) {
            map.put("name", iServer.getWorldData().getLevelName());
            map.put("ip", iServer.getLocalIp());
            map.put("motd", iServer.getMotd());
            return map;
        }

        ServerData mServer = Minecraft.getInstance().getCurrentServer();
        if (mServer != null) {
            map.put("name", mServer.name);
            map.put("ip", mServer.ip);
            map.put("motd", mServer.motd.getString());
        }

        return map;
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_date")
    public static Map<String, Object> getDate() {
        Map<String, Object> map = new HashMap<>();

        Calendar calendar = FiguraMod.CALENDAR;
        Date date = new Date();
        calendar.setTime(date);

        map.put("day", calendar.get(Calendar.DAY_OF_MONTH));
        map.put("month", calendar.get(Calendar.MONTH) + 1);
        map.put("year", calendar.get(Calendar.YEAR));
        map.put("hour", calendar.get(Calendar.HOUR_OF_DAY));
        map.put("minute", calendar.get(Calendar.MINUTE));
        map.put("second", calendar.get(Calendar.SECOND));
        map.put("millisecond", calendar.get(Calendar.MILLISECOND));
        map.put("week", calendar.get(Calendar.WEEK_OF_YEAR));
        map.put("year_day", calendar.get(Calendar.DAY_OF_YEAR));
        map.put("week_day", calendar.get(Calendar.DAY_OF_WEEK));
        map.put("daylight_saving", calendar.getTimeZone().inDaylightTime(date));

        SimpleDateFormat format = new SimpleDateFormat("Z|zzzz|G|MMMM|EEEE", Locale.US);
        String[] f = format.format(date).split("\\|");

        map.put("timezone", f[0]);
        map.put("timezone_name", f[1]);
        map.put("era", f[2]);
        map.put("month_name", f[3]);
        map.put("day_name", f[4]);

        return map;
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_frame_time")
    public static double getFrameTime() {
        return Minecraft.getInstance().getFrameTime();
    }

    @LuaWhitelist
    @LuaMethodDoc("client.list_atlases")
    public static List<String> listAtlases() {
        List<String> list = new ArrayList<>();
        for (ResourceLocation res : ModelManagerAccessor.getVanillaAtlases().keySet())
            list.add(res.toString());
        return list;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "path"
            ),
            value = "client.get_atlas"
    )
    public static TextureAtlasAPI getAtlas(@LuaNotNil String atlas) {
        ResourceLocation path = LuaUtils.parsePath(atlas);
        try {
            return new TextureAtlasAPI(Minecraft.getInstance().getModelManager().getAtlas(path));
        } catch (Exception ignored) {
            return null;
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("client.get_tab_list")
    public static Map<String, Object> getTabList() {
        Map<String, Object> map = new HashMap<>();
        PlayerTabOverlayAccessor accessor = (PlayerTabOverlayAccessor) Minecraft.getInstance().gui.getTabList();

        //header
        Component header = accessor.getHeader();
        if (header != null) {
            map.put("header", header.getString());
            map.put("headerJson", header);
        }

        //players
        List<String> list = new ArrayList<>();
        for (PlayerInfo entry : EntityUtils.getTabList())
            list.add(entry.getTabListDisplayName() != null ? entry.getTabListDisplayName().getString() : entry.getProfile().getName());
        map.put("players", list);

        //footer
        Component footer = accessor.getFooter();
        if (footer != null) {
            map.put("footer", footer.getString());
            map.put("footerJson", footer);
        }

        return map;
    }



    @Override
    public String toString() {
        return "ClientAPI";
    }
}
