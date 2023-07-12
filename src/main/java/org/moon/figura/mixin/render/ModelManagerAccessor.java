package org.moon.figura.mixin.render;

import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.resources.model.ModelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelManager.class)
public interface ModelManagerAccessor {
	@Accessor
	AtlasSet getAtlases();
}
