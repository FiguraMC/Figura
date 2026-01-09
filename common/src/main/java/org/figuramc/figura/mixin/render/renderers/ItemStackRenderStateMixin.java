package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.ducks.FiguraItemStackRenderStateExtension;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.function.BiFunction;

@Mixin(ItemStackRenderState.class)
public class ItemStackRenderStateMixin implements FiguraItemStackRenderStateExtension, FiguraSubmitCallBackExtension {
    @Shadow
    ItemDisplayContext displayContext;
    @Shadow private ItemStackRenderState.LayerRenderState[] layers;
    @Unique
    ItemStack figura$itemStack;
    @Unique
    private BiFunction<MultiBufferSource, PoseStack, Boolean> figura$preRenderingCallback = null;
    @Unique
    private Runnable figura$postRenderingCallback = null;

    @Override
    public void figura$setItemStack(@Nullable ItemStack itemStack) {
        this.figura$itemStack = itemStack;
    }

    @Override
    public ItemStack figura$getItemStack() {
        return figura$itemStack;
    }

    @Override
    public boolean figura$isLeftHanded() {
        return displayContext.leftHand();
    }

    @Override
    public ItemDisplayContext figura$getDisplayContext() {
        return displayContext;
    }

    @Override
    public ItemTransform figura$getItemTransform() {
        for (ItemStackRenderState.LayerRenderState layerRenderState : layers) {
            if (((ItemStackRenderState$LayerRenderStateAccessor)layerRenderState).figura$getTransform() != null)
                return ((ItemStackRenderState$LayerRenderStateAccessor)layerRenderState).figura$getTransform();
        }
        return ItemTransform.NO_TRANSFORM;
    }

    @Override
    public List<BakedQuad> figura$getQuads() {
        for (ItemStackRenderState.LayerRenderState layerRenderState : layers) {
            if (((ItemStackRenderState$LayerRenderStateAccessor)layerRenderState).figura$getQuads() != null)
                return ((ItemStackRenderState$LayerRenderStateAccessor)layerRenderState).figura$getQuads();
        }
        return List.of();
    }

    @Override
    public void figura$setPreRenderingCallback(BiFunction<MultiBufferSource, PoseStack, Boolean> callback) {
        this.figura$preRenderingCallback = callback;
    }

    @Override
    public void figura$setPostRenderingCallback(Runnable callback) {
        this.figura$postRenderingCallback = callback;
    }

    @Override
    public Runnable figura$getPostRenderingCallback() {
        return figura$postRenderingCallback;
    }

    @Override
    public BiFunction<MultiBufferSource, PoseStack, Boolean> figura$getPreRenderingCallback() {
        return figura$preRenderingCallback;
    }
}
