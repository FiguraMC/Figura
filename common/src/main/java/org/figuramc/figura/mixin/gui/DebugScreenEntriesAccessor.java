package org.figuramc.figura.mixin.gui;

import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugScreenEntries.class)
public interface DebugScreenEntriesAccessor {
    @Invoker("register")
    static ResourceLocation figura$invokeRegister(ResourceLocation resourceLocation, DebugScreenEntry debugScreenEntry) {
        throw new AssertionError();
    }
}
