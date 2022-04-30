package org.moon.figura.avatars;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.model.rendering.AvatarRenderer;
import org.moon.figura.avatars.model.rendering.ImmediateAvatarRenderer;
import org.moon.figura.lua.FiguraLuaState;
import org.moon.figura.lua.api.EventsAPI;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.terasology.jnlua.LuaRuntimeException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

//the avatar class
//contains all things related to the avatar
//and also related to the owner, like trust settings
public class Avatar {

    //metadata
    public final String name;
    public final String author;
    public final String version;
    public final float fileSize;

    //Runtime data
    public final UUID owner;
    private final AvatarRenderer renderer;
    public FiguraLuaState luaState;

    public Avatar(CompoundTag nbt, UUID owner) {
        this.owner = owner;

        //read metadata
        CompoundTag metadata = nbt.getCompound("metadata");
        name = metadata.getString("name");
        author = metadata.getString("author");
        version = metadata.getString("ver");
        fileSize = getFileSize(nbt);
        renderer = new ImmediateAvatarRenderer(this, nbt);
        luaState = createLuaState(nbt);
    }

    private void tryCall(EventsAPI.LuaEvent event, Object... args) {
        try {
            event.call(args);
        } catch (LuaRuntimeException ex) {
            FiguraLuaState.sendLuaError(ex.getMessage(), name);
            luaState.close();
            luaState = null;
        }
    }

    public void onTick() {
        if (luaState != null)
            tryCall(luaState.events.tick);
    }

    public void onRender(Entity entity, float yaw, float delta, PoseStack matrices, MultiBufferSource bufferSource, int light) {
        renderer.entity = entity;
        renderer.yaw = yaw;
        renderer.tickDelta = delta;
        renderer.matrices = matrices;
        renderer.bufferSource = bufferSource;
        renderer.light = light;
        if (luaState != null)
            tryCall(luaState.events.render, delta);
        renderer.render();
        if (luaState != null)
            tryCall(luaState.events.postRender, delta);
    }



    /**
     * We should call this whenever an avatar is no longer reachable!
     * It free()s all the CachedType used inside of the avatar, and also
     * closes the native texture resources.
     */
    public void clean() {
        renderer.clean();
    }

    private float getFileSize(CompoundTag nbt) {
        try {
            //get size
            DataOutputStream dos = new DataOutputStream(new ByteArrayOutputStream());
            NbtIo.writeCompressed(nbt, dos);
            long size = dos.size();

            //format size to kb
            DecimalFormat df = new DecimalFormat("#0.00", new DecimalFormatSymbols(Locale.US));
            df.setRoundingMode(RoundingMode.HALF_UP);
            return Float.parseFloat(df.format(size / 1000f));
        } catch (Exception e) {
            FiguraMod.LOGGER.warn("Failed to generate file size for model " + this.name, e);
            return 0f;
        }
    }

    private FiguraLuaState createLuaState(CompoundTag avatarNbt) {
        if (!avatarNbt.contains("scripts"))
            return null;

        Map<String, String> scripts = parseScripts(avatarNbt.getCompound("scripts"));
        String mainScriptName = avatarNbt.getString("script");
        if (!avatarNbt.contains("script", Tag.TAG_STRING)) mainScriptName = "script";

        FiguraLuaState luaState = new FiguraLuaState(name, TrustManager.get(owner).get(TrustContainer.Trust.MAX_MEM));

        if (renderer != null && renderer.root != null)
            luaState.loadGlobal(renderer.root, "models");

        boolean success = luaState.init(scripts, mainScriptName);
        if (success)
            return luaState;
        else
            luaState.close();
        return null;
    }

    private static Map<String, String> parseScripts(CompoundTag scripts) {
        Map<String, String> result = new HashMap<>();
        for (String s : scripts.getAllKeys()) {
            StringBuilder builder = new StringBuilder();
            ListTag list = scripts.getList(s, Tag.TAG_STRING);
            for (Tag tag : list)
                builder.append(tag.getAsString());
            result.put(s, builder.toString());
        }
        return result;
    }
}
