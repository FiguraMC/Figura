package org.figuramc.figura.mixin.render.renderers;

import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.figuramc.figura.ducks.FiguraItemStackRenderStateExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin implements org.figuramc.figura.ducks.FiguraItemRendererExtension {
    @org.spongepowered.asm.mixin.Unique
    private final ItemStackRenderState figura$scratchRenderState = new ItemStackRenderState();

    @Override
    public int figura$getModelComplexity(ItemStack stack, net.minecraft.util.RandomSource randomSource) {
        ((ItemModelResolver) (Object) this).updateForTopItem(figura$scratchRenderState, stack, ItemDisplayContext.NONE, org.figuramc.figura.lua.api.world.WorldAPI.getCurrentWorld(), null, 1);
        int quads = ((FiguraItemStackRenderStateExtension) figura$scratchRenderState).figura$getQuads().size();
        return quads == 0 ? 20 : quads;
    }

    @Inject(method = "updateForTopItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemModelResolver;appendItemLayers(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/ItemOwner;I)V"))
    private void injectItemStack(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemDisplayContext itemDisplayContext, Level level, ItemOwner itemOwner, int i, CallbackInfo ci) {
        ((FiguraItemStackRenderStateExtension)itemStackRenderState).figura$setItemStack(itemStack);
    }
}
