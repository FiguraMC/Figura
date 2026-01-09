package org.figuramc.figura.mixin.render.renderers;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.client.renderer.special.PlayerHeadSpecialRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpecialModelWrapper.class)
public class SpecialModelWrapperMixin<T> {
    @Shadow @Final private SpecialModelRenderer<T> specialRenderer;

    @Inject(method = "update", at = @At(value = "RETURN"))
    public void update(CallbackInfo ci, @Local(argsOnly = true) ItemStackRenderState layerRenderState, @Local(argsOnly = true)ItemStack stack) {
        ResolvableProfile profile = stack.get(DataComponents.PROFILE);
        Avatar avatar = (profile != null && profile.partialProfile() != null) ? AvatarManager.getAvatarForPlayer(profile.partialProfile().id()) : null;
        if (avatar != null && specialRenderer instanceof PlayerHeadSpecialRenderer)
            layerRenderState.setAnimated();
    }
}
