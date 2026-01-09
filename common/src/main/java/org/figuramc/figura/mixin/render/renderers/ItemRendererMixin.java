package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraItemRendererExtension;
import org.figuramc.figura.ducks.FiguraItemStackRenderStateExtension;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin implements FiguraItemRendererExtension {
    @Unique
    private final ItemStackRenderState figura$ScratchRenderState = new ItemStackRenderState();

    @Unique
    public int figura$getModelComplexity(ItemStack stack, RandomSource randomSource) {
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(figura$ScratchRenderState, stack, ItemDisplayContext.NONE, WorldAPI.getCurrentWorld(), null, 1);
        if (((FiguraItemStackRenderStateExtension)(this.figura$ScratchRenderState)).figura$getQuads() != null && !((FiguraItemStackRenderStateExtension) (this.figura$ScratchRenderState)).figura$getQuads().isEmpty())
            return ((FiguraItemStackRenderStateExtension)(this.figura$ScratchRenderState)).figura$getQuads().size();
        return 20;
    }
}
