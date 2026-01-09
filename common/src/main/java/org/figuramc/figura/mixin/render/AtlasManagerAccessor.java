package org.figuramc.figura.mixin.render;

import net.minecraft.client.resources.model.AtlasManager;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(AtlasManager.class)
public interface AtlasManagerAccessor {
    @Intrinsic
    @Accessor("KNOWN_ATLASES")
    static List<AtlasManager.AtlasConfig> getVanillaAtlases() {
        throw new AssertionError();
    }
}
