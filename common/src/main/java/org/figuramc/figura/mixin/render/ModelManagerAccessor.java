package org.figuramc.figura.mixin.render;

import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ModelManager.class)
public interface ModelManagerAccessor {
    @Intrinsic
    @Accessor("VANILLA_ATLASES")
    static Map<ResourceLocation, ResourceLocation> getVanillaAtlases() {
        throw new AssertionError();
    }
}
