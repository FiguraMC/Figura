package org.moon.figura.lua.api;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "AvatarAPI",
        description = "avatar"
)
public class AvatarAPI {

    private final Avatar avatar;
    public final LuaTable storedStuff = new LuaTable();

    public AvatarAPI(Avatar avatar) {
        this.avatar = avatar;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {String.class, Object.class},
                    argumentNames = {"key", "value"}
            ),
            description = "avatar.store"
    )
    public void store(@LuaNotNil String key, LuaValue value) {
        storedStuff.set(key, value == null ? LuaValue.NIL : value);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_current_instructions")
    public int getCurrentInstructions() {
        return avatar.luaRuntime == null ? 0 : avatar.luaRuntime.getInstructions();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_color")
    public String getColor() {
        return avatar.color;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            description = "avatar.set_color"
    )
    public void setColor(Object r, Double g, Double b) {
        FiguraVec3 vec = LuaUtils.parseVec3("setColor", r, g, b, 1, 1, 1);
        avatar.color = ColorUtils.rgbToHex(vec);
        vec.free();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_version")
    public String getVersion() {
        return avatar.version;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_authors")
    public String getAuthors() {
        return avatar.authors;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_name")
    public String getName() {
        return avatar.name;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_size")
    public double getSize() {
        return avatar.fileSize;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.has_texture")
    public boolean hasTexture() {
        return avatar.hasTexture;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.has_script_error")
    public boolean hasScriptError() {
        //useless I know
        return avatar.scriptError;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_init_count")
    public int getInitCount() {
        return avatar.initInstructions;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_max_init_count")
    public int getMaxInitCount() {
        return avatar.trust.get(TrustContainer.Trust.INIT_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_tick_count")
    public int getTickCount() {
        return avatar.entityTickInstructions;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_max_tick_count")
    public int getMaxTickCount() {
        return avatar.trust.get(TrustContainer.Trust.TICK_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_render_count")
    public int getRenderCount() {
        return avatar.entityRenderInstructions;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_max_render_count")
    public int getMaxRenderCount() {
        return avatar.trust.get(TrustContainer.Trust.RENDER_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_world_tick_count")
    public int getWorldTickCount() {
        return avatar.worldTickInstructions;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_max_world_tick_count")
    public int getMaxWorldTickCount() {
        return avatar.trust.get(TrustContainer.Trust.WORLD_TICK_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_world_render_count")
    public int getWorldRenderCount() {
        return avatar.worldRenderInstructions;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_max_world_render_count")
    public int getMaxWorldRenderCount() {
        return avatar.trust.get(TrustContainer.Trust.WORLD_RENDER_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_complexity")
    public int getComplexity() {
        return avatar.complexity;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_max_complexity")
    public int getMaxComplexity() {
        return avatar.trust.get(TrustContainer.Trust.COMPLEXITY);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_remaining_particles")
    public int getRemainingParticles() {
        return avatar.particlesRemaining.peek();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_max_particles")
    public int getMaxParticles() {
        return avatar.trust.get(TrustContainer.Trust.PARTICLES);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_remaining_sounds")
    public int getRemainingSounds() {
        return avatar.soundsRemaining.peek();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_max_sounds")
    public int getMaxSounds() {
        return avatar.trust.get(TrustContainer.Trust.SOUNDS);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_animation_complexity")
    public int getAnimationComplexity() {
        return avatar.animationComplexity;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.get_max_animation_complexity")
    public int getMaxAnimationComplexity() {
        return avatar.trust.get(TrustContainer.Trust.BB_ANIMATIONS);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.can_edit_vanilla_model")
    public boolean canEditVanillaModel() {
        TrustContainer.Trust trust = TrustContainer.Trust.VANILLA_MODEL_EDIT;
        return trust.asBoolean(avatar.trust.get(trust));
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.can_edit_nameplate")
    public boolean canEditNameplate() {
        TrustContainer.Trust trust = TrustContainer.Trust.NAMEPLATE_EDIT;
        return trust.asBoolean(avatar.trust.get(trust));
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.can_render_offscreen")
    public boolean canRenderOffscreen() {
        TrustContainer.Trust trust = TrustContainer.Trust.OFFSCREEN_RENDERING;
        return trust.asBoolean(avatar.trust.get(trust));
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "avatar.can_use_custom_sounds")
    public boolean canUseCustomSounds() {
        TrustContainer.Trust trust = TrustContainer.Trust.CUSTOM_SOUNDS;
        return trust.asBoolean(avatar.trust.get(trust));
    }

    @Override
    public String toString() {
        return "AvatarAPI";
    }
}
