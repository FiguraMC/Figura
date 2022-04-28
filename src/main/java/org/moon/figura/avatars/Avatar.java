package org.moon.figura.avatars;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.model.rendering.AvatarRenderer;
import org.moon.figura.avatars.model.rendering.ImmediateAvatarRenderer;
import org.moon.figura.lua.FiguraLuaState;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;

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
    public final AvatarRenderer renderer;
    public final FiguraLuaState luaState;

    public Avatar(CompoundTag nbt, UUID owner) {
        this.owner = owner;

        //read metadata
        CompoundTag metadata = nbt.getCompound("metadata");
        name = metadata.getString("name");
        author = metadata.getString("author");
        version = metadata.getString("ver");
        fileSize = getFileSize(nbt);
        renderer = new ImmediateAvatarRenderer(this, nbt);
        luaState = createLuaState(nbt, owner);
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

    private static FiguraLuaState createLuaState(CompoundTag avatarNbt, UUID owner) {
        if (!avatarNbt.contains("scripts"))
            return null;

        Map<String, String> scripts = parseScripts(avatarNbt.getCompound("scripts"));
        String mainScriptName = avatarNbt.getString("script");
        if (!avatarNbt.contains("script", Tag.TAG_STRING)) mainScriptName = "script";

        FiguraLuaState luaState = new FiguraLuaState(TrustManager.get(owner).get(TrustContainer.Trust.MAX_MEM));
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
