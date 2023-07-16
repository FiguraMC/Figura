package org.figuramc.figura.mixin.input;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {

    @Intrinsic
    @Accessor("ALL")
    static Map<String, KeyMapping> getAll() {
        throw new AssertionError();
    }
}
