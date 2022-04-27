package org.moon.figura.avatars;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.model.rendering.AvatarRenderer;
import org.moon.figura.avatars.model.rendering.ImmediateAvatarRenderer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

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
    public final AvatarRenderer renderer;

    public Avatar(CompoundTag nbt) {
        //read metadata
        CompoundTag metadata = nbt.getCompound("metadata");
        name = metadata.getString("name");
        author = metadata.getString("author");
        version = metadata.getString("ver");
        fileSize = getFileSize(nbt);
        renderer = new ImmediateAvatarRenderer(this, nbt);
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
            FiguraMod.LOGGER.error("Failed to generate file size for model " + this.name, e);
            return 0f;
        }
    }
}
