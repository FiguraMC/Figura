package org.figuramc.figura.mixin.render.renderers;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public interface ItemStackRenderState$LayerRenderStateAccessor {

    @Intrinsic
    @Accessor("quads")
    List<BakedQuad> figura$getQuads();

    @Intrinsic
    @Accessor("transform")
    ItemTransform figura$getTransform();
}
