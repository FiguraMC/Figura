package org.figuramc.figura.mixin;

import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.DataOutput;

@Mixin(NbtIo.class)
public interface NbtIoAccessor {

    @Invoker("writeUnnamedTag")
    static void figura$invokeWriteUnnamedTag(Tag compound, DataOutput output) {
        throw new AssertionError();
    }
}
