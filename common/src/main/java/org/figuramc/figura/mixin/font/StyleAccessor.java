package org.figuramc.figura.mixin.font;

import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Style.class)
public interface StyleAccessor {
    @Intrinsic
    @Mutable
    @Accessor("obfuscated")
    void setObfuscated(Boolean bool);
}
