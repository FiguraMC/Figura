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
import org.moon.figura.permissions.Permissions;
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

    private boolean bool(Permissions permissions) {
        return permissions.asBoolean(avatar.permissions.get(permissions));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {String.class, Object.class},
                    argumentNames = {"key", "value"}
            ),
            value = "avatar.store"
    )
    public AvatarAPI store(@LuaNotNil String key, LuaValue value) {
        storedStuff.set(key, value == null ? LuaValue.NIL : value);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_uuid")
    public String getUUID() {
        return avatar.owner.toString();
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
            aliases = "color",
            value = "avatar.set_color"
    )
    public AvatarAPI setColor(Object r, Double g, Double b) {
        FiguraVec3 vec = LuaUtils.parseOneArgVec("setColor", r, g, b, 1d);
        avatar.color = ColorUtils.rgbToHex(vec);
        return this;
    }

    @LuaWhitelist
    public AvatarAPI color(Object r, Double g, Double b) {
        return setColor(r, g, b);
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
    @LuaMethodDoc("avatar.get_entity_name")
    public String getEntityName() {
        return avatar.entityName;
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
    @LuaMethodDoc("avatar.get_permission_level")
    public String getPermissionLevel() {
        return avatar.permissions.getCategory().name();
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
        return avatar.permissions.get(Permissions.INIT_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_tick_count")
    public int getTickCount() {
        return avatar.tick.getTotal();
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_tick_count")
    public int getMaxTickCount() {
        return avatar.permissions.get(Permissions.TICK_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_render_count")
    public int getRenderCount() {
        return avatar.render.getTotal();
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_render_count")
    public int getMaxRenderCount() {
        return avatar.permissions.get(Permissions.RENDER_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_world_tick_count")
    public int getWorldTickCount() {
        return avatar.worldTick.getTotal();
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_world_tick_count")
    public int getMaxWorldTickCount() {
        return avatar.permissions.get(Permissions.WORLD_TICK_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_world_render_count")
    public int getWorldRenderCount() {
        return avatar.worldRender.getTotal();
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_world_render_count")
    public int getMaxWorldRenderCount() {
        return avatar.permissions.get(Permissions.WORLD_RENDER_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_complexity")
    public int getComplexity() {
        return avatar.complexity.pre;
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_complexity")
    public int getMaxComplexity() {
        return avatar.permissions.get(Permissions.COMPLEXITY);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_remaining_particles")
    public int getRemainingParticles() {
        return avatar.particlesRemaining.peek();
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_particles")
    public int getMaxParticles() {
        return avatar.permissions.get(Permissions.PARTICLES);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_remaining_sounds")
    public int getRemainingSounds() {
        return avatar.soundsRemaining.peek();
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_sounds")
    public int getMaxSounds() {
        return avatar.permissions.get(Permissions.SOUNDS);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_volume")
    public int getVolume() {
        return avatar.permissions.get(Permissions.VOLUME);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_animation_complexity")
    public int getAnimationComplexity() {
        return avatar.animationComplexity;
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_animation_complexity")
    public int getMaxAnimationComplexity() {
        return avatar.permissions.get(Permissions.BB_ANIMATIONS);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_animation_count")
    public int getAnimationCount() {
        return avatar.animation.pre;
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_animation_count")
    public int getMaxAnimationCount() {
        return avatar.permissions.get(Permissions.ANIMATION_INST);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.get_max_texture_size")
    public int getMaxTextureSize() {
        return avatar.permissions.get(Permissions.TEXTURE_SIZE);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.can_edit_vanilla_model")
    public boolean canEditVanillaModel() {
        return bool(Permissions.VANILLA_MODEL_EDIT);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.can_edit_nameplate")
    public boolean canEditNameplate() {
        return bool(Permissions.NAMEPLATE_EDIT);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.can_render_offscreen")
    public boolean canRenderOffscreen() {
        return bool(Permissions.OFFSCREEN_RENDERING);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.can_use_custom_sounds")
    public boolean canUseCustomSounds() {
        return bool(Permissions.CUSTOM_SOUNDS);
    }

    @LuaWhitelist
    @LuaMethodDoc("avatar.can_have_custom_skull")
    public boolean canHaveCustomSkull() {
        return bool(Permissions.CUSTOM_SKULL);
    }

    @Override
    public String toString() {
        return "AvatarAPI";
    }
}
