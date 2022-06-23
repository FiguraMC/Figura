package org.moon.figura.lua.api;

import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;

@LuaWhitelist
@LuaTypeDoc(
        name = "MetaAPI",
        description = "meta"
)
public class MetaAPI {

    private final Avatar avatar;
    private final TrustContainer trust;

    private String color = "";

    public MetaAPI(Avatar avatar) {
        this.avatar = avatar;
        this.trust = TrustManager.get(avatar.owner);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {MetaAPI.class, String.class, Object.class},
                    argumentNames = {"meta", "key", "value"}
            ),
            description = "meta.store"
    )
    public static void store(@LuaNotNil MetaAPI api, @LuaNotNil String key, Object value) {
        api.avatar.luaState.storedStuff.putValue(key, value);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_color"
    )
    public static String getColor(@LuaNotNil MetaAPI api) {
        return api.color;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {MetaAPI.class, String.class},
                    argumentNames = {"meta", "color"}
            ),
            description = "meta.set_color"
    )
    public static void setColor(@LuaNotNil MetaAPI api, @LuaNotNil String text) {
        api.color = text;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_version"
    )
    public static String getVersion(@LuaNotNil MetaAPI api) {
        return api.avatar.version;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_authors"
    )
    public static String getAuthors(@LuaNotNil MetaAPI api) {
        return api.avatar.authors;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_name"
    )
    public static String getName(@LuaNotNil MetaAPI api) {
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
    public static float getSize(@LuaNotNil MetaAPI api) {
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
    public static Boolean hasTexture(@LuaNotNil MetaAPI api) {
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
    public static Boolean hasScriptError(@LuaNotNil MetaAPI api) {
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
    public static int getComplexity(@LuaNotNil MetaAPI api) {
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
    public static int getInitCount(@LuaNotNil MetaAPI api) {
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
    public static int getTickCount(@LuaNotNil MetaAPI api) {
        return api.avatar.tickInstructions;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_world_tick_count"
    )
    public static int getWorldTickCount(@LuaNotNil MetaAPI api) {
        return api.avatar.worldTickInstructions;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_render_count"
    )
    public static int getRenderCount(@LuaNotNil MetaAPI api) {
        return api.avatar.renderInstructions;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_world_render_count"
    )
    public static int getWorldRenderCount(@LuaNotNil MetaAPI api) {
        return api.avatar.worldRenderInstructions;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_script_memory"
    )
    public static int getScriptMemory(@LuaNotNil MetaAPI api) {
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
    public static int getMaxInitCount(@LuaNotNil MetaAPI api) {
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
    public static int getMaxTickCount(@LuaNotNil MetaAPI api) {
        return api.trust.get(TrustContainer.Trust.TICK_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_max_world_tick_count"
    )
    public static int getMaxWorldTickCount(@LuaNotNil MetaAPI api) {
        return api.trust.get(TrustContainer.Trust.WORLD_TICK_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_max_render_count"
    )
    public static int getMaxRenderCount(@LuaNotNil MetaAPI api) {
        return api.trust.get(TrustContainer.Trust.RENDER_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_max_world_render_count"
    )
    public static int getMaxWorldRenderCount(@LuaNotNil MetaAPI api) {
        return api.trust.get(TrustContainer.Trust.WORLD_RENDER_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_max_script_memory"
    )
    public static long getMaxScriptMemory(@LuaNotNil MetaAPI api) {
        return Math.min(Integer.MAX_VALUE, Math.min(api.trust.get(TrustContainer.Trust.MAX_MEM), 2048) * 1_000_000L);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = MetaAPI.class,
                    argumentNames = "meta"
            ),
            description = "meta.get_max_complexity"
    )
    public static int getMaxComplexity(@LuaNotNil MetaAPI api) {
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
    public static int getMaxParticles(@LuaNotNil MetaAPI api) {
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
    public static int getMaxSounds(@LuaNotNil MetaAPI api) {
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
    public static boolean canEditNameplate(@LuaNotNil MetaAPI api) {
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
    public static boolean canRenderOffscreen(@LuaNotNil MetaAPI api) {
        TrustContainer.Trust trust = TrustContainer.Trust.OFFSCREEN_RENDERING;
        return trust.asBoolean(api.trust.get(trust));
    }

    @Override
    public String toString() {
        return "MetaAPI";
    }
}
