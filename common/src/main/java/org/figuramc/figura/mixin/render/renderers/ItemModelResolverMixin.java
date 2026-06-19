package org.figuramc.figura.mixin.render.renderers;

import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.ducks.FiguraItemModelResolverExtension;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import org.figuramc.figura.ducks.FiguraItemStackRenderStateExtension;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin implements FiguraItemModelResolverExtension {
    @Unique
    private final ItemStackRenderState figura$ScratchRenderState = new ItemStackRenderState();

    @Inject(method = "updateForTopItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemModelResolver;appendItemLayers(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/ItemOwner;I)V"))
    private void injectItemStack(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemDisplayContext itemDisplayContext, Level level, ItemOwner itemOwner, int i, CallbackInfo ci) {
        ((FiguraItemStackRenderStateExtension)itemStackRenderState).figura$setItemStack(itemStack);
    }

    @Unique
    public int figura$getModelComplexity(ItemStack stack, RandomSource randomSource) {
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(figura$ScratchRenderState, stack, ItemDisplayContext.NONE, WorldAPI.getCurrentWorld(), null, 1);
        if (((FiguraItemStackRenderStateExtension)(this.figura$ScratchRenderState)).figura$getQuads() != null && !((FiguraItemStackRenderStateExtension) (this.figura$ScratchRenderState)).figura$getQuads().isEmpty())
            return ((FiguraItemStackRenderStateExtension)(this.figura$ScratchRenderState)).figura$getQuads().size();
        return 20;
    }
}
