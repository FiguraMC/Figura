package org.moon.figura.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.providers.LocalAvatarFetcher;
import org.moon.figura.avatars.providers.LocalAvatarLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    private static Avatar avatar = null;

    @Inject(at = @At("HEAD"), method = "render")
    private void testDrawing(Entity e, float yaw, float delta, PoseStack m, MultiBufferSource bufferSource, int l, CallbackInfo ci) {

        //Create avatar if not done already
        if (avatar == null) {
            LocalAvatarFetcher.load();
            if (!LocalAvatarFetcher.ALL_AVATARS.isEmpty()) {
                CompoundTag nbt = LocalAvatarLoader.loadAvatar(LocalAvatarFetcher.ALL_AVATARS.get(0).getPath());
                if (nbt != null) {
                    avatar = new Avatar(nbt);
                    LocalAvatarLoader.saveNbt();
                    FiguraMod.LOGGER.warn(avatar.toString());
                }
            }
        }

        //Render avatar with params
        avatar.renderer.entity = e;
        avatar.renderer.yaw = yaw;
        avatar.renderer.tickDelta = delta;
        avatar.renderer.matrices = m;
        avatar.renderer.light = l;
        avatar.renderer.bufferSource = bufferSource;

        avatar.renderer.render();
    }

}
