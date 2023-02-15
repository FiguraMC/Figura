package org.moon.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.permissions.Permissions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

    @Unique
    private static Avatar fireAvatar;

    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private static void renderFire(Minecraft client, PoseStack matrices, CallbackInfo ci) {
        fireAvatar = null;

        Avatar avatar = AvatarManager.getAvatar(client.getCameraEntity());
        if (avatar == null || avatar.luaRuntime == null || avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 0)
            return;

        if (!avatar.luaRuntime.renderer.renderFire)
            ci.cancel();
        else fireAvatar = avatar;
    }

    @ModifyVariable(method = "renderFire", at = @At("STORE"), ordinal = 0)
    private static TextureAtlasSprite secondFireTexture(TextureAtlasSprite sprite) {
        if (fireAvatar == null)
            return sprite;

        ResourceLocation layer1 = fireAvatar.luaRuntime.renderer.fireLayer1;
        ResourceLocation layer2 = fireAvatar.luaRuntime.renderer.fireLayer2;
        return layer2 != null ? Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(layer2) : layer1 != null ? Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(layer1) : sprite;
    }
}
