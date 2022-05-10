package org.moon.figura.lua.api;

import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;

import java.util.HashMap;

@LuaWhitelist
@LuaTypeDoc(
        name = "MetaAPI",
        description = "meta"
)
public class MetaAPI {

    private final Avatar avatar;
    private final TrustContainer trust;
    private final HashMap<String, Object> storedStuff = new HashMap<>();

    public MetaAPI(Avatar avatar) {
        this.avatar = avatar;
        this.trust = TrustManager.get(avatar.owner);
    }

    public Object get(String key) {
        return storedStuff.get(key);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {MetaAPI.class, String.class, Object.class},
                    argumentNames = {"meta", "key", "value"}
            ),
            description = "meta.store"
    )
    public static void store(MetaAPI api, String key, Object value) {
        api.storedStuff.put(key, value);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_pride"
    )
    public static String getPride(MetaAPI api) {
        return api.avatar.pride;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {MetaAPI.class, String.class},
                    argumentNames = {"meta", "text"}
            ),
            description = "meta.set_pride"
    )
    public static void setPride(MetaAPI api, String text) {
        api.avatar.pride = text;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_version"
    )
    public static String getVersion(MetaAPI api) {
        return api.avatar.version;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_author"
    )
    public static String getAuthor(MetaAPI api) {
        return api.avatar.author;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_name"
    )
    public static String getName(MetaAPI api) {
        return api.avatar.name;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_size"
    )
    public static float getSize(MetaAPI api) {
        return api.avatar.fileSize;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.has_texture"
    )
    public static Boolean hasTexture(MetaAPI api) {
        return api.avatar.hasTexture;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.has_script_error"
    )
    public static Boolean hasScriptError(MetaAPI api) {
        //useless I know
        return api.avatar.scriptError;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_complexity"
    )
    public static int getComplexity(MetaAPI api) {
        return api.avatar.complexity;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_init_count"
    )
    public static int getInitCount(MetaAPI api) {
        return api.avatar.initInstructions;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_tick_count"
    )
    public static int getTickCount(MetaAPI api) {
        return api.avatar.tickInstructions;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_render_count"
    )
    public static int getRenderCount(MetaAPI api) {
        return api.avatar.renderInstructions;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_script_memory"
    )
    public static int getScriptMemory(MetaAPI api) {
        return api.avatar.luaState.getTotalMemory() - api.avatar.luaState.getFreeMemory();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_max_init_count"
    )
    public static int getMaxInitCount(MetaAPI api) {
        return api.trust.get(TrustContainer.Trust.INIT_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_max_tick_count"
    )
    public static int getMaxTickCount(MetaAPI api) {
        return api.trust.get(TrustContainer.Trust.TICK_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_max_render_count"
    )
    public static int getMaxRenderCount(MetaAPI api) {
        return api.trust.get(TrustContainer.Trust.RENDER_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_max_script_memory"
    )
    public static int getMaxScriptMemory(MetaAPI api) {
        return api.trust.get(TrustContainer.Trust.MAX_MEM);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_max_complexity"
    )
    public static int getMaxComplexity(MetaAPI api) {
        return api.trust.get(TrustContainer.Trust.COMPLEXITY);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_max_particles"
    )
    public static int getMaxParticles(MetaAPI api) {
        return api.trust.get(TrustContainer.Trust.PARTICLES);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_max_sounds"
    )
    public static int getMaxSounds(MetaAPI api) {
        return api.trust.get(TrustContainer.Trust.SOUNDS);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.can_edit_nameplate"
    )
    public static boolean canEditNameplate(MetaAPI api) {
        TrustContainer.Trust trust = TrustContainer.Trust.NAMEPLATE_EDIT;
        return trust.asBoolean(api.trust.get(trust));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.can_render_offscreen"
    )
    public static boolean canRenderOffscreen(MetaAPI api) {
        TrustContainer.Trust trust = TrustContainer.Trust.OFFSCREEN_RENDERING;
        return trust.asBoolean(api.trust.get(trust));
    }
}
