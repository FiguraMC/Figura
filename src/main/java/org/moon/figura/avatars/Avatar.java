package org.moon.figura.avatars;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import org.moon.figura.FiguraMod;

import java.io.FileOutputStream;
import java.io.IOException;

//the avatar class
//contains all things related to the avatar
//and also related to the owner, like trust settings
public class Avatar {

    private AvatarMetadata metadata;

    public Avatar(NbtCompound nbt) {
        //TESTING
        try {
            NbtIo.writeCompressed(nbt, new FileOutputStream(FiguraMod.getFiguraDirectory().resolve("avatarOutput.moon").toFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class AvatarMetadata {
        String name, author, version;
        float size;
    }
}
