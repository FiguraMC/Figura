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

import java.util.ArrayList;
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
    private final List<BiFunction<MultiBufferSource, PoseStack, Boolean>> figura$preRenderingCallback = new ArrayList<>();
    @Unique
    private final List<Runnable> figura$postRenderingCallback = new ArrayList<>();

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
    public void figura$addPreRenderingCallback(BiFunction<MultiBufferSource, PoseStack, Boolean> callback) {
        this.figura$preRenderingCallback.add(callback);
    }

    @Override
    public void figura$addPostRenderingCallback(Runnable callback) {
        this.figura$postRenderingCallback.add(callback);
    }

    @Override
    public List<Runnable> figura$getPostRenderingCallbacks() {
        return figura$postRenderingCallback;
    }

    @Override
    public List<BiFunction<MultiBufferSource, PoseStack, Boolean>> figura$getPreRenderingCallbacks() {
        return figura$preRenderingCallback;
    }
}
