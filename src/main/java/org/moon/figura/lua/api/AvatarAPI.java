package org.moon.figura.lua.api;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.trust.Trust;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "AvatarAPI",
        value = "avatar"
)
public class AvatarAPI {

    private final Avatar avatar;
    public final LuaTable storedStuff = new LuaTable();

    public AvatarAPI(Avatar avatar) {
        this.avatar = avatar;
    }

    private boolean bool(Trust trust) {
        return trust.asBoolean(avatar.trust.get(trust));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {String.class, Object.class},
                    argumentNames = {"key", "value"}
            ),
            value = "avatar.store"
    )
    public void store(@LuaNotNil String key, LuaValue value) {
        storedStuff.set(key, value == null ? LuaValue.NIL : value);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_current_instructions")
    public int getCurrentInstructions() {
        return avatar.luaRuntime == null ? 0 : avatar.luaRuntime.getInstructions();
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_color")
    public String getColor() {
        return avatar.color;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            value = "avatar.set_color"
    )
    public void setColor(Object r, Double g, Double b) {
        FiguraVec3 vec = LuaUtils.parseVec3("setColor", r, g, b, 1, 1, 1);
        avatar.color = ColorUtils.rgbToHex(vec);
        vec.free();
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_version")
    public String getVersion() {
        return avatar.version.toString();
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_authors")
    public String getAuthors() {
        return avatar.authors;
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_name")
    public String getName() {
        return avatar.name;
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_size")
    public double getSize() {
        return avatar.fileSize;
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.has_texture")
    public boolean hasTexture() {
        return avatar.hasTexture;
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.has_script_error")
    public boolean hasScriptError() {
        //useless I know
        return avatar.scriptError;
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_trust_level")
    public String getTrustLevel() {
        return avatar.trust.getGroup().name();
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_init_count")
    public int getInitCount() {
        return avatar.init.pre;
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_entity_init_count")
    public int getEntityInitCount() {
        return avatar.init.post;
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_init_count")
    public int getMaxInitCount() {
        return avatar.trust.get(Trust.INIT_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_tick_count")
    public int getTickCount() {
        return avatar.tick.getTotal();
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_tick_count")
    public int getMaxTickCount() {
        return avatar.trust.get(Trust.TICK_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_render_count")
    public int getRenderCount() {
        return avatar.render.getTotal();
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_render_count")
    public int getMaxRenderCount() {
        return avatar.trust.get(Trust.RENDER_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_world_tick_count")
    public int getWorldTickCount() {
        return avatar.worldTick.getTotal();
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_world_tick_count")
    public int getMaxWorldTickCount() {
        return avatar.trust.get(Trust.WORLD_TICK_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_world_render_count")
    public int getWorldRenderCount() {
        return avatar.worldRender.getTotal();
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_world_render_count")
    public int getMaxWorldRenderCount() {
        return avatar.trust.get(Trust.WORLD_RENDER_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_complexity")
    public int getComplexity() {
        return avatar.complexity.pre;
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_complexity")
    public int getMaxComplexity() {
        return avatar.trust.get(Trust.COMPLEXITY);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_remaining_particles")
    public int getRemainingParticles() {
        return avatar.particlesRemaining.peek();
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_particles")
    public int getMaxParticles() {
        return avatar.trust.get(Trust.PARTICLES);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_remaining_sounds")
    public int getRemainingSounds() {
        return avatar.soundsRemaining.peek();
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_sounds")
    public int getMaxSounds() {
        return avatar.trust.get(Trust.SOUNDS);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_volume")
    public int getVolume() {
        return avatar.trust.get(Trust.VOLUME);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_animation_complexity")
    public int getAnimationComplexity() {
        return avatar.animationComplexity;
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_animation_complexity")
    public int getMaxAnimationComplexity() {
        return avatar.trust.get(Trust.BB_ANIMATIONS);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_texture_size")
    public int getMaxTextureSize() {
        return avatar.trust.get(Trust.TEXTURE_SIZE);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.can_edit_vanilla_model")
    public boolean canEditVanillaModel() {
        return bool(Trust.VANILLA_MODEL_EDIT);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.can_edit_nameplate")
    public boolean canEditNameplate() {
        return bool(Trust.NAMEPLATE_EDIT);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.can_render_offscreen")
    public boolean canRenderOffscreen() {
        return bool(Trust.OFFSCREEN_RENDERING);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.can_use_custom_sounds")
    public boolean canUseCustomSounds() {
        return bool(Trust.CUSTOM_SOUNDS);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.can_have_custom_heads")
    public boolean canHaveCustomHeads() {
        return bool(Trust.CUSTOM_HEADS);
    }

    @Override
    public String toString() {
        return "AvatarAPI";
    }
}
