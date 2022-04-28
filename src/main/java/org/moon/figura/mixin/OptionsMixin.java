package org.moon.figura.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.moon.figura.config.KeyMappingRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class OptionsMixin {

    @Mutable @Shadow @Final public KeyMapping[] keyMappings;

    @Inject(at = @At("HEAD"), method = "load()V")
    public void load(CallbackInfo info) {
        this.keyMappings = KeyMappingRegistry.appendKeys(this.keyMappings);
    }
}
