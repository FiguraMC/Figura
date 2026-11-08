package org.figuramc.figura.mixin.sound;

import com.mojang.blaze3d.audio.OggAudioStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.ByteBuffer;
import java.util.List;

@Mixin(OggAudioStream.OutputConcat.class)
public interface OutputConcatAccessor {
    @Accessor
    ByteBuffer getCurrentBuffer();

    @Accessor
    int getByteCount();

    @Accessor
    void setByteCount(int byteCount);

    @Accessor
    List<ByteBuffer> getBuffers();

    @Invoker("createNewBuffer")
    void makeNewBuf(); // can't be named createNewBuffer because it breaks everything
}

